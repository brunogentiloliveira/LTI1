import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.w3c.dom.Text;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.metal.MetalBorders;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chat {
    public static SerialPort serialPort;
    public static JFrame chatFrame; public static JTextArea ta; public static JTextField tf_message;
    public static JLabel btn_anexo; public static JLabel btn_send;
    public static String userDirectory = Paths.get("").toAbsolutePath().toString();
    public static boolean fileReceiving = false;
    public static ArrayList<byte[]> listaFile = new ArrayList<>();
    public static  int x = 0;

    public void start(){
        configureSerialPort();
        getChatUI();
        chekcSerialPort();
    }
    public void configureSerialPort(){
        serialPort = SerialPort.getCommPorts()[0];
        serialPort.setComPortParameters(115200, 8, 1,0);
        serialPort.openPort();
        System.out.println("Selected serial port: "+serialPort.getSystemPortName());
    }
    public void getChatUI(){

        ImageIcon imageIcon = new ImageIcon("/home/moutinho/Transferências/add.png"); Image image = imageIcon.getImage(); // transform it
        Image newimg = image.getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH); imageIcon = new ImageIcon(newimg);  // transform it back

        ImageIcon imageIcon2 = new ImageIcon("/home/moutinho/Transferências/send.png"); Image imageX = imageIcon2.getImage(); // transform it
        Image newimg2 = imageX.getScaledInstance(25, 25,  java.awt.Image.SCALE_SMOOTH); imageIcon2 = new ImageIcon(newimg2);  // transform it back


        chatFrame = new JFrame("Chat-Grupo4-"+serialPort.getSystemPortName());  chatFrame.setSize(800, 490);
        chatFrame.setLocationRelativeTo(null);
        JPanel panelAux = new JPanel(); panelAux.setBounds(0,410,800,40);        ta = new JTextArea(30,60);  ta.setEditable(false);   ta.setBackground(UIManager.getColor(Color.darkGray));  ta.setFont(new Font("LucidaSans", Font.PLAIN, 20));
            tf_message = new JTextField();         tf_message.setPreferredSize(new Dimension(650,30)); tf_message.setFont(new Font("LucidaSans", Font.PLAIN, 20));
            btn_anexo = new JLabel(imageIcon);     btn_anexo.setPreferredSize(new Dimension(30,30));
            btn_send = new JLabel(imageIcon2);     btn_send.setPreferredSize(new Dimension(50,30));
        panelAux.add(btn_anexo, BorderLayout.BEFORE_LINE_BEGINS);  panelAux.add(tf_message, BorderLayout.CENTER);   panelAux.add(btn_send, BorderLayout.LINE_END);
        chatFrame.add(panelAux, BorderLayout.AFTER_LAST_LINE); chatFrame.getContentPane().add(ta);
        chatFrame.setVisible(true); chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        /***** Handler btn anexar *****/
        btn_anexo.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                try {   sendFile();     } catch (IOException ioException) { System.out.println("Ficheiro Inexistente..."); }
            }
        });


        /***** Handler btn send *****/
        btn_send.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                String msgTsend = tf_message.getText(); tf_message.setText(null);
                ta.append("\nEu: "+msgTsend);
                sendMessage(msgTsend);
            }
        });

    }

    public void chekcSerialPort(){

        serialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }
            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)  return;
                byte[] newData = new byte[serialPort.bytesAvailable()];
                int numRead = serialPort.readBytes(newData, newData.length);

                if(receivingFile(newData) || fileReceiving || Arrays.toString(newData).contains("-119, 80, 78,")){
                    System.out.println("FILE: Read " + numRead + " bytes."+ new String(newData, StandardCharsets.US_ASCII));
                    fileReceiving=true;
                    listaFile.add(newData);
                    if(isEndOfFile(newData)) { fileReceiving=false;
                        try { saveReceivedFile(); } catch (IOException e) { e.printStackTrace(); }
                    }  }

                else if(fileReceiving == false){
                    System.out.println("Read "+new String(newData, StandardCharsets.US_ASCII));
                    String mensagemRecebida = new String(newData, StandardCharsets.US_ASCII);
                    ta.append("\nPC2: "+mensagemRecebida.substring(13,mensagemRecebida.length()-1));
                }
            }
        });
    }  /** Verificar se existem dados na Serial Port **/
    public void sendMessage(String messageToSend){
        int bytesEscritos = 0;  String beginMsg = "LTI-MIETI-MSG"; byte[] startMessage = beginMsg.getBytes(StandardCharsets.US_ASCII);
        byte[] bytesToSend = messageToSend.getBytes(StandardCharsets.US_ASCII);
        serialPort.writeBytes(startMessage, startMessage.length );
        while(bytesEscritos < bytesToSend.length){
            bytesEscritos += serialPort.writeBytes(bytesToSend, bytesToSend.length, bytesEscritos);
        }
        System.out.println("Written "+bytesEscritos+" bytes to serial port");
    }
    public String selectFileToSend(){
        FileDialog fd = new FileDialog(chatFrame, "Select File");
        fd.setMode(FileDialog.LOAD);
        fd.pack();
        fd.setMultipleMode(false);
        fd.setLocationRelativeTo(null);
        fd.setVisible(true);
        System.out.println(fd.getDirectory());

        return fd.getDirectory()+fd.getFile();
    }
    public void sendFile() throws IOException {
        int bytesEscritos = 0;  String beginFile = "LTI-MIETI-File", endFile = "Ends";
        byte[] delimiter = beginFile.getBytes(StandardCharsets.US_ASCII);
        String fileToSend = selectFileToSend();

        if( fileToSend.contains(".png")){
               File file = new File(fileToSend);    FileInputStream fis = new FileInputStream(file);
               ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                try {
                    for (int readNum; (readNum = fis.read(buf)) != -1;) {
                        //Writes to this byte array output stream
                        bos.write(buf, 0, readNum);
                        System.out.println("read " + readNum + " bytes,");
                    }
                } catch (IOException ex) {
                    System.out.println("Exception occured :" + ex.getMessage());
                }
                byte [] bytesToSend = bos.toByteArray();
                System.out.println(Arrays.toString(bytesToSend));
                serialPort.writeBytes(bytesToSend, bytesToSend.length);

        }else{
            byte[] bytesToSend = Files.readAllBytes(Path.of(fileToSend));
            serialPort.writeBytes(delimiter, delimiter.length);
            while (bytesEscritos < bytesToSend.length){
                bytesEscritos += serialPort.writeBytes(bytesToSend, bytesToSend.length, bytesEscritos);
            }
            delimiter = endFile.getBytes(StandardCharsets.US_ASCII);
            serialPort.writeBytes(delimiter, delimiter.length);
        }

        ta.append("\nFicheiro enviado com sucesso");
    }
    public boolean receivingFile(byte[] dataReceived){
        return new String(dataReceived, StandardCharsets.US_ASCII).contains("LTI-MIETI-File");
    }
    public boolean isEndOfFile(byte[] dataReceived){
        return new String(dataReceived, StandardCharsets.US_ASCII).contains("Ends");
    }
    public void saveReceivedFile() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();   x++;
        for(byte[] b: listaFile){
            baos.write(b);
        }


        String filePath = null, s = baos.toString(StandardCharsets.US_ASCII);
        s = s.replaceAll("\00", "");
        s = s.replace("LTI-MIETI-File", "");
        s = s.replace("Ends", "");

        String formato = "";
        if(s.contains("PNG")){
            formato = "png";
        }else if (s.contains("<?xml version=")){
            formato = "iml";
        }else if (s.contains(".pdf")){
            formato = "pdf";
        }else{
            formato = "txt";
        }
        filePath = userDirectory+"/received"+x+"."+formato;
        ta.append("\nFicheiro recebido!");

        Files.deleteIfExists(Paths.get(filePath));
        Files.createFile(Paths.get(filePath));
        Files.write(Paths.get(filePath), s.getBytes());
        listaFile.clear();

    }
}