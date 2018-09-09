package ru.johnlife.lifetools.task;

import java.util.ArrayList;
import java.util.List;

public class Queue {
    private final List<Runnable> queue = new ArrayList<>();
    private boolean running = false;

    public void add(Runnable action) {
        synchronized (queue) {
            queue.add(action);
        }
        if (!running) {
            runNext();
        }
    }

    private void runNext() {
        Runnable action;
        synchronized (queue) {
            if (queue.isEmpty()) {
                running = false;
                return;
            }
            action = queue.remove(0);
        }
        running = true;
        action.run();
        runNext();
    }
}
