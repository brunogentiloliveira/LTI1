import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import jssc.SerialPortList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;


public class demo {

    static byte[] buffer;
    public ArrayList<byte[]> lista;

    public demo(){
        this.lista = new ArrayList<byte[]>();
    }

    public  void connect(String portname){

        SerialPort port = new SerialPort(portname);

        try {
            port.openPort();
            port.setParams(

                    SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE

            );


            port.addEventListener((SerialPortEvent event) -> {
                if (event.isRXCHAR()) { //ver event.isRXCHAR () //Definir tempo á espera de receber da serial Port ****

                    try {
                        buffer = port.readBytes();
                        lista.add(buffer);
                        System.out.println(lista.size());

                       if(lista.size() == 6){ // DEPENDE DO TAMANHO DO FICHEIRO(TAMANHO / 3)
                           saveFILE();
                       }

                    } catch (SerialPortException | IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });

        } catch (SerialPortException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void saveFILE() throws IOException {
            int j = 0;
             ArrayList<Byte> byteList = new ArrayList<Byte>();

            for(byte[] b: lista){
                for (byte value : b) {
                    byteList.add(value);
                }
            }

            byte[] buff = new byte[byteList.size()];
            while(!byteList.isEmpty() && j < byteList.size()){
                buff[j] = byteList.get(j);
                j++;
            }

            String s = new String(buff, StandardCharsets.US_ASCII);

            if(s.contains("PNG")){
                System.out.println("PNG CARALHO");
                Files.deleteIfExists(Paths.get("/home/moutinho/Desktop/received.png"));
                File f = new File("/home/moutinho/Desktop/received.png");
                ByteArrayInputStream in = new ByteArrayInputStream(buff);
                BufferedImage img ;
                img = ImageIO.read(in);
                ImageIO.write(img,"png", f);
                System.out.println("Ficheiro PNG recebido com sucesso");
            }else{
                System.out.println("TXT CARALHO");
                Files.deleteIfExists(Paths.get("/home/moutinho/Desktop/received.txt"));
                Files.createFile(Paths.get("/home/moutinho/Desktop/received.txt"));
                Files.write(Paths.get("/home/moutinho/Desktop/received.txt"),buff);
            }
        System.exit(0);
    }

    public static void main(String[] args){

        String[] portlist = SerialPortList.getPortNames();
        System.out.println("Listening Serialport " +portlist[0]+ " :");
        demo obj = new demo();
        obj.connect(portlist[0]);
    }

}
