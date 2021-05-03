import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import kotlin.concurrent.thread

class SocketIO(val socket : Socket) {
    //для остановки клиента
    private var stop = false
    private var thread:Thread?=null
    //события когда теряется соединение с сокетом
    private val socketClosedListener = mutableListOf<()->Unit>()

    fun addSocketClosedListener(l:()->Unit){
        socketClosedListener.add(l)
    }
    fun removeSocketClosedListener(l:()->Unit){
        socketClosedListener.remove(l)
    }


    fun stop(){
        stop=true
        socket.close()
    }
    fun startDataReceiving() {
        stop=false
        thread=thread{
            try {
                val br = BufferedReader(InputStreamReader(socket.getInputStream()))
                while (!stop) {
                    //считываем из потока строчки, который будет подвешивать процесс пока нет данных(нет символа конца строки)
                    val data = br.readLine()
                    if(data!=null) {
                        //вывод данных
                        println(data)//////более осмысленное действие с данными
                    }else{
                        //println("Связь прервалась ")
                        throw IOException("Связь прервалась")
                    }
                }
            }catch (ex:Exception){
                socket.close()
                socketClosedListener.forEach { it() }
                println(ex.message)//вызывалось изза строке br.readLine когда в stop socket.close() вызываем
            }
            finally{
                socket.close()
                socketClosedListener.forEach { it() }
            }
        }
    }
    fun sendData(data:String):Boolean{
        try {
            val pw = PrintWriter(socket.getOutputStream())
            pw.println(data)
            pw.flush()
            return true
        }catch (ex:Exception){
            return false
        }
    }

}