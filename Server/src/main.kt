import java.util.*

fun main(){
    //Запуск сервера
    val s = Server()
    s.start()

    //Сервер остановится при вводе "STOP" в его консоль
    var cmd: String
    val sc= Scanner(System.`in`)
    do{
        cmd=sc.nextLine()
    }while (cmd!="STOP")
    s.stop()
}