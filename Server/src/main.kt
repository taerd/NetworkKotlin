import java.util.*

fun main(){
    val s = Server()
    s.start2()

    var cmd: String
    val sc= Scanner(System.`in`)
    do{
        cmd=sc.nextLine()
    }while (cmd!="STOP")
    s.stop()
}