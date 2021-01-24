import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ChatApp {
    public static ArrayList<byte[]> dataList = new ArrayList<byte[]>();
    public static void main(String[] args) throws InterruptedException, IOException {
        int i =0;   String messageAux = null;
        Scanner inF = new Scanner(System.in);
        AppLayout app = new AppLayout();
        app.runApp();
    }

}
