import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

/**
 * Класс для работы с клиентами, NETWORK WITH CLIENTS
 * @param port - порт для работы с клиентами
 */
class Server(val port : Int=5804) {

    private val sSocket: ServerSocket
    private val clients= mutableListOf<Client>()
    private var stop=false

    /** Вложенный класс клиентов, чтобы сервер хранил 'копии' подключенных клиентов
     * и взаимодействовал с подключенными клиентами
     * @param socket - сокет подключенного клиента
     */
    inner class Client(val socket: Socket){
        private var sock:SocketIO?=null

        /**
         * Обработка информации с подключенными клиентами
         */
        fun startDialog(){
            sock=SocketIO(socket).apply{
                addSocketClosedListener {
                    clients.remove(this@Client)
                }
                addDataListener{
                    clients.forEach{ client ->
                        if(client!=this@Client) client.sock?.sendData(this@Client.toString()+" :"+it)
                        else{
                            client.sock?.sendData("You said: "+it)
                        }
                    }
                }
                startDataReceiving()
            }
        }

        /**
         * Остановка всех подключений
         */
        fun stop(){
            sock?.stop()
        }
    }
    init{
        sSocket= ServerSocket(port)
    }

    /**
     * Закрытие сокета сервера
     */
    fun stop(){
        sSocket.close()
        stop=true
    }

    /**
     * Остановка всех клиентов
     */
    private fun stopAllClient(){
        clients.forEach{it.stop()}
    }

    /**
     * Старт сервера
     * Сервер постоянно ждет новых подключений к нему
     */
    fun start(){
        stop=false
        thread{
            try{
                while(!stop){
                    clients.add(Client(sSocket.accept()).also{client->client.startDialog()})
                }
            }catch (e: Exception){
                println("${e.message}")
            }
            finally {
                stopAllClient()
                sSocket.close()
                println("Сервер остановлен")
            }

        }
    }
}