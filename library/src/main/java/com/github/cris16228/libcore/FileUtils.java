package com.github.cris16228.libcore;

import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;

import com.github.cris16228.libcore.http.image_loader.ImageLoader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

public class FileUtils {

    private ArrayList<String> folderName;
    private int position;
    private Context context;

    public FileUtils() {
    }

    public static FileUtils with(ArrayList<String> folderName, int position) {
        FileUtils fileUtils = new FileUtils();
        fileUtils.folderName = folderName;
        fileUtils.position = position;
        return fileUtils;
    }

    public static FileUtils with(Context context) {
        FileUtils fileUtils = new FileUtils();
        fileUtils.context = context;
        return fileUtils;
    }

    public boolean isFileValid(String path) {
        File tmp = new File(path);
        return tmp.exists() && tmp.length() > 0;
    }

    public boolean isFileValid(File tmp) {
        return tmp.exists() && tmp.length() > 0;
    }

    public String getPersonalSpace(Context _context) {
        return _context.getExternalFilesDir(null).getAbsolutePath();
    }

    public String getPersonalSpace() {
        return context.getExternalFilesDir(null).getAbsolutePath();
    }

    public Bitmap decodeFile(File file) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, options);
            int width_tmp = options.outWidth, height_tmp = options.outHeight;
            int scale = 1;
            int final_width, final_height;
            final_width = width_tmp /= 2;
            final_height = height_tmp /= 2;
            while (width_tmp / 2 >= final_width && height_tmp / 2 >= final_height) {
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }
            BitmapFactory.Options _options = new BitmapFactory.Options();
            _options.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(file), null, _options);
        } catch (FileNotFoundException exception) {
            return null;
        }
    }

    public void copyStream(InputStream is, OutputStream os, int contentLength, ImageLoader.DownloadProgress downloadProgress) {
        try {
            byte[] data = new byte[33554432];
            int count;
            long progress = 0;
            while ((count = is.read(data, 0, data.length)) != -1) {
                progress += count;
                os.write(data, 0, count);
                if (contentLength > 0) {
                    if (downloadProgress != null) {
                        downloadProgress.downloadInProgress(progress, contentLength);
                    }
                }
            }
            if (downloadProgress != null) {
                downloadProgress.downloadComplete();
            }
            is.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void copyStream(InputStream is, OutputStream os, int contentLength) {
        copyStream(is, os, contentLength, null);
    }

    //Get saved base64 Image
    public Bitmap getBitmap(String base64String) {
        Base64Utils.Base64Decoder decoder = new Base64Utils.Base64Decoder();
        InputStream is = new ByteArrayInputStream(decoder.decryptV2(base64String));
        return BitmapFactory.decodeStream(is);
    }

    //Save image to base64
    public String saveBitmap(Bitmap bitmap) {
        Base64Utils.Base64Encoder encoder = new Base64Utils.Base64Encoder();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return encoder.encrypt(bytes, Base64.DEFAULT);
    }

    public void deleteVideo(Uri uri, ActivityResultLauncher<IntentSenderRequest> deleteResultLauncher) {
        ContentResolver contentResolver = context.getContentResolver();
        try {
            contentResolver.delete(uri, null, null);
        } catch (SecurityException securityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                RecoverableSecurityException recoverableSecurityException = (RecoverableSecurityException) securityException;
                IntentSenderRequest senderRequest = new IntentSenderRequest.Builder(recoverableSecurityException.getUserAction()
                        .getActionIntent().getIntentSender()).build();
                deleteResultLauncher.launch(senderRequest);
            } else {
                File file = new File(uri.getPath());
                if (file.exists())
                    file.delete();
            }
        }
    }

    public String readJson(String file) throws IOException {
        return readFile(file);
    }

    public String readFile(String file) throws IOException {
        File f = new File(file);
        StringBuilder text = new StringBuilder();
        if (!f.exists()) {
            Log.e("readFiles", "The file doesn't exists");
            return "";
        }
        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                text.append(line);
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            if (e instanceof FileNotFoundException)
                throw new IOException("File " + file + " not found!");
            return "";
        }
        return text.toString();
    }

    public File getNewestFile(String directoryPath) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles(File::isFile);
        long lastModifiedTime = Long.MIN_VALUE;
        File fileChosen = null;
        if (files != null) {
            for (File file : files) {
                if (file.lastModified() > lastModifiedTime) {
                    fileChosen = file;
                    lastModifiedTime = file.lastModified();

                }
            }
        }
        return fileChosen;
    }

    public String binaryFileToString(String path, boolean print) throws IOException {
        File file = new File(path);
        String text = "";
        if (!file.exists()) return "";
        if (!StringUtils.isEmpty(StringUtils.binaryToString(readFile(file.getAbsolutePath()), print))) {
            text = StringUtils.binaryToString(readFile(file.getAbsolutePath()), print);
        }
        return text;
    }

    public void writeFile(String file, String text) {
        File f = new File(file);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(f);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.append(text);
            osw.flush();
            osw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFile(String file, String text, boolean lineSeparator) {
        File f = new File(file);
        if (!f.exists()) {
            try {
                new File(f.getParent()).mkdirs();
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            String separator = System.getProperty("line.separator");
            FileOutputStream fos = new FileOutputStream(f, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.append(text);
            if (lineSeparator)
                osw.append(separator);
            osw.flush();
            osw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void debugLog(String text) {
        String path = getPersonalSpace(context) + "/debug.log";
        Log(path, text);
    }

    public void debugLog(String text, String fileName) {
        String path = getPersonalSpace(context) + fileName;
        Log(path, text);
    }

    public void debugLog(String text, String fileName, String path) {
        path += fileName;
        Log(path, text);
    }

    public void Log(String path, String text) {
        writeFile(path, "[" + new DateTimeUtils().getDateTime(new Date().getTime(), null) + "]: " + text, true);
    }

    public void writeJson(String file, String json) {
        writeFile(file, json);
    }

    public String getFileName(File file) {
        return file.getName();
    }
}
