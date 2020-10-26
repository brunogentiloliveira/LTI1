import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import jssc.SerialPortList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;


public class demo {
    public static final String pathTXT_toWrite = "/home/moutinho/Desktop/teste.txt";
    static byte[] buffer;

    public  void connect(String portname) throws IOException {
        String aux = "";

        SerialPort port = new SerialPort(portname);
        try {
            port.openPort();
            port.setParams(

                    SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE

            );

            Files.deleteIfExists(Paths.get(pathTXT_toWrite));
            port.addEventListener((SerialPortEvent event) -> {

                if (event.isRXCHAR()) { //ver event.isRXCHAR () //Definir tempo á espera de receber da serial Port ****

                    try {
                        buffer = port.readBytes();

                        if (!Files.exists(Paths.get(pathTXT_toWrite))) {
                            Files.createFile(Paths.get(pathTXT_toWrite));
                        }
                        Files.write(Paths.get(pathTXT_toWrite), buffer, StandardOpenOption.APPEND);
                        checkFormat();
                        
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
    /*
    Verifica se o conteudo do ficheiro (bytes lidos da serialPort) contem a String PNG
    Podemos fazer o mesmo para os outros formatos
    */
    public void checkFormat() throws IOException {

        byte[] aux = Files.readAllBytes(Paths.get(pathTXT_toWrite));
        String s = new String(aux, StandardCharsets.UTF_8);
        if(s.contains("PNG")){
            System.out.println("PNG FOI ENCONTRADO FILHOTE");
            savePng();
        }else{
            System.out.println("TXT CRIADO FILHOTE");
        }


    }
    /*Faz tudo direito, recebe e cria uma png| CORRIGIR -> lança IOException na leitura dos bytes do txt 
    "Error reading PNG metadata" mas o programa continua já com a png guardada/criada 
    Ta a acrescentar bytes á imagem original e é por isso que lança a IOException
    */
    
    public void savePng() throws IOException { 

        File f = new File("/home/moutinho/Desktop/received.png");
        byte[] aux = Files.readAllBytes(Paths.get(pathTXT_toWrite));
        ByteArrayInputStream in = new ByteArrayInputStream(aux);
        BufferedImage img ;
        img = ImageIO.read(in);
        ImageIO.write(img,"png", f);

    }

    public static void main(String[] args) throws IOException {

        String[] portlist = SerialPortList.getPortNames();

       // System.out.println("Listening Serialport " +portlist[0]+ " :");
        demo obj = new demo();
        obj.connect("/dev/tnt0");


    }

}
