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

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;

import com.github.cris16228.libcore.Base64Utils;
import com.github.cris16228.libcore.FileUtils;
import com.github.cris16228.libcore.QueueUtils;
import com.github.cris16228.libcore.StringUtils;
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
import java.util.concurrent.atomic.AtomicReference;

public class ImageLoader {

    private static final int THREAD_POOL_SIZE = 12;
    private final Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<Uri, Future<?>> loadingTasks = new HashMap<>();
    private MemoryCache memoryCache;
    private FileCache fileCache;
    private ExecutorService executor;
    private FileUtils fileUtils;
    private Context context;
    private boolean asBitmap = false;
    private Handler handler;
    private boolean saveInCache;

    public static ImageLoader with(Context context, String path) {
        ImageLoader loader = new ImageLoader();
        loader.init(context, path);
        return loader;
    }

    public static ImageLoader get() {
        return new ImageLoader();
    }

    private void init(Context context, String path) {
        fileCache = new FileCache(path);
        executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        handler = new Handler(Looper.getMainLooper());
        fileUtils = new FileUtils();
        this.context = context;
        memoryCache = new MemoryCache(context, path);
    }

    public void fileCache(Context _context) {
        fileCache = new FileCache(_context);
    }

    public void fileCache(String path) {
        fileCache = new FileCache(path);
    }

    public ImageLoader with(Context _context) {
        fileCache = new FileCache(_context);
        executor = Executors.newFixedThreadPool(3);
        fileUtils = new FileUtils();
        context = _context;
        return this;
    }

    public ImageLoader asBitmap() {
        asBitmap = true;
        return this;
    }

    /*
    * public void load(Bitmap bitmap, ImageView imageView, String path) {
        imageView.setImageBitmap(null);
        imageView.setImageDrawable(null);
        Bitmap bitmapCache = memoryCache.get(path);
        if (bitmapCache != null) {
            imageView.setImageBitmap(bitmapCache);
            imageView.invalidate();
        } else {
            memoryCache.put(path, bitmap, true);
            imageView.setImageBitmap(bitmap);
            imageView.invalidate();
        }
        if (!StringUtils.isEmpty(path)) {
            imageViews.put(imageView, path);
            queuePhoto(path, imageView);
        }
    }*/

    public void load(String url, ImageView imageView, LoadImage loadImage, ConnectionErrors connectionErrors, DownloadProgress downloadProgress) {
        imageView.setImageBitmap(null);
        imageView.setImageDrawable(null);
        executor.execute(() -> {
            Bitmap bitmap = memoryCache.get(url);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.invalidate();
                if (loadImage != null) {
                    loadImage.onSuccess(bitmap);
                }
            } else {
                imageViews.put(imageView, url);
                queuePhoto(url, imageView, loadImage, connectionErrors, downloadProgress);
            }
        });
    }

    public void load(String url, ImageView imageView, LoadImage loadImage, ConnectionErrors connectionErrors, DownloadProgress downloadProgress, HashMap<String, String> params) {
        imageView.setImageBitmap(null);
        imageView.setImageDrawable(null);
        executor.execute(() -> {
            Bitmap bitmap = memoryCache.get(url);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.invalidate();
                if (loadImage != null) {
                    loadImage.onSuccess(bitmap);
                }
            } else {
                imageViews.put(imageView, url);
                queuePhoto(url, imageView, loadImage, connectionErrors, downloadProgress, params);
            }
        });
    }

    public void load(String url, ImageView imageView, LoadImage loadImage, ConnectionErrors connectionErrors, DownloadProgress downloadProgress, boolean saveInCache) {
        this.saveInCache = saveInCache;
        imageView.setImageBitmap(null);
        imageView.setImageDrawable(null);
        executor.execute(() -> {
            Bitmap bitmap = memoryCache.get(url);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.invalidate();
                if (loadImage != null) {
                    loadImage.onSuccess(bitmap);
                }
            } else {
                imageViews.put(imageView, url);
                queuePhoto(url, imageView, loadImage, connectionErrors, downloadProgress);
            }
        });
    }

    public void load(String url, ImageView imageView, LoadImage loadImage, ConnectionErrors connectionErrors, DownloadProgress downloadProgress, HashMap<String, String> params, boolean saveInCache) {
        this.saveInCache = saveInCache;
        imageView.setImageBitmap(null);
        imageView.setImageDrawable(null);
        executor.execute(() -> {
            Bitmap bitmap = memoryCache.get(url);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.invalidate();
                if (loadImage != null) {
                    loadImage.onSuccess(bitmap);
                }
            } else {
                imageViews.put(imageView, url);
                queuePhoto(url, imageView, loadImage, connectionErrors, downloadProgress, params);
            }
        });
    }

    public void download(String url, LoadImage loadImage, ConnectionErrors connectionErrors, DownloadProgress downloadProgress) {
        executor.execute(() -> {
            Bitmap bitmap = memoryCache.get(url);
            if (bitmap == null) {
                queuePhoto(url, loadImage, connectionErrors, downloadProgress);
            }
        });
    }

    public void load(String url, ImageView imageView, LoadImage loadImage, ConnectionErrors connectionErrors, String offlineUrl, DownloadProgress downloadProgress) {
        imageView.setImageBitmap(null);
        imageView.setImageDrawable(null);
        executor.execute(() -> {
            Bitmap bitmap = memoryCache.get(url);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.invalidate();
                if (loadImage != null) {
                    loadImage.onSuccess(bitmap);
                }
            } else {
                imageViews.put(imageView, offlineUrl);
                queuePhoto(url, imageView, loadImage, connectionErrors, downloadProgress);
            }
        });
    }

    public void load(List<Object> urls, ImageView imageView, LoadImage loadImage, ConnectionErrors connectionErrors) {
        QueueUtils queueUtils = new QueueUtils();
        queueUtils.setQueue(urls);
        String url = (String) queueUtils.dequeue();
        imageView.setImageBitmap(null);
        imageView.setImageDrawable(null);
        executor.execute(() -> {
            Bitmap bitmap = memoryCache.get(url);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.invalidate();
                load(urls, imageView, loadImage, connectionErrors);
                if (loadImage != null) {
                    loadImage.onSuccess(bitmap);
                }
            } else {
                imageViews.put(imageView, url);
                queuePhoto(urls, url, imageView, loadImage, connectionErrors);
            }
        });
    }


    public void load(byte[] bytes, ImageView imageView, LoadImage loadImage, ConnectionErrors connectionErrors, DownloadProgress downloadProgress) {
        imageView.setImageBitmap(null);
        imageView.setImageDrawable(null);
        executor.execute(() -> {
            Base64Utils.Base64Encoder encoder = new Base64Utils.Base64Encoder();
            String url = encoder.encrypt(Arrays.toString(bytes), Base64.NO_WRAP, null);
            Bitmap bitmap = memoryCache.get(url);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.invalidate();
                if (loadImage != null) {
                    loadImage.onSuccess(bitmap);
                }
            } else {
                imageViews.put(imageView, url);
                queuePhoto(bytes, imageView, loadImage, connectionErrors, downloadProgress);
            }
        });
    }


    void load(byte[] bytes, ImageView imageView, String path) {
        executor.execute(() -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(null);
            imageView.setImageDrawable(null);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.invalidate();
            }
            if (!StringUtils.isEmpty(path)) {
                imageViews.put(imageView, path);
                queuePhoto(bytes, imageView);
            }
        });
    }

    public void load(byte[] bytes, ImageView imageView) {
        load(bytes, imageView, null);
    }

    public void load(Bitmap bitmap, ImageView imageView, String path, boolean saveInCache) {
        imageView.setImageBitmap(null);
        imageView.setImageDrawable(null);
        AtomicReference<Bitmap> tpm = new AtomicReference<>(bitmap);
        executor.execute(() -> {
            Bitmap bitmapCache = memoryCache.get(path);
            if (bitmapCache == null) {
                memoryCache.put(path, tpm.get());
                bitmapCache = memoryCache.get(path);
                File file = fileCache.getFile(path); // Get the cache file for the URL
                tpm.set(BitmapFactory.decodeFile(path));
                try (FileOutputStream fos = new FileOutputStream(file);
                     BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                    tpm.get().compress(Bitmap.CompressFormat.PNG, 100, bos); // Save as PNG with full quality
                    bos.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            imageView.setImageBitmap(bitmapCache);
            imageView.invalidate();
            if (!StringUtils.isEmpty(path)) {
                imageViews.put(imageView, path);
                queuePhoto(path, imageView);
            }
        });
    }

    public void load(Bitmap bitmap, ImageView imageView, String path) {
        load(bitmap, imageView, path, false);
    }

    public void load(@RawRes @DrawableRes @NonNull Integer resourceId, ImageView imageView) {
        imageView.setImageBitmap(null);
        imageView.setImageDrawable(null);
        executor.execute(() -> {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                imageView.invalidate();
            }
        });
    }

    public void queuePhoto(String url, ImageView imageView, LoadImage loadImage, ConnectionErrors connectionErrors, DownloadProgress downloadProgress) {
        PhotoToLoad photoToLoad = new PhotoToLoad(url, imageView);
        executor.submit(new PhotoLoader(photoToLoad, loadImage, connectionErrors, downloadProgress));
    }

    private void queuePhoto(String url, ImageView imageView, LoadImage loadImage, ConnectionErrors connectionErrors, DownloadProgress downloadProgress, HashMap<String, String> params) {
        PhotoToLoad photoToLoad = new PhotoToLoad(url, imageView);
        executor.submit(new PhotoLoader(photoToLoad, loadImage, connectionErrors, downloadProgress, params));
    }

    public void queuePhoto(byte[] bytes, ImageView imageView, LoadImage loadImage, ConnectionErrors connectionErrors, DownloadProgress downloadProgress) {
        PhotoToLoad photoToLoad = new PhotoToLoad(bytes, imageView);
        executor.submit(new PhotoLoader(photoToLoad, loadImage, connectionErrors, downloadProgress));
    }

    public void queuePhoto(byte[] bytes, ImageView imageView) {
        PhotoToLoad photoToLoad = new PhotoToLoad(bytes, imageView);
        executor.submit(new PhotoLoader(photoToLoad));
    }

    private void queuePhoto(List<Object> urls, String url, ImageView imageView, LoadImage loadImage, ConnectionErrors connectionErrors) {
        PhotoToLoad photoToLoad = new PhotoToLoad(url, imageView);
        executor.submit(new PhotoLoader(urls, photoToLoad, loadImage, connectionErrors));
    }

    private void queuePhoto(String url, LoadImage loadImage, ConnectionErrors connectionErrors, DownloadProgress downloadProgress) {
        PhotoToLoad photoToLoad = new PhotoToLoad(url);
        executor.submit(new PhotoLoader(photoToLoad, loadImage, connectionErrors, downloadProgress));
    }

    public void queuePhoto(String path, ImageView imageView, LoadImage loadImage, FileType fileType) {
        PhotoToLoad photoToLoad = new PhotoToLoad(path, imageView);
        executor.submit(new PhotoLoader(photoToLoad, loadImage, true, fileType));
    }

    public void queuePhoto(String path, ImageView imageView) {
        PhotoToLoad photoToLoad = new PhotoToLoad(path, imageView);
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

    public void loadFileThumbnail(Uri uri, ImageView imageView, LoadImage loadImage, FileType fileType) {
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
                imageViews.put(imageView, uri.getPath());
                queuePhoto(uri.getPath(), imageView, loadImage, fileType);
            }
        });
        loadingTasks.put(uri, loadingTask);
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

        Log.d(getClass().getName(), "file is " + file.exists() + "\n" + file.length());
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

    private Bitmap getBitmap(String url, ConnectionErrors connectionErrors, DownloadProgress downloadProgress, HashMap<String, String> params) {
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
            if (downloadProgress != null) {
                fileUtils.copyStream(is, os, connection.getContentLength(), downloadProgress);
            } else {
                fileUtils.copyStream(is, os, connection.getContentLength());
            }
            connection.disconnect();
            os.close();
            is.close();
            _webImage = fileUtils.decodeFile(file);
            return _webImage;
        } catch (OutOfMemoryError outOfMemoryError) {
            if (connectionErrors != null)
                connectionErrors.OutOfMemory(memoryCache);
            else
                memoryCache.clear();
            return null;
        } catch (FileNotFoundException fileNotFoundException) {
            if (connectionErrors != null)
                connectionErrors.FileNotFound(url);
            return null;
        } catch (IOException ioException) {
            if (connectionErrors != null)
                connectionErrors.NormalError();
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
        LoadImage loadImage;
        ConnectionErrors connectionErrors;
        DownloadProgress downloadProgress;
        private Bitmap bitmap;
        private boolean local;
        private final HashMap<String, String> params;


        PhotoLoader(PhotoToLoad _photoToLoad, LoadImage _loadImage, ConnectionErrors _connectionErrors, DownloadProgress _downloadProgress) {
            photoToLoad = _photoToLoad;
            loadImage = _loadImage;
            connectionErrors = _connectionErrors;
            downloadProgress = _downloadProgress;
            params = new HashMap<>();
        }

        PhotoLoader(PhotoToLoad _photoToLoad, LoadImage _loadImage, ConnectionErrors _connectionErrors, DownloadProgress _downloadProgress, HashMap<String, String> params) {
            photoToLoad = _photoToLoad;
            loadImage = _loadImage;
            connectionErrors = _connectionErrors;
            downloadProgress = _downloadProgress;
            this.params = params;
        }

        PhotoLoader(PhotoToLoad _photoToLoad, LoadImage _loadImage, boolean _local, FileType fileType) {
            photoToLoad = _photoToLoad;
            loadImage = _loadImage;
            local = _local;
            bitmap = getFileThumbnail(Uri.parse(_photoToLoad.url), fileType);
            params = new HashMap<>();
        }

        PhotoLoader(List<Object> _urls, PhotoToLoad _photoToLoad, LoadImage _loadImage, ConnectionErrors _connectionErrors) {
            photoToLoad = _photoToLoad;
            loadImage = _loadImage;
            connectionErrors = _connectionErrors;
            urls = _urls;
            params = new HashMap<>();
        }

        public PhotoLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
            params = new HashMap<>();
        }

        @Override
        public void run() {
            if (asBitmap) {
                bitmap = getBitmap(photoToLoad.bytes);
                Base64Utils.Base64Encoder encoder = new Base64Utils.Base64Encoder();
                String bytes = encoder.encrypt(Arrays.toString(photoToLoad.bytes), Base64.NO_WRAP, null);
                memoryCache.put(bytes, bitmap);
            } else {
                if (local) {
                    if (saveInCache)
                        memoryCache.put(photoToLoad.url, bitmap, local, saveInCache);
                } else {
                    bitmap = getBitmap(photoToLoad.url, connectionErrors, downloadProgress, params);
                    if (saveInCache)
                        memoryCache.put(photoToLoad.url, bitmap, saveInCache);
                }
                if (bitmap != null) {
                    if (saveInCache)
                        memoryCache.put(photoToLoad.url, bitmap, saveInCache);
                }
            }
            /*if (imageViewReused(photoToLoad))
                return;*/
            Displacer displacer;
            if (urls != null && !urls.isEmpty()) {
                displacer = new Displacer(urls, bitmap, photoToLoad, loadImage, connectionErrors);
            } else {
                displacer = new Displacer(bitmap, photoToLoad, loadImage);
            }
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

        public Displacer(Bitmap bitmap, PhotoToLoad photoToLoad, LoadImage _loadImage) {
            this.bitmap = bitmap;
            this.photoToLoad = photoToLoad;
            this.loadImage = _loadImage;
        }

        public Displacer(List<Object> _urls, Bitmap bitmap, PhotoToLoad photoToLoad, LoadImage _loadImage, ConnectionErrors _connectionErrors) {
            this.bitmap = bitmap;
            this.photoToLoad = photoToLoad;
            this.loadImage = _loadImage;
            connectionErrors = _connectionErrors;
            urls = _urls;
        }

        @Override
        public void run() {
            handler.post(() -> {
                /*if (imageViewReused(photoToLoad))
                    return;*/
                if (bitmap != null) {
                    if (photoToLoad.imageView != null) {
                        if (urls != null && !urls.isEmpty()) {
                            load(urls, photoToLoad.imageView, loadImage, connectionErrors);
                        }
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
