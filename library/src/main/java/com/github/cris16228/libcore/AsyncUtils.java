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

    public <T> void execute(onExecuteListener<T> _onExecuteListener) throws InterruptedException {
        _onExecuteListener.preExecute();
        executor.execute(() -> {
            T result = _onExecuteListener.doInBackground();
            handler.post(() -> {
                _onExecuteListener.postDelayed();
                if (onUIUpdate != null) {
                    onUIUpdate.updateUI();
                }
            });
        });
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
