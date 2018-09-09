package ru.johnlife.lifetools.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QueuedTask extends Task {
    private final static ExecutorService queue = Executors.newSingleThreadExecutor();
    private Runnable action;

    public QueuedTask(Runnable action) {
        this.action = action;
    }

    @Override
    protected void doInBackground() {
        action.run();
    }

    @Override
    public void execute() {
        enqueue();
    }

    public void enqueue() {
        executeOnExecutor(queue, (Void)null);
    }
}
