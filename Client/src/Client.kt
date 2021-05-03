import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

class Client(
    val host: String,
    val port : Int
    ) {
    private val socket: Socket
    private val communicator: SocketIO
    //private val SessionFinishedListener= mutableListOf<()->Unit>()
    public fun addSessionFinishedListener(l:()->Unit){
        //SessionFinishedListener.add(l)
        communicator.addSocketClosedListener(l)
    }
    public fun removeSessionFinishedListener(l:()->Unit){
        //SessionFinishedListener.add(l)
        communicator.removeSocketClosedListener(l)
    }

    init{
        //указываем два параметра для сокета и порта
        // при создания сокета происходит попытка подключения к серверу
        socket=Socket(host,port)
        //через классы bufferedReader и printWriter сможем обменяться с сервером. Клиент и сервер должен работать согласованно
        //т.е на сервере мы ожидаем данные, то на клиенте мы отправляем; потом он отправляет а клиент принимает
        communicator=SocketIO(socket)
    }
    fun stop(){
        communicator.stop()
    }
    fun start(){
        val pw=PrintWriter(socket.getOutputStream())
        pw.println("Hi! Can i connect?")
        //скидываем в сеть информацию в буферезованной памяти
        pw.flush()
        val br = BufferedReader(InputStreamReader(socket.getInputStream()))
        val response=br.readLine()
        println("Server response : \"$response\"")
        //закрытие сокета
        socket.close()
    }
    fun start2(){
        communicator.startDataReceiving()
        communicator.addDataListener {
            println(it)//обработчик события в форму -окошко
        }
    }

    fun send(data: String) {
        communicator.sendData(data)
    }
}