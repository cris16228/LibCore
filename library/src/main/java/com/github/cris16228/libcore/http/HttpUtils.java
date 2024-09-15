package com.github.cris16228.libcore.http;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.cris16228.libcore.StringUtils;
import com.github.cris16228.libcore.deviceutils.DeviceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpUtils {

    String url = "";
    String lineEnd = "\r\n";
    String twoHyphens = "--";
    String boundary = "*****";
    String TAG = getClass().getSimpleName();
    private StringBuilder result;
    private int readTimeout = 10000;
    private int connectionTimeout = 15000;
    private HttpURLConnection conn;
    private DataOutputStream dos;
    private JSONObject jsonObject;
    private boolean debug;

    public static HttpUtils get() {
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.debug = false;
        return httpUtils;
    }

    public static HttpUtils get(boolean debug) {
        HttpUtils httpUtils = new HttpUtils();
        httpUtils.debug = debug;
        return httpUtils;
    }

    private List<String> cookies = new ArrayList<>();

    public String get(String urlString, Map<String, String> headers) {
        HttpURLConnection urlConnection;
        StringBuilder sb = new StringBuilder();
        String jsonString = null;
        URL url = null;
        try {
            url = new URL(urlString);
            System.out.println(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            if (url != null) {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setInstanceFollowRedirects(true);
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestProperty("Accept", "*/*");
                urlConnection.setRequestProperty("Connection", "Keep-Alive");
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
                if (headers != null && !headers.isEmpty()) {
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }
                String contentType = urlConnection.getHeaderField("Content-Type");
                if (contentType != null) {
                    String htmlContent;
                    String title = url.toString().substring(url.toString().lastIndexOf("/") + 1);
                    if (contentType.startsWith("image/")) {
                        try {
                            htmlContent = "<html><head><meta property=\"og:image\" content=\"" + urlString + "\"><title>" + title + "</title></head><body><img src=\"" + urlString + "\" /></body" +
                                    "></html>";
                        } catch (Exception e) {
                            e.printStackTrace();
                            htmlContent = "<html><head><title>" + title + "</title></head><body><p>Error loading image.<p/></body></html>";
                        }
                        sb.append(htmlContent);
                    }
                    if (contentType.startsWith("application/xml")) {
                        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(urlString));
                        if (mimeType != null) {
                            if (mimeType.startsWith("image/")) {
                                try {
                                    htmlContent = "<html><head><meta property=\"og:image\" content=\"" + urlString + "\"><title>" + title + "</title></head><body><img src=\"" + urlString + "\" /></body" +
                                            "></html>";
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    htmlContent = "<html><head><title>" + title + "</title></head><body><p>Error loading image.<p/></body></html>";
                                }
                                sb.append(htmlContent);
                            }
                        } else if (mimeType.startsWith("video/")) {
                            try {
                                htmlContent = "<html><head><meta property=\"og:image\" content=\"" + urlString + "\"><title>" + title + "</title></head><body><video src=\"" + urlString + "\" " +
                                        "type=\"video/" + MimeTypeMap.getFileExtensionFromUrl(urlString) + "\" /></body></html>";
                            } catch (Exception e) {
                                e.printStackTrace();
                                htmlContent = "<html><head><title>" + title + "</title></head><body><p>Error loading image.<p/></body></html>";
                            }
                            sb.append(htmlContent);
                        }
                    }
                    if (contentType.startsWith("video/")) {
                        try {
                            String extension = urlString.substring(urlString.lastIndexOf("."));
                            htmlContent = "<html><head><title>" + title + "</title></head><body><video src=\"" + urlString + "\" type=\"video/" + extension + "\" /></body></html>";
                        } catch (Exception e) {
                            e.printStackTrace();
                            htmlContent = "<html><head><title>" + title + "</title></head><body><p>Error loading video.<p/></body></html>";
                        }
                        sb.append(htmlContent);
                    } else if (contentType.startsWith("text/html")) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        br.close();
                    } else {
                        title = "Error";
                        htmlContent = "<html><head><title>" + title + "</title></head><body><p>Error loading url.<p/></body></html>";
                        sb.append(htmlContent);
                    }
                }
                jsonString = sb.toString();
                if (DeviceUtils.isEmulator())
                    System.out.println("JSON: " + jsonString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    public String get(String url) {
        return get(url, null);
    }

    public boolean postSuccess() {
        return !StringUtils.isEmpty(result.toString());
    }

    public HashMap<String, String> defaultFileParams(String path) {
        HashMap<String, String> fileParams = new HashMap<>();
        fileParams.put("file", path);
        return fileParams;
    }

    public HashMap<String, String> setFileParams(@NonNull String[] paths) {
        HashMap<String, String> fileParams = new HashMap<>();
        for (String path : paths) {
            fileParams.put("file", path);
        }
        return fileParams;
    }

    public void downloadFile(String _url, String path) {
        int count;
        try {
            URL url = new URL(_url);
            URLConnection connection = url.openConnection();
            connection.connect();

            try (InputStream input = new BufferedInputStream(url.openStream(), 8192)) {
                File tmp = new File(path);
                String tmpPath = tmp.getParent();
                if (tmpPath != null && !new File(tmpPath).exists()) tmp.mkdirs();

                try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(Paths.get(path)))) {
                    byte[] data = new byte[8192]; // Use a larger buffer size for better performance

                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }

                    // flushing output
                    output.flush();
                }
            }
        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
    }

    public String post(String _url, HashMap<String, String> params) {
        return post(_url, params, null);
    }

    public String get(String urlString, boolean printJSON, int readTimeout, int connectionTimeout) {
        HttpURLConnection urlConnection;
        StringBuilder sb = new StringBuilder();
        String jsonString = null;
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            if (url != null) {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(readTimeout /* milliseconds */);
                urlConnection.setConnectTimeout(connectionTimeout /* milliseconds */);
                urlConnection.setDoOutput(true);
                BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                jsonString = sb.toString();
                if (printJSON || DeviceUtils.isEmulator())
                    System.out.println("JSON: " + jsonString);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonString;
    }

    public String post(String _url, HashMap<String, String> params, @Nullable HashMap<String, String> headers) {
        if (TextUtils.isEmpty(_url))
            url = _url;
        result = new StringBuilder();
        try {
            HttpURLConnection urlConnection = getHttpURLConnection(_url, headers);

            OutputStream os = urlConnection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append("&");
                }
                sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            if (debug)
                System.out.println(sb);
            writer.write(sb.toString());
            writer.flush();
            writer.close();
            os.close();
            try {
                BufferedReader reader = getBufferedReader(urlConnection);
                if (debug)
                    System.out.println(result);
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            getCookies(urlConnection);
            urlConnection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (StringUtils.isEmpty(result.toString()))
            return result.toString();
        try {
            jsonObject = new JSONObject(result.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing data " + e);
            Log.e(TAG, result.toString());
        }

        if (debug)
            System.out.println(jsonObject);
        return result.toString();
    }

    public List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

    public void getCookies(HttpURLConnection httpURLConnection) {
        Map<String, List<String>> headerFields = httpURLConnection.getHeaderFields();
        List<String> setCookies = headerFields.get("Set-Cookie");
        if (setCookies != null) {
            for (String cookie : setCookies) {
                String[] cookieParts = cookie.split(";");
                cookies.add(cookieParts[0]);
            }
        }
    }

    private @NonNull HttpURLConnection getHttpURLConnection(String _url, HashMap<String, String> headers) throws IOException {
        URL url = new URL(_url);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setUseCaches(false);
        urlConnection.setReadTimeout(5000);
        urlConnection.setConnectTimeout(5000);
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                urlConnection.setRequestProperty(header.getKey(), header.getValue());
            }
        }
        return urlConnection;
    }

    private @NonNull BufferedReader getBufferedReader(HttpURLConnection urlConnection) throws IOException {
        int responseCode = urlConnection.getResponseCode();
        System.out.println(responseCode);
        InputStream in;
        if (responseCode >= 200 && responseCode <= 400) {
            in = urlConnection.getInputStream();
        } else {
            in = urlConnection.getErrorStream();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(in)));
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return reader;
    }

    public JSONObject uploadFile(String _url, HashMap<String, String> params, HashMap<String, String> files) {
        return uploadFile(_url, params, files, "");
    }

    public JSONObject uploadFile(String _url, HashMap<String, String> params, HashMap<String, String> files, String bearer) {
        if (TextUtils.isEmpty(_url))
            url = _url;
        result = new StringBuilder();
        try {
            conn = (HttpURLConnection) new URL(_url).openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            if (!StringUtils.isEmpty(bearer)) {
                conn.addRequestProperty("Authorization", "Bearer " + bearer);
            }
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(50000);
            if (files != null) {
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                for (String key : files.keySet()) {
                    conn.setRequestProperty(key, files.get(key));
                }
            }
            conn.connect();
            dos = new DataOutputStream(conn.getOutputStream());

            if (files != null) {
                for (String key : files.keySet()) {
                    int bytesRead, bytesAvailable, bufferSize;
                    byte[] buffer;
                    int maxBuffer = 1024 * 1024;

                    File selectedFile = new File(files.get(key));
                    if (!selectedFile.isFile()) {
                        break;
                    }

                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\";filename=\"" + files.get(key) + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    FileInputStream fis = new FileInputStream(selectedFile);
                    bytesAvailable = fis.available();
                    bufferSize = Math.min(bytesAvailable, maxBuffer);
                    buffer = new byte[bufferSize];

                    bytesRead = fis.read(buffer, 0, bufferSize);

                    while (bytesRead > 0) {
                        dos.write(buffer, 0, bufferSize);
                        bytesAvailable = fis.available();
                        bufferSize = Math.min(bytesAvailable, maxBuffer);
                        bytesRead = fis.read(buffer, 0, bufferSize);

                    }

                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    fis.close();
                }
            }
            if (params != null && files != null) {
                for (String key : params.keySet()) {
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(params.get(key));
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                }
            } else if (params != null) {
                StringBuilder sb = new StringBuilder();
                boolean flag = false;
                for (String key : params.keySet()) {
                    try {
                        if (flag) {
                            sb.append("&");
                        }
                        sb.append(key).append("=").append(URLEncoder.encode(params.get(key), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    flag = true;
                    dos.writeBytes(sb.toString());
                }
                if (debug)
                    System.out.println(sb);
                Log.d(TAG, "RC: " + conn.getResponseCode() + " RM: " + conn.getResponseMessage());
                dos.flush();
                dos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(conn.getInputStream())));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        conn.disconnect();

        try {
            jsonObject = new JSONObject(result.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing data " + e);
        }

        return jsonObject;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public HttpUtils setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public HttpUtils setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public boolean isOnline(String site) throws MalformedURLException {
        URL url = new URL(site);
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-Agent", "Android Application: 1.0");
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setConnectTimeout(1000 * 5); // mTimeout is in seconds
            urlConnection.connect();
            return urlConnection.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
