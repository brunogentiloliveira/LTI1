import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Scanner;

public class MySerialPort {
    private static boolean running=false;
    public static Scanner in = new Scanner(System.in);
    public static SerialPort serialPort = null;
    private static byte[] receivingPack;
    private static byte[] sendingPack;

    public static SerialPort getSerialPort(){
        return serialPort;
    }
    public static boolean isRunning(){
        return running;
    }
    public static void setRunning(boolean run){
        receivingPack= new byte[32];
        sendingPack= new byte[32];
        running = run;
    }
    public static void setup(){
        serialPort = SerialPort.getCommPorts()[0];
            System.out.println("Selected serial port: "+serialPort.getSystemPortName());
            setRunning(true);
            serialPort.setComPortParameters(115200, 8, 1,0);
        serialPort.openPort();
    }
    public static boolean hasConsoleMessage(){
        return in.hasNext();
    }
    public static void uploadFile() throws IOException {
        String filename = "/home/moutinho/Desktop/teste1.txt";
        byte[] buff = Files.readAllBytes(Path.of(filename));
        (new Thread(new portWritter(buff, buff.length, "/Wt"))).start();
    }
    public static void writeMessage2(String message) throws IOException {
            sendingPack = message.getBytes(StandardCharsets.UTF_8);
            (new Thread(new portWritter(sendingPack, message.length(), "w8T"))).start();
    }
    public static class portWritter implements Runnable {
        byte[] buffer;
        int size;
        String writeType;
        public portWritter ( byte[] buffer, int sz, String operation ) {
            this.buffer = buffer;
            this.size = sz;
            this.writeType = operation;
        }
        public void run () {
            sendData(buffer, size, writeType);
        }
    }
    public static void sendData(byte[] buffer, int SIZE, String type){
        int escritos = 0;   String eof = type;
        while (isRunning()){
            if(escritos >= SIZE ){ serialPort.writeBytes(eof.getBytes(StandardCharsets.UTF_8), eof.getBytes(StandardCharsets.UTF_8).length); break;}
            escritos += serialPort.writeBytes(buffer, SIZE,0);
            //System.out.println("Sending"+ Arrays.toString(buffer)+ " Escritos: "+escritos);
            try { Thread.sleep(200); } catch (Exception e) { e.printStackTrace(); }

        }
    }

}