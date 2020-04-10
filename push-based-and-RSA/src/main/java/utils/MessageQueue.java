package utils;

import java.util.LinkedList;
import java.util.Queue;

public class MessageQueue<Item> {

    private Queue<Item> queue;
    private int size;

    public MessageQueue(int size) {
        queue = new LinkedList<>();
        this.size = size;
    }

    public synchronized void offer(Item item) throws InterruptedException {
        while (queue.size() == size) wait();
        queue.offer(item);
        if (queue.size() == 1) notifyAll();
    }

    public synchronized Item poll() throws InterruptedException {
        while (queue.size() == 0) wait();
        if (queue.size() == size) notifyAll();
        return queue.poll();
    }
}