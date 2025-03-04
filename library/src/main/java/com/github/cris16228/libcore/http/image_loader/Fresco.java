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

import com.github.cris16228.libcore.Base64Utils;
import com.github.cris16228.libcore.FileUtils;
import com.github.cris16228.libcore.http.image_loader.interfaces.ConnectionErrors;
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
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    private ImageView finalImageView;
    private Handler handler;
    private String url;
    private final HashMap<String, String> params = new HashMap<>();
    private LoadImage loadImage;


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
        this.url = url;
        return this;
    }

    public Fresco addEvent(LoadImage loadImage) {
        this.loadImage = loadImage;
        return this;
    }

    public Fresco into(ImageView imageView) {
        imageView.setImageBitmap(null);
        imageView.setImageDrawable(null);
        Bitmap bitmap = memoryCache.get(url);
        handler.post(() -> {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.invalidate();
            } else {
                imageViews.put(new WeakReference<>(imageView), url);
                queuePhoto(url, imageView);
            }
        });
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
                    fileUtils.copyStream(is, outputStream, is.available());
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

        // Create a matrix for the scaling and translate
        Matrix matrix = new Matrix();
        matrix.postScale(scalePercent, scalePercent);

        // Recreate the new bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, targetWidth, targetHeight, matrix, true);
    }


    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
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
            long randomTimeMicroseconds = (long) (new Random().nextInt(10000) + 5000) * 1000;
            thumbnail = retriever.getFrameAtTime(5000000);
            assert thumbnail != null;
            InputStream is = bitmapToInputStream(thumbnail);
            OutputStream os = Files.newOutputStream(file.toPath());
            fileUtils.copyStream(is, os, is.available());
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
        Bitmap _image = fileUtils.decodeFile(file);
        if (_image != null)
            return _image;
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
            OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
            /*if (downloadProgress != null) {
                fileUtils.copyStream(is, os, connection.getContentLength(), downloadProgress);
            } else {*/
            fileUtils.copyStream(is, os, connection.getContentLength());
            /*}*/
            connection.disconnect();
            os.close();
            is.close();
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
        } catch (IOException ioException) {
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
        String tag = imageViews.get(_photoToLoad.imageView);
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

    public interface DownloadProgress {

        void downloadInProgress(Long progress, long total);

        void downloadComplete();
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

        public List<Object> urls;
        PhotoToLoad photoToLoad;
        ConnectionErrors connectionErrors;
        DownloadProgress downloadProgress;
        private Bitmap bitmap;
        private boolean local;
        private final HashMap<String, String> params;



        public PhotoLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
            params = new HashMap<>();
        }

        @Override
        public void run() {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }
            if (asBitmap) {
                bitmap = getBitmap(photoToLoad.bytes);
                Base64Utils.Base64Encoder encoder = new Base64Utils.Base64Encoder();
                String bytes = encoder.encrypt(Arrays.toString(photoToLoad.bytes), Base64.NO_WRAP, null);
                memoryCache.put(bytes, bitmap);
            } else {
                bitmap = getBitmap(photoToLoad.url);
                if (bitmap != null) {
                    memoryCache.put(photoToLoad.url, bitmap);
                }
            }

        /*if (imageViewReused(photoToLoad))
            return;*/
            Displacer displacer = new Displacer(bitmap, photoToLoad);
            executor.execute(displacer);
            photoToLoad.imageView.invalidate();
        }
    }

    public class Displacer implements Runnable {

        public List<Object> urls;
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        LoadImage loadImage;
        ConnectionErrors connectionErrors;

        public Displacer(Bitmap bitmap, PhotoToLoad photoToLoad) {
            this.bitmap = bitmap;
            this.photoToLoad = photoToLoad;
        }


        @Override
        public void run() {
            handler.post(() -> {
                /*if (imageViewReused(photoToLoad))
                    return;*/
                if (bitmap != null) {
                    if (photoToLoad.imageView != null) {
                        if (loadImage != null)
                            loadImage.onSuccess(bitmap);
                    }
                } else {
                    if (loadImage != null)
                        loadImage.onFail();
                }
            });
        }
    }
}
