import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.ServerSocket
import java.net.Socket

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
     * Ожидание новых подключений к серверу
     * Приостанавливаемая функция и продолжение в месте остановки для корутин
     * CoroutineScope - не блокирует основной процесс
     */
    fun ClientsWait() = CoroutineScope(Dispatchers.Default).launch {
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

    /**
     * Старт сервера
     */
    fun start(){
        stop=false
        ClientsWait()
    }
}