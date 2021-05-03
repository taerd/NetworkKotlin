import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

fun main(){
    var inputThread:Thread?=null
    //поскольку сервер работает на том же компе что и клиент указываем localhost
    val client = Client("localhost",5804)
    client.start2()
    //получаем информацию с клавиатуры чтобы передать
    val br = BufferedReader(InputStreamReader(System.`in`))
    var data:String

    client.addSessionFinishedListener{
        println("Работа с сервером завершена, нажмите Enter для выхода..")
        br.close()
    }
    inputThread= thread{
        try{
            do{
                data=br.readLine()
                //считываем очередную строчку с консоли и отправляем клиенту
                client.send(data)
            }while(data!="STOP")
            client.stop()
        }catch (ex: Exception){
            println("${ex.message}")
        }
    }
}