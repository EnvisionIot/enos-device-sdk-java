package com.envisioniot.enos.iot_http_sdk.file;

import okhttp3.*;
import org.apache.commons.collections.MapUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author mengyuantan
 */
public class FileUtil {
    public static Response uploadFile(String serverURL, File file, Map<String, String> headers) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody body = RequestBody.create(mediaType, file);
        Request.Builder builder = new Request.Builder();
        if (MapUtils.isNotEmpty(headers)) {
            headers.forEach(builder::addHeader);
        }
        Request request = builder
                .url(serverURL)
                .method("PUT", body)
                .addHeader("Content-Type", "application/octet-stream")
                .build();
        return client.newCall(request).execute();
    }

    public static Response downloadFile(String url) throws IOException {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request httpRequest = new Request.Builder()
                .url(url)
                .get()
                .build();

        Call call = okHttpClient.newCall(httpRequest);
        return call.execute();
    }
}
