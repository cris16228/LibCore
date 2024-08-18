package com.github.cris16228.libcore.http.image_loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;

import com.github.cris16228.libcore.Base64Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class MemoryCache {

    private final Map<String, Bitmap> cache = Collections.synchronizedMap(new LinkedHashMap<>(10, 1.5f, true));
    private final Context context;
    private long size = 0;
    private long limit = 1000000;

    public MemoryCache(Context context) {
        this.context = context;
        setLimit(Runtime.getRuntime().maxMemory() / 4);
    }

    public Bitmap getBitmap(byte[] bytes) {
        Base64Utils.Base64Encoder encoder = new Base64Utils.Base64Encoder();
        String key = encoder.encrypt(Arrays.toString(bytes), Base64.NO_WRAP, null);
        return get(key);
    }

    public void setLimit(long _limit) {
        limit = _limit;
    }

    public Bitmap get(String id) {
        try {
            if (!cache.containsKey(id))
                return null;
            return cache.get(id);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void put(String id, Bitmap bitmap) {
        try {
            if (cache.containsKey(id))
                size -= sizeInBytes(cache.get(id));
            cache.put(id, bitmap);
            size += sizeInBytes(bitmap);
            checkSize();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private void checkSize() {
        if (size > limit) {
            Iterator<Map.Entry<String, Bitmap>> iterator = cache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Bitmap> entry = iterator.next();
                size -= sizeInBytes(entry.getValue());
                iterator.remove();
                if (size <= limit)
                    break;
            }
        }
    }

    public void clear() {
        try {
            cache.clear();
            size = 0;
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private long sizeInBytes(Bitmap bitmap) {
        if (bitmap == null)
            return 0;
        return bitmap.getByteCount();
    }
}
