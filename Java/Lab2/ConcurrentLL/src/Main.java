import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Main <sorting_threads_amount>");
            System.exit(1);
        }
        int threads = Integer.parseInt(args[0]);
        LinkedList ll = new LinkedList(threads);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            ll.run.set(false);
            System.out.println("Steps performed " + ll.counter.get() + " times.");
        }));
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                String input = br.readLine();

                if (!input.isEmpty()) {
                    while (input.length() > 80) {
                        String data = input.substring(0, 80);
                        input = input.substring(80);
                        ll.push(data);
                    }
                    if (!input.isEmpty()) {
                        ll.push(input);
                    }
                } else {
                    for (String s : ll) {
                        System.out.println(s);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file");
            ll.run.set(false);
            System.exit(1);
        }
    }
}