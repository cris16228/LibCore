package com.github.cris16228.libcore.http.image_loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;

import com.github.cris16228.libcore.Base64Utils;
import com.github.cris16228.libcore.StringUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.Arrays;

public class MemoryCache {

    /*private final Map<String, Bitmap> cache = Collections.synchronizedMap(new LinkedHashMap<>(10, 1.5f, true));*/
    private final LruCache<String, Bitmap> cache;
    private final Context context;
    private long size = 0;
    private long limit = 1000000;
    private final String path;

    public MemoryCache(Context context) {
        this.context = context;
        this.path = context.getCacheDir() + "/fresco";
        setLimit(Runtime.getRuntime().maxMemory() / 4);
        this.cache = new LruCache<String, Bitmap>((int) limit) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    public Bitmap getBitmap(byte[] bytes) {
        Base64Utils.Base64Encoder encoder = new Base64Utils.Base64Encoder();
        String key = encoder.encrypt(Arrays.toString(bytes), Base64.NO_WRAP, null);
        return get(key);
    }

    public void setLimit(long _limit) {
        limit = _limit;
    }

    public synchronized Bitmap get(String id) {
        if (StringUtils.isEmpty(path)) return null;
        try {
            id = URLEncoder.encode(id, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        id = new File(path, id).getAbsolutePath();
        try {
            if (cache.get(id) != null)
                return cache.get(id);
            File file = new File(id);
            if (file.exists()) {
                return decodeSampleFromFile(file.getAbsolutePath(), 200, 200);
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
        return null;
    }

    private Bitmap decodeSampleFromFile(String path, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public synchronized void put(String id, Bitmap bitmap, boolean isLocal, boolean saveInCache) {
        try {
            if (isLocal) {
                File file = new File(id);
                if (bitmap.getByteCount() > 0) {
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, Files.newOutputStream(file.toPath()));
                    }
                } else {
                    Log.e("MemoryCache", "Bitmap is null");
                }
            }
            if (cache.get(id) != null)
                size -= sizeInBytes(cache.get(id));
            cache.put(id, bitmap);
            size += sizeInBytes(bitmap);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void put(String id, Bitmap bitmap) {
        put(id, bitmap, false, true);
    }

    public void put(String id, Bitmap bitmap, boolean saveInCache) {
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeFile(id);
        }
        put(id, bitmap, false, saveInCache);
    }

    public String getPath() {
        return path;
    }

    public void clear() {
        try {
            cache.evictAll();
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
