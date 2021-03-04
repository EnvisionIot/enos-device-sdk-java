package http;

import com.envisioniot.enos.iot_http_sdk.HttpConnection;
import com.envisioniot.enos.iot_http_sdk.SessionConfiguration;
import com.envisioniot.enos.iot_http_sdk.StaticDeviceCredential;
import com.envisioniot.enos.iot_http_sdk.file.FileCategory;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;

/**
 * @author mengyuantan
 */
public class DownloadFileByLarkSample {
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
        String fileUri = "enos-lark://xxx.txt";
        String downloadUrl = connection.getDownloadUrl(fileUri, FileCategory.FEATURE);
        System.out.println(downloadUrl);
    }
}