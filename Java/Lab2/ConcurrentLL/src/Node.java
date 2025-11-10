import java.util.concurrent.locks.ReentrantLock;

public class Node {
    public final String name;
    public Node next;
    public final ReentrantLock lock;

    public Node(String name) {
        this.name = name;
        this.lock = new ReentrantLock();
    }

    public void putNext(Node next) {
        next.next = this.next;
        this.next = next;
    }
}
