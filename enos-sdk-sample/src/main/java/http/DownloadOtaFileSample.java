package http;

import com.envisioniot.enos.iot_http_sdk.HttpConnection;
import com.envisioniot.enos.iot_http_sdk.SessionConfiguration;
import com.envisioniot.enos.iot_http_sdk.StaticDeviceCredential;
import com.envisioniot.enos.iot_http_sdk.file.FileCategory;
import com.envisioniot.enos.iot_http_sdk.file.IFileCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author :charlescai
 * @date :2021-03-04
 */
public class DownloadOtaFileSample {
    // EnOS HTTP Broker URL, which can be obtained from Environment Information page in EnOS Console
    static final String BROKER_URL = "https://broker_url/";

    // Device credentials, which can be obtained from Device Details page in EnOS Console
    static final String PRODUCT_KEY = "productKey";
    static final String DEVICE_KEY = "deviceKey";
    static final String DEVICE_SECRET = "deviceSecret";

    public static void main(String[] args) throws EnvisionException {
        // construct a static device credential via ProductKey, DeviceKey and DeviceSecret
        StaticDeviceCredential credential = new StaticDeviceCredential(
                PRODUCT_KEY, DEVICE_KEY, DEVICE_SECRET);

        SessionConfiguration configuration = SessionConfiguration.builder()
                .lifetime(30_000)
                .build();

        // construct a http connection
        HttpConnection connection = new HttpConnection.Builder(BROKER_URL, credential)
                .sessionConfiguration(configuration)
                .build();

        // fileUri is an enos scheme file uri
        String fileUri = "enos-connect://xxx.zip";
        int bufferLength = 1024 * 1024;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            InputStream inputStream = connection.downloadFile(fileUri, FileCategory.OTA);
            byte[] buffer = new byte[bufferLength];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            byte[] data = outputStream.toByteArray();
            System.out.println(new String(data));
        } catch (EnvisionException | IOException e) {
            e.printStackTrace();
        }

        // Asynchronously call the file download request
        try {
            connection.downloadFileAsync(fileUri, FileCategory.OTA, new IFileCallback() {
                        @Override
                        public void onResponse(InputStream inputStream) throws IOException {
                            System.out.println("download ota file asynchronously");
                            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                                byte[] buffer = new byte[bufferLength];
                                int len;
                                while ((len = inputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, len);
                                }
                                byte[] data = outputStream.toByteArray();
                                System.out.println(new String(data));
                            }
                        }

                        @Override
                        public void onFailure(Exception failure) {
                            failure.printStackTrace();
                        }
                    }
            );
        } catch (EnvisionException e) {
            e.printStackTrace();
        }
    }
}

