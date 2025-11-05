import java.time.Duration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class LinkedList implements Iterable<String> {
    private Node head;
    public final ReentrantLock headLock = new ReentrantLock();
    public final AtomicBoolean run = new AtomicBoolean(true);
    public final AtomicLong counter = new AtomicLong(0);

    private final ExecutorService exec;

    public LinkedList(int threads) {
        exec = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            exec.submit(this::sort_worker);
        }
        head = new Node("");
    }

    private void sort_worker() {
        Node prev, current1, current2;
        boolean swap = false;
        try {
            while (run.get()) {
                sleep(Duration.ofSeconds(5));
                boolean flag = true;
                while (flag) {
                    prev = head;
                    flag = false;
                    while (true) {
                        swap = false;
                        prev.lock.lock();
                        current1 = prev.next;
                        if (current1 == null) {
                            prev.lock.unlock();
                            break;
                        }
                        current1.lock.lock();
                        current2 = current1.next;
                        if (current2 == null) {
                            prev.lock.unlock();
                            current1.lock.unlock();
                            break;
                        }
                        current2.lock.lock();
                        if (current1.name.compareTo(current2.name) > 0) {
                            swap = true;
                            flag = true;
                            current1.next = current2.next;
                            prev.next = current2;
                            current2.next = current1;
                            counter.incrementAndGet();
                        }

                        prev.lock.unlock();
                        current1.lock.unlock();
                        current2.lock.unlock();
                        if (swap) {
                            sleep(Duration.ofSeconds(1));
                            prev = current2;
                        } else {
                            prev = current1;
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Thread got interrupted: " + e.getMessage());
        }
    }

    public void push(String item) {
        Node node = new Node(item);
        headLock.lock();

        node.next = head.next;
        head.next = node;

        headLock.unlock();
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<>() {
            private Node current;
            {
                head.lock.lock();
                current = head;
                advance();
            }

            private void advance() {
                if (current == null) return;
                Node next = current.next;
                if (next != null) {
                    next.lock.lock();
                }
                current.lock.unlock();
                current = next;
            }

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public String next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                String result = current.name;
                advance();
                return result;
            }
        };
    }
}
