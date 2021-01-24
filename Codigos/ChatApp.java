import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ChatApp {
    public static ArrayList<byte[]> dataList = new ArrayList<byte[]>();
    public static void makeRecData(byte[] data){
        //System.out.println("Adding...");
        dataList.add(data);
    }
    public static void proccessMessage(){
        System.out.print("\nPc2: ");
        for(byte[] b: dataList){
            String s = new String(b, StandardCharsets.US_ASCII);
            System.out.print(s);
        }
        System.out.println(" ");
        dataList.clear();
    }
    public static void processFile() throws IOException {
        System.out.println("\nFicheiro recebido com sucesso");
        String fileName = "/home/moutinho/Desktop/received.txt";
        Files.deleteIfExists(Paths.get(fileName));
        Files.createFile(Paths.get(fileName));
        for(byte[] b: dataList){
            Files.write(Paths.get(fileName),b, StandardOpenOption.APPEND);
        }
        dataList.clear();
    }
    public static void main(String[] args) throws InterruptedException, IOException {
        int i =0;
        Scanner inF = new Scanner(System.in);

        // MySerialPort.setRunning(true);

        MySerialPort.setup();
        SerialPort serialPort = MySerialPort.getSerialPort();
        System.out.println("\nEscreva a sua mensagem ('1' para transferir ficheiros)");
        while(true){
            Thread.sleep(20);


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
                        proccessMessage();
                    }else if(Arrays.toString(eof2.getBytes(StandardCharsets.UTF_8)).equals(Arrays.toString(newData))){
                        try {   processFile();   } catch (IOException e) { e.printStackTrace(); }
                    }else{
                        makeRecData(newData);
                    }


                }
            });

            if(MySerialPort.hasConsoleMessage()){ MySerialPort.writeMessage(); }


        }
    }

}
