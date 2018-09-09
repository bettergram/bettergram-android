package ru.johnlife.lifetools.task;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

/**
 * Created by yanyu on 5/11/2016.
 */
public abstract class Task extends AsyncTask<Void, Void, Void> {
    protected abstract void doInBackground();

    @Override
    protected Void doInBackground(Void... params) {
        doInBackground();
        return null;
    }

    public void execute() {
        executeOnExecutor(THREAD_POOL_EXECUTOR, (Void)null);
    }

    public boolean isRunning() {
        return getStatus() == Status.RUNNING;
    }

    @SuppressLint("StaticFieldLeak")
    public static void run(Runnable action) {
        new Task(){
            @Override
            protected void doInBackground() {
                action.run();
            }
        }.execute();
    }
}
