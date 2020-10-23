import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class demo { //Recebe Strings da porta serie, enviadas pelo arduino

    static String filePath = "/home/moutinho/Desktop/teste.jpeg";
    static File file = new File(filePath);

    public void connect(String portname) { //le da porta c/ name = 'portname'

        SerialPort port = new SerialPort(portname);
        try {
            port.openPort();

            port.setParams(

                    SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE

            );

            port.addEventListener((SerialPortEvent event)->{

                if(event.isRXCHAR()) { //ver event.isRXCHAR ()

                    try {
                       byte[] str = port.readBytes();
                       // Path path = Paths.get("/home/moutinho/Desktop/teste.jpg");
                       // Files.write(path, bytesFile);
                       // String s = port.readString();


                        OutputStream os = new FileOutputStream(file);
                        os.write(str);
                      //  os.write(b);
                        os.close();


                    } catch (SerialPortException | IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }

            });

        } catch (SerialPortException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        //array strings p/ guardar todas as portas
        String[] portlist = SerialPortList.getPortNames(); //Falta corrigir quando arduino desconectado

        for (String s : portlist) {
            System.out.println(s);
        }

        demo obj = new demo();
        obj.connect(portlist[0]);



    }

}
