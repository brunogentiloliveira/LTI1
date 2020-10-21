import java.util.Scanner;
import jssc.SerialPort;
import jssc.SerialPortException;

public class SerialCommunication{
    public static void main(String[] args) {
        SerialPort serialport;
        serialport = new SerialPort("/dev/cu.usbserial-14230");
        try{
            serialport.openPort();
            serialport.setParams(9600,8,1,0);

            Scanner reader = new Scanner(System.in);

            if (reader.hasNext()){
                String buf = reader.next();
                char[] buf2 = buf.toCharArray();
                serialport.writeBytes(buf.getBytes());
                System.out.println(buf.getBytes() + "<= sent!");
            }
        }catch(SerialPortException ex){
            System.out.println(ex);
        }
    }
}
