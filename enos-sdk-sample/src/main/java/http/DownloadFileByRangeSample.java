package http;

import com.envisioniot.enos.iot_http_sdk.HttpConnection;
import com.envisioniot.enos.iot_http_sdk.SessionConfiguration;
import com.envisioniot.enos.iot_http_sdk.StaticDeviceCredential;
import com.envisioniot.enos.iot_http_sdk.file.FileCategory;
import com.envisioniot.enos.iot_http_sdk.file.IFileCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.RangeFileBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author :charlescai
 * @date :2021-05-10
 */
public class DownloadFileByRangeSample {
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
        String fileUri = "enos-connect://xxx.txt";
        long startRange = 0;
        long endRange = 1023;
        int bufferLength = 1024;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            RangeFileBody rangeFileBody = connection.downloadFile(fileUri, FileCategory.FEATURE, startRange, endRange);

            InputStream inputStream = rangeFileBody.getData();
            byte[] buffer = new byte[bufferLength];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            System.out.println(outputStream);
        } catch (EnvisionException | IOException e) {
            e.printStackTrace();
        }


        // Asynchronously call the file download request
        try {
            connection.downloadFileAsync(fileUri, FileCategory.FEATURE, startRange, endRange, new IFileCallback() {
                        @Override
                        public void onRangeResponse(RangeFileBody rangeFileBody) throws IOException {
                            System.out.println("download feature ile asynchronously");
                            InputStream inputStream = rangeFileBody.getData();
                            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                                byte[] buffer = new byte[bufferLength];
                                int len;
                                while ((len = inputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, len);
                                }
                                System.out.println(outputStream);
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

