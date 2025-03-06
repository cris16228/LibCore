package com.github.cris16228.libcore.http.image_loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.github.cris16228.libcore.Base64Utils;
import com.github.cris16228.libcore.FileUtils;
import com.github.cris16228.libcore.http.image_loader.interfaces.LoadImage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Fresco {

    private static final int THREAD_POOL_SIZE = 4;
    private final Map<WeakReference<ImageView>, String> imageViews = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<Uri, Future<?>> loadingTasks = new HashMap<>();
    private MemoryCache memoryCache;
    private FileCache fileCache;
    private ExecutorService executor;
    private FileUtils fileUtils;
    private Context context;
    private boolean asBitmap = false;
    private Handler handler;
    private String urlPath;
    private final HashMap<String, String> params = new HashMap<>();
    private LoadImage loadImage;
    private int width;
    private int height;


    public static Fresco with(Context context) {
        Fresco loader = new Fresco();
        loader.fileCache = new FileCache(context);
        loader.executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        loader.handler = new Handler(Looper.getMainLooper());
        loader.fileUtils = new FileUtils();
        loader.context = context;
        loader.memoryCache = new MemoryCache(context);
        return loader;
    }

    public Fresco asBitmap() {
        asBitmap = true;
        return this;
    }

    public Fresco load(String url) {
        this.urlPath = url;
        return this;
    }

    public Fresco addEvent(LoadImage loadImage) {
        this.loadImage = loadImage;
        return this;
    }

    public Fresco into(ImageView imageView) {
        imageView.setImageBitmap(null);
        imageView.setImageDrawable(null);
        imageView.setTag(urlPath);
        executor.execute(() -> {
            Bitmap bitmap = memoryCache.get(urlPath);
            handler.post(() -> {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    imageView.invalidate();
                } else {
                    imageViews.put(new WeakReference<>(imageView), urlPath);
                    queuePhoto(urlPath, imageView);
                }
            });
        });
        return this;
    }

    public Bitmap decode() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(urlPath, options);

        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        options.inMutable = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inBitmap = Bitmap.createBitmap(options.outWidth, options.outHeight, Bitmap.Config.RGB_565);

        return BitmapFactory.decodeFile(urlPath, options);
    }

    public Fresco size(@NonNull String size) {
        if (!size.contains("x")) {
            return null;
        }
        String[] sizes = size.split("x");
        if (sizes.length != 2) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(urlPath, options);
        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;

        if (originalWidth <= 0 || originalHeight <= 0) {
            return null;
        }
        if (sizes[0].equals("?") && sizes[1].equals("?")) {
            throw new IllegalArgumentException("You must specify either the width or the height");
        }

        if (sizes[0].equals("?")) {
            height = Integer.parseInt(sizes[1]);
            width = (int) ((height / (float) originalHeight) * originalWidth);
        } else if (sizes[1].equals("?")) {
            width = Integer.parseInt(sizes[0]);
            height = (int) ((width / (float) originalWidth) * originalHeight);
        } else {
            width = Integer.parseInt(sizes[0]);
            height = Integer.parseInt(sizes[1]);
        }
        return this;
    }

    public Fresco addParam(String key, String value) {
        params.put(key, value);
        return this;
    }

    public void queuePhoto(String url, ImageView imageView) {
        PhotoToLoad photoToLoad = new PhotoToLoad(url, imageView);
        executor.submit(new PhotoLoader(photoToLoad));
    }

    private void cancelLoadingTask(Uri uri) {
        Future<?> loadingTask = loadingTasks.get(uri);
        if (loadingTask != null) {
            loadingTask.cancel(true);
            loadingTasks.remove(uri);
        }
    }

    public void cancelAllLoadingTasks() {
        for (Future<?> loadingTask : loadingTasks.values()) {
            loadingTask.cancel(true);
        }
        loadingTasks.clear();
    }

    public Fresco loadFileThumbnail(Uri uri, ImageView imageView, LoadImage loadImage, FileType fileType) {
        cancelLoadingTask(uri);
        try {
            imageView.setImageBitmap(null);
            imageView.setImageDrawable(null);
        } catch (Exception e) {
            Log.d("loadFileThumbnail", e.toString());
        }

        Future<?> loadingTask = executor.submit(() -> {
            File file = fileCache.getFile(uri.getPath());
            Bitmap thumbnail = memoryCache.get(uri.getPath());

            if (thumbnail != null) {
                Log.d("loadFileThumbnail", "Thumbnail found in memory cache for URI: " + uri);
                handler.post(() -> imageView.setImageBitmap(thumbnail));
            } else {
                Log.d("loadFileThumbnail", "Thumbnail not found in memory cache for URI: " + uri);
                imageViews.put(new WeakReference<>(imageView), uri.getPath());
                queuePhoto(uri.getPath(), imageView);
            }
        });
        loadingTasks.put(uri, loadingTask);
        return this;
    }

    public Bitmap getFileThumbnail(Uri uri, FileType fileType) {
        if (fileType == FileType.VIDEO)
            return getVideoThumbnail(uri);
        if (fileType == FileType.IMAGE)
            return getImageThumbnail(uri);
        return null;
    }

    private Bitmap getImageThumbnail(Uri uri) {
        return getImageThumbnail(uri, 25);
    }

    private Bitmap getImageThumbnail(Uri uri, float scalePercent) {
        File file = new File(uri.getPath());
        Bitmap thumbnail;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            Bitmap _image = fileUtils.decodeFile(file);
            if (_image != null)
                return _image;

            Log.e("getFileThumbnail", "URI scheme: " + uri.getScheme());
            if ("content".equals(uri.getScheme())) {
                inputStream = context.getContentResolver().openInputStream(uri);
            } else if ("file".equals(uri.getScheme()) || uri.getScheme() == null) {
                // Handle both "file" scheme and URIs with no scheme (file paths)
                if (file.exists()) {
                    inputStream = Files.newInputStream(file.toPath());
                } else {
                    Log.e("getFileThumbnail", "File does not exist: " + uri.getPath());
                    return null;
                }
            } else {
                Log.e("getFileThumbnail", "Unsupported URI scheme: " + uri.getScheme());
                return null;
            }
            if (inputStream != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                int width = options.outWidth;
                int height = options.outHeight;
                int targetWidth = (int) (width * scalePercent);
                int targetHeight = (int) (height * scalePercent);
                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
                inputStream.close();
                inputStream = context.getContentResolver().openInputStream(uri);
                options.inJustDecodeBounds = false;
                thumbnail = BitmapFactory.decodeStream(inputStream, null, options);
                if (thumbnail != null) {
                    InputStream is = bitmapToInputStream(thumbnail);
                    outputStream = Files.newOutputStream(file.toPath());
                    fileUtils.copyStream(is, outputStream);
                    is.close();
                    outputStream.close();
                    if (targetWidth < 100 && targetHeight < 100)
                        return scaleBitmap(thumbnail, scalePercent);
                } else {
                    Log.e("getFileThumbnail", "Failed to decode bitmap from input stream for URI: " + uri);
                }
            } else {
                Log.e("getFileThumbnail", "Input stream is null for URI: " + uri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileUtils.decodeFile(file);
    }

    private Bitmap scaleBitmap(Bitmap bitmap, float scalePercent) {
        if (bitmap == null) return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Calculate the scaled width and height
        int targetWidth = (int) (width * scalePercent);
        int targetHeight = (int) (height * scalePercent);

        Matrix matrix = new Matrix();
        matrix.postScale(scalePercent, scalePercent);

        return Bitmap.createBitmap(bitmap, 0, 0, targetWidth, targetHeight, matrix, true);
    }


    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private InputStream bitmapToInputStream(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] bitmapArray = outputStream.toByteArray();
        return new ByteArrayInputStream(bitmapArray);
    }

    public Bitmap getVideoThumbnail(Uri videoUri) {
        File file = fileCache.getFile(videoUri.getPath());
        Bitmap thumbnail;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, videoUri);

            Bitmap _image = fileUtils.decodeFile(file);
            if (_image != null)
                return _image;
            thumbnail = retriever.getFrameAtTime(5000000);
            assert thumbnail != null;
            InputStream is = bitmapToInputStream(thumbnail);
            OutputStream os = Files.newOutputStream(file.toPath());
            fileUtils.copyStream(is, os);
            is.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException | IOException e) {
                e.printStackTrace();
            }
        }
        return fileUtils.decodeFile(file);
    }

    private Bitmap getBitmap(String url) {
        File file = fileCache.getFile(url);
        if (file.exists() && file.length() > 0) {
            Bitmap _image = fileUtils.decodeFile(file);
            if (_image != null) {
                return _image;
            }
        }
        File tempFile = new File(file.getAbsolutePath() + ".tmp");
        try {
            Bitmap _webImage;
            URL imageURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) imageURL.openConnection();
            if (!params.isEmpty()) {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Accept-Encoding", "identity");
            InputStream is = new BufferedInputStream(connection.getInputStream());
            OutputStream os = new BufferedOutputStream(new FileOutputStream(tempFile));
            fileUtils.copyStream(is, os);
            /*}*/
            os.flush();
            os.close();
            is.close();
            connection.disconnect();
            if (tempFile.length() > 0) {
                if (!tempFile.renameTo(file)) {
                    tempFile.delete();
                    return null;
                }
            } else {
                tempFile.delete();
                return null;
            }
            _webImage = fileUtils.decodeFile(file);
            return _webImage;
        } catch (OutOfMemoryError outOfMemoryError) {
            /*if (connectionErrors != null)
                connectionErrors.OutOfMemory(memoryCache);
            else*/
            memoryCache.clear();
            return null;
        } catch (FileNotFoundException fileNotFoundException) {
            /*if (connectionErrors != null)
                connectionErrors.FileNotFound(url);*/
            return null;
        } catch (Exception e) {
            tempFile.delete();
            /*if (connectionErrors != null)
                connectionErrors.NormalError();*/
            return null;
        }
    }

    public Bitmap getBitmap(byte[] bytes) {
        Base64Utils.Base64Encoder encoder = new Base64Utils.Base64Encoder();
        File file = fileCache.getFile(encoder.encrypt(Arrays.toString(bytes), Base64.NO_WRAP, null));
        Bitmap _image = fileUtils.decodeFile(file);
        if (_image != null)
            return _image;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    boolean imageViewReused(PhotoToLoad _photoToLoad) {
        String tag = imageViews.get(new WeakReference<>(_photoToLoad.imageView));
        return tag == null || !tag.equals(_photoToLoad.url);
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
        executor.shutdown();
    }

    public enum FileType {
        VIDEO,
        IMAGE,
        OTHER
    }

    static class PhotoToLoad {
        public String url;
        public ImageView imageView;
        public byte[] bytes;

        public PhotoToLoad(String _url, ImageView _imageView) {
            url = _url;
            imageView = _imageView;
        }

        public PhotoToLoad(byte[] _bytes, ImageView _imageView) {
            bytes = _bytes;
            imageView = _imageView;
        }

        public PhotoToLoad(String _url) {
            url = _url;
        }
    }

    class PhotoLoader implements Runnable {

        PhotoToLoad photoToLoad;
        private Bitmap bitmap;

        public PhotoLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
            bitmap = getBitmap(photoToLoad.url);
            if (bitmap != null) {
                memoryCache.put(photoToLoad.url, bitmap);
            }

            Displacer displacer = new Displacer(bitmap, photoToLoad);
            executor.execute(displacer);
        }
    }

    public class Displacer implements Runnable {

        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        LoadImage loadImage;

        public Displacer(Bitmap bitmap, PhotoToLoad photoToLoad) {
            this.bitmap = bitmap;
            this.photoToLoad = photoToLoad;
        }


        @Override
        public void run() {
            handler.post(() -> {
                if (bitmap != null && photoToLoad.imageView != null) {
                    if (photoToLoad.url.equals(photoToLoad.imageView.getTag())) {
                        if (loadImage != null) {
                            loadImage.onSuccess(bitmap);
                        }
                        photoToLoad.imageView.setImageBitmap(bitmap);
                        photoToLoad.imageView.invalidate();
                    }
                } else {
                    if (loadImage != null)
                        loadImage.onFail();
                }
            });
        }
    }
}
