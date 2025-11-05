// Main.java (adapted for library implementation with Collections.synchronizedList)
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class LibraryImpl {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Main <sorting_threads_amount>");
            System.exit(1);
        }
        int threads = Integer.parseInt(args[0]);
        List<String> ll = Collections.synchronizedList(new ArrayList<>());
        AtomicBoolean run = new AtomicBoolean(true);
        AtomicLong stepCount = new AtomicLong(0);
        ExecutorService exec = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            exec.submit(() -> sort_worker(ll, run, stepCount));
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            run.set(false);
            System.out.println("Total steps (library): " + stepCount.get());
        }));
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                String input = br.readLine();
                if (input == null) break;
                if (!input.isEmpty()) {
                    while (input.length() > 80) {
                        String data = input.substring(0, 80);
                        input = input.substring(80);
                        ll.addFirst(data);
                    }
                    if (!input.isEmpty()) {
                        ll.addFirst(input);
                    }
                } else {
                    synchronized (ll) {
                        for (String s : ll) {
                            System.out.println(s);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file");
            run.set(false);
            System.exit(1);
        }
    }

    private static void sort_worker(List<String> list, AtomicBoolean run, AtomicLong stepCount) {
        boolean swap = false;
        try {
            while (run.get()) {
                Thread.sleep(Duration.ofSeconds(5));
                boolean flag = true;
                while (flag) {
                    int i = 0;
                    flag = false;
                    while (true) {
                        boolean done = false;
                        synchronized (list) {
                            if (i >= list.size() - 1) {
                                done = true;
                            } else if (list.get(i).compareTo(list.get(i + 1)) > 0) {
                                swap = true;
                                flag = true;
                                Collections.swap(list, i, i + 1);
                                stepCount.incrementAndGet();
                            }
                        }
                        if (done) break;

                        if (swap) {
                            Thread.sleep(Duration.ofSeconds(1));
                        }
                        i++;
                    }
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Thread got interrupted: " + e.getMessage());
        }
    }
}
