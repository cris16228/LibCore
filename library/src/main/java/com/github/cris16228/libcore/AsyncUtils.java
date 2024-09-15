package com.github.cris16228.libcore;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncUtils {

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());

    private onExecuteListener onExecuteListener;
    private onUIUpdate onUIUpdate;

    public AsyncUtils(ExecutorService executor, Handler handler) {
        this.executor = executor;
        this.handler = handler;
    }

    public AsyncUtils() {
    }

    public static AsyncUtils get() {
        return new AsyncUtils();
    }

    public void updateUIBackground(Activity activity) {
        activity.runOnUiThread(() -> {
            onUIUpdate.updateUI();
        });
    }

    public void onExecuteListener(onExecuteListener _onExecuteListener) {
        onExecuteListener = _onExecuteListener;
    }

    public void onExecuteListener(onExecuteListener _onExecuteListener, onUIUpdate _onUIUpdate) {
        onExecuteListener = _onExecuteListener;
        onUIUpdate = _onUIUpdate;
    }

    public void onUIUpdate(onUIUpdate _onUIUpdate) {
        onUIUpdate = _onUIUpdate;
    }

    public <T> T execute(onExecuteListener _onExecuteListener) throws InterruptedException {
        final Object lock = new Object();
        final T[] result = (T[]) new Object[1];
        _onExecuteListener.preExecute();
        executor.execute(() -> {
            result[0] = (T) _onExecuteListener.doInBackground();

            synchronized (lock) {
                lock.notifyAll();
            }

            handler.post(() -> {
                _onExecuteListener.postDelayed();
                if (onUIUpdate != null) onUIUpdate.updateUI();
            });
        });
        synchronized (lock) {
            lock.wait();
        }
        return result[0];
    }

    public interface onExecuteListener<T> {

        void preExecute();

        T doInBackground();

        void postDelayed();
    }

    public interface onUIUpdate {

        void updateUI();
    }
}
