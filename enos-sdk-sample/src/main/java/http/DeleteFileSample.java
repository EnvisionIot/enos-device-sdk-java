package http;

import com.envisioniot.enos.iot_http_sdk.HttpConnection;
import com.envisioniot.enos.iot_http_sdk.SessionConfiguration;
import com.envisioniot.enos.iot_http_sdk.StaticDeviceCredential;
import com.envisioniot.enos.iot_http_sdk.file.FileDeleteResponse;
import com.envisioniot.enos.iot_mqtt_sdk.core.IResponseCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.google.gson.GsonBuilder;

/**
 * @author :charlescai
 * @date :2020-04-22
 */
public class DeleteFileSample {
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
        try {
            FileDeleteResponse response = connection.deleteFile(fileUri);
            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(response));
        } catch (EnvisionException e) {
            e.printStackTrace();
        }

        // Asynchronously call the file download request
        try {
            connection.deleteFileAsync(fileUri,  new IResponseCallback<FileDeleteResponse>() {
                        @Override
                        public void onResponse(FileDeleteResponse response)  {
                            System.out.println("receive response asynchronously");
                            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(response));
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
