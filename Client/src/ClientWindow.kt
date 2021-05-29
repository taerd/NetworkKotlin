import java.awt.Button
import java.awt.Color
import java.awt.Dimension
import java.awt.TextField
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.Socket
import javax.swing.*

/**
 * Клиент, который взаимодействует с пользователем через окно
 * @param host -адрес сервера
 * @param port - порт сервера
 */
class ClientWindow (
        val host : String,
        val port : Int
        ) :JFrame() {

    //window properties
    private val minSize = Dimension(550,400)
    private val textArea : JTextArea
    private val textField : TextField
    private val sendButton : Button
    private val scroll : JScrollPane

    //client properties
    private val socket: Socket
    private val communicator: SocketIO

    init{
        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = Dimension(minSize.width+300, minSize.height+300)
        textArea = JTextArea()
        textArea.background= Color.WHITE
        textArea.setBorder(BorderFactory.createLineBorder(Color.BLACK))
        textArea.isEditable=false
        //textArea.lineWrap=false
        scroll = JScrollPane(textArea)
        //scroll.verticalScrollBarPolicy
        //textArea.lineWrap=true
        //textArea.wrapStyleWord=false

        textField = TextField()
        sendButton = Button("send")

        layout = GroupLayout(contentPane).apply{
            setVerticalGroup(
                    createSequentialGroup()
                            .addGap(5)
                            .addComponent(textArea,minSize.height,GroupLayout.DEFAULT_SIZE,GroupLayout.DEFAULT_SIZE)
                            .addGap(5)
                            .addGroup(createParallelGroup()
                                    .addComponent(textField,50,50,50)
                                    .addGap(5)
                                    .addComponent(sendButton,50,50,50)
                            )
                            .addGap(5)
            )
            setHorizontalGroup(
                    createSequentialGroup()
                            .addGap(5)
                            .addGroup(
                                    createParallelGroup()
                                            .addComponent(textArea,minSize.width,GroupLayout.DEFAULT_SIZE,GroupLayout.DEFAULT_SIZE)
                                            .addGap(5)
                                            .addGroup(
                                                    createSequentialGroup()
                                                            .addComponent(textField,400,GroupLayout.DEFAULT_SIZE,GroupLayout.DEFAULT_SIZE)
                                                            .addGap(5)
                                                            .addComponent(sendButton,100,GroupLayout.DEFAULT_SIZE,100)
                                            )
                            )
                            .addGap(5)
            )
        }

        //Работа с сокетами
        socket=Socket(host,port)
        communicator=SocketIO(socket)

        communicator.addSocketClosedListener {
            //println("connection lost with server")
            JOptionPane.showMessageDialog(null,"Connection with server lost")
            System.exit(0)
        }

        with(sendButton){
            addMouseListener(object : MouseAdapter(){
                override fun mouseClicked(e : MouseEvent?){
                    super.mouseClicked(e)
                    if(textField.text!="") {
                        send(textField.text)
                        textField.text=""
                    }
                    else JOptionPane.showMessageDialog(null,"You cant send null string")
                }
            })
        }

    }

    /**
     * Старт клиента, для взаимодействия с сервером(и другими клиентами)
     */
    fun start(){
        communicator.startDataReceiving()
        communicator.addDataListener {
            textArea.append(it+"\n")
        }
    }

    /**
     * Функция отправки сообщения на сервер
     */
    fun send(data: String) {
        communicator.sendData(data)
    }
}