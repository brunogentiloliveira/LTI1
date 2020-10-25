import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

import com.sun.xml.internal.messaging.saaj.util.Base64;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import javax.imageio.ImageIO;
import javax.swing.*;

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
                sendTextFile(formato, portlist[1]);
                break;
            case 2:
                formato = ".png";
                sendImageFile(formato, portlist[1]);
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
