import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class Server(val port : Int=5804) {//значение по умолчанию 5804

    private val sSocket: ServerSocket //переменная типа серверсокет

    //cписок клиентов
    private val clients= mutableListOf<Client>()

    private var stop=false

    //создадим список клиентов, которых лучше оформить в виде отдельного класса
    //будет хранить сокеты
    inner class Client(val socket: Socket){//внутренний класс имеет доступ к классу(методы,свойства) в котором он пишется
        private var sock:SocketIO?=null
        fun startDialog(){

            sock=SocketIO(socket).apply{
                addSocketClosedListener { //есть доступ к clients тк как класс inner
                    clients.remove(this@Client)//указание с аннотацией тк как есть 2 this (от apply и this.client) и this@socket перекроет this@client
                }
                addDataListener{
                    clients.forEach{ client ->
                        if(client!=this@Client) client.sock?.sendData(this@Client.toString()+" :"+it)
                    }
                }
                //отдельно будем запускать и запускать взаимодействие с SocketIO
                startDataReceiving()//начать получение данных с противоположной стороны в thread
            }
        }

        fun stop(){
            sock?.stop()
        }
    }
    init{
        sSocket= ServerSocket(port)
    }

    fun stop(){
        sSocket.close()
        stop=true
    }


    //все заключить в try catch ибо может возникнуть исключетельная ситуация в случае физического разрыва сети
    fun start(){
        //подвисаем на accept пока не подключится клиент(висит все приложение) в плане обмена информации с другими клиентами плохо
        //ибо каждый раз ожидаем подключение
        //Напишем кратко без потоков для примера отправим строку клиенту и примем от него
        val acceptedClient =sSocket.accept()//прослушивание порта, принять соединение, возвращает клиентский сокет
        //для работы с потоками клиентов (вывода ввода)
        val br = BufferedReader(InputStreamReader(acceptedClient.getInputStream()))
        val clientQuery=br.readLine()
        println("Client send: \"$clientQuery\"")
        //получим поток на outputStream
        val pw=PrintWriter(acceptedClient.getOutputStream())
        //Символ конца строки 'ln' должен стоять, чтобы завершить запись так как и на чтении через readLine
        pw.println("You were successfully connected!")
        //в памяти создается блок (буфферезиванный)  куда записывает информация, отправится только тогда, когда он заполнится
        //нужно скидывать принудительно из буфера в сеть информацию
        //flush очищает буфер и скидывает информацию в сеть / либо завершать поток, тогда информация скидывается тоже из буфера
        pw.flush()
        //закрытие сокета клиентов
        acceptedClient.close()
    }
    private fun stopAllClient(){
        //clients.forEach {client -> client.stop() }
        clients.forEach{it.stop()}
    }

    fun start2(){
        stop=false

        thread{ //нужно проводить в отдельном подпроцессе
            try{
                while(!stop){//ожидаем каждый раз клиентов.
                    // создает класс клиент из sSocket.accept() и сразу добавляем его в список clients, но и созданного класса клиента вызываем startDialog()
                    //висим на accepte
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

        /*
        val br = BufferedReader(InputStreamReader(acceptedClient.getInputStream()))
        val clientQuery=br.readLine()
        println("Client send: \"$clientQuery\"")
        val pw=PrintWriter(acceptedClient.getOutputStream())
        pw.println("You were successfully connected!")
        pw.flush()
        acceptedClient.close()
        */
    }
}