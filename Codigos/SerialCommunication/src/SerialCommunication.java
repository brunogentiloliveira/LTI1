import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import javax.imageio.ImageIO;


public class SerialCommunication{
    public static void main(String[] args) throws SerialPortException, IOException {
        String formato = "";
        String[] portlist = SerialPortList.getPortNames();

        System.out.println("Selecione o tipo de ficheiro a enviar: \n 0 - Sair \n 1 - Ficheiro de Texto(.txt) \n 2 - Ficheiro de Imagem(.jpg)");
        System.out.print("Opção escolhida: ");
        Scanner userInput = new Scanner(System.in);
        int choice = userInput.nextInt();

        switch (choice){
            case 0:
                System.exit(0);
                break;
            case 1:
                formato = ".txt";
                sendTextFile(formato, "/dev/ttyUSB1");
              //  sendTextFile(formato, "/dev/tnt1");
                break;
            case 2:
                formato = ".png";
                sendImageFile(formato, "/dev/ttyUSB1");
               // sendImageFile(formato, "/dev/tnt1");
                break;
        }

    }
    public static void sendTextFile(String formato, String portname) throws SerialPortException, IOException {
        SerialPort serialPort = new SerialPort(portname);
        serialPort.openPort();
        serialPort.setParams(9600,8,1,0);

        byte[] buff = Files.readAllBytes(Paths.get("/home/moutinho/Desktop/teste1.txt"));
        serialPort.writeBytes(buff);

        System.out.println("Ficheiro enviado");
    }
    public static void sendImageFile(String formato, String portname) throws SerialPortException, IOException {
        SerialPort serialPort = new SerialPort(portname);
        serialPort.openPort();
        serialPort.setParams(9600,8,1,0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedImage img;
        img = ImageIO.read(new File("/home/moutinho/Desktop/imgToSend.png"));
        ImageIO.write(img, "png", baos);
        byte[] buff = baos.toByteArray();
        serialPort.writeBytes(buff);

    }
}
