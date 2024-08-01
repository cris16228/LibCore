package com.github.cris16228.libcore;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class QueueUtils {

    private List<Object> queue = new ArrayList<>();

    public void enqueue(Object obj) {
        queue.add(obj);
    }

    public Object dequeue() {
        Object obj = queue.get(0);
        queue.remove(0);
        return obj;
    }

    public void clear() {
        queue.clear();
    }

    public List<Object> getQueue() {
        return queue;
    }

    public void setQueue(List<Object> queue) {
        this.queue = queue;
    }

    @NonNull
    @Override
    public String toString() {
        return "QueueUtils{" +
                "queue=" + queue +
                '}';
    }
}
