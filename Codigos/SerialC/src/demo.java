import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import jssc.SerialPortList;

import javax.swing.text.html.parser.Parser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Scanner;

public class demo { //Recebe Strings da porta serie, enviadas pelo arduino

    static byte[] buffer = new byte[49];

    public  void connect(String portname) { //le da porta c/ name = 'portname'


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

                if(event.isRXCHAR()) { //ver event.isRXCHAR () //Definir tempo á espera de receber da serial Port ****

                        try {

                            buffer = port.readBytes();

                            String s = new String(buffer, StandardCharsets.UTF_8);
                        
                            System.out.print(s);
                            Files.deleteIfExists(Paths.get("/home/moutinho/Desktop/teste.txt")); //Falta so meter isto mais bonito e declarar uma variavel para o PATH
                            Files.createFile(Paths.get("/home/moutinho/Desktop/teste.txt"));
                            Files.write(Paths.get("/home/moutinho/Desktop/teste.txt"), s.getBytes(),StandardOpenOption.APPEND);




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


    public static void main(String[] args) throws IOException {

        //array strings p/ guardar todas as portas
        String[] portlist = SerialPortList.getPortNames();

        for (String s : portlist) {
            System.out.println(s);
        }

        demo obj = new demo();
        obj.connect("/dev/tnt0");




    }

}

