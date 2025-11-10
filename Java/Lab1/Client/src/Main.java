import java.net.InetAddress;
import java.net.Socket;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java Main <ip> <port> <delay>");
            System.exit(1);
        }
        int delay = Integer.parseInt(args[0]);
        try (Socket clientSocket = new)
    }
}