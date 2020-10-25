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
import java.nio.file.StandardOpenOption;
import java.util.concurrent.atomic.AtomicInteger;


public class demo {

    static byte[] buffer;

    private static final byte[] PNG_HEADER = new byte[] {
            (byte) 0x89,
            'P', 'N', 'G',
            (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A
    };

    public  void connect(String portname) throws IOException {

        SerialPort port = new SerialPort(portname);
        try {
            port.openPort();
            port.setParams(

                    SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE

            );
            Files.deleteIfExists(Paths.get("/home/moutinho/Desktop/teste.txt"));

            port.addEventListener((SerialPortEvent event) -> {

                if (event.isRXCHAR()) { //ver event.isRXCHAR () //Definir tempo á espera de receber da serial Port ****

                    try {
                        buffer = port.readBytes();


                        if (Files.exists(Paths.get("/home/moutinho/Desktop/teste.txt"))) {
                            Files.write(Paths.get("/home/moutinho/Desktop/teste.txt"), buffer, StandardOpenOption.APPEND);
                        } else {
                            Files.createFile(Paths.get("/home/moutinho/Desktop/teste.txt"));
                            Files.write(Paths.get("/home/moutinho/Desktop/teste.txt"), buffer, StandardOpenOption.APPEND);
                        }

                    } catch (SerialPortException | IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            });

        } catch (SerialPortException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }




    }
    public void savePng() throws IOException { //Faz a conversao corretamente mas tem de se arranjar ainda PARA IMAGENS

        System.out.println("Ficheiro Imagem Recebido");
        File f = new File("/home/moutinho/Desktop/received.png");
        byte[] aux = Files.readAllBytes(Paths.get("/home/moutinho/Desktop/teste.txt"));
        ByteArrayInputStream in = new ByteArrayInputStream(aux);
        BufferedImage img ;
        img = ImageIO.read(in);
        ImageIO.write(img,"png", f);

    }

    public static void main(String[] args) throws IOException {

        String[] portlist = SerialPortList.getPortNames();

        System.out.println("Listening Serialport " +portlist[0]+ " :");
        demo obj = new demo();
        obj.connect(portlist[0]);

    }

}
