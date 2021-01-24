import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;

public class AppLayout {

    public static ArrayList<byte[]> dataList = new ArrayList<byte[]>();
    public static String messageToSend;

    public static void makeRecData(byte[] data){
        dataList.add(data);
    }
    public static void processFile() throws IOException {
        String fileName = "/home/moutinho/Desktop/received.txt";
        Files.deleteIfExists(Paths.get(fileName));
        Files.createFile(Paths.get(fileName));
        for(byte[] b: dataList){
            Files.write(Paths.get(fileName),b, StandardOpenOption.APPEND);
        }
        dataList.clear();
    }
    public  void runApp(){
        MySerialPort.setup();
        SerialPort serialPort = MySerialPort.getSerialPort();

        JFrame frame = new JFrame("LTI - Chat");
        frame.setSize(800, 900);    frame.setLocationRelativeTo(null);
        JTextArea ta = new JTextArea(30,60);
        ta.setEditable(false);
        JTextField tf_message = new JTextField();    tf_message.setBounds(0,840,700,20);
        JButton btn_anexo = new JButton("A");   btn_anexo.setBounds(700,840,50,20);
        JButton btn_send = new JButton("->");   btn_send.setBounds(750,840,50,20);
            frame.add(tf_message);
            frame.add(btn_anexo);
            frame.add(btn_send);
            frame.add(new JScrollPane(ta), BorderLayout.CENTER);
            frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        btn_send.addActionListener(e -> {   /* Handler send message */
            messageToSend = tf_message.getText(); tf_message.setText(null);
            ta.append("\nEu: "+messageToSend);
            try {   MySerialPort.writeMessage2(messageToSend);  } catch (IOException ioException) { ioException.printStackTrace(); }
        });

        btn_anexo.addActionListener(e -> {  /* Handler send file */
            try {   MySerialPort.uploadFile();  ta.append("\nFicheiro enviado!");  } catch (IOException ioException) { ioException.printStackTrace(); }
        });


        serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
            @Override
            public void serialEvent(SerialPortEvent event)
            {
                String eof = "w8T", eof2 = "/Wt";
                byte[] newData = new byte[serialPort.bytesAvailable()];
                int numRead = serialPort.readBytes(newData, newData.length);
                if(Arrays.toString(eof.getBytes(StandardCharsets.UTF_8)).equals(Arrays.toString(newData))){
                    for( byte[] b: dataList ){    String s = new String(b, StandardCharsets.US_ASCII); ta.append("PC2: "+s);  }
                    dataList.clear();
                }else if(Arrays.toString(eof2.getBytes(StandardCharsets.UTF_8)).equals(Arrays.toString(newData))){
                    try {   processFile(); ta.append("\nFicheiro recebido com sucesso");  } catch (IOException e) { e.printStackTrace(); }
                }else{
                    makeRecData(newData);
                }
            }
        });



    }

}
