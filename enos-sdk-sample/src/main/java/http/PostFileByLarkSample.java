package http;

import com.envisioniot.enos.iot_http_sdk.HttpConnection;
import com.envisioniot.enos.iot_http_sdk.SessionConfiguration;
import com.envisioniot.enos.iot_http_sdk.StaticDeviceCredential;
import com.envisioniot.enos.iot_http_sdk.file.UriInfo;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostResponse;
import com.google.gson.GsonBuilder;
import org.apache.commons.beanutils.BeanUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author mengyuantan
 */
public class PostFileByLarkSample {
    // EnOS HTTP Broker URL, which can be obtained from Environment Information page in EnOS Console
    static final String BROKER_URL = "https://broker_url/";

    // Device credentials, which can be obtained from Device Details page in EnOS Console
    static final String PRODUCT_KEY = "productKey";
    static final String DEVICE_KEY = "deviceKey";
    static final String DEVICE_SECRET = "deviceSecret";

    static final boolean USE_LARK = true;
    static final boolean AUTO_UPLOAD = true;

    private static MeasurepointPostRequest buildMeasurepointPostRequest() {
        // Measurepoints are defined in ThingModel
        // newFile is a file-type measurepoint
        return MeasurepointPostRequest.builder()
                .addMeasurePoint("newFile", new File("small_text.txt"))
                .addMeasurePoint("DI_value_01", 4)
                .build();
    }

    public static void main(String[] args) {
        // construct a static device credential via ProductKey, DeviceKey and DeviceSecret
        StaticDeviceCredential credential = new StaticDeviceCredential(
                PRODUCT_KEY, DEVICE_KEY, DEVICE_SECRET);

        SessionConfiguration configuration = SessionConfiguration.builder()
                .lifetime(30_000)
                .build();

        // construct a http connection
        HttpConnection connection = new HttpConnection.Builder(BROKER_URL, credential)
                .sessionConfiguration(configuration)
                .setUseLark(USE_LARK)
                .setAutoUpload(AUTO_UPLOAD)
                .build();

        // we are going to post a file "example.jpg"
        MeasurepointPostRequest request = buildMeasurepointPostRequest();

        long start = System.currentTimeMillis();
        try {
            MeasurepointPostResponse response = connection.publish(request,
                    (bytes, length) ->
                    {
                        System.out.println(String.format("Progress: %.2f %%", (float) bytes / length * 100.0));
                    });
            if (USE_LARK && response.isSuccess() && request.getFiles() != null) {
                List<Map> responseData = response.getData();
                List<UriInfo> uriInfos = new ArrayList<>();
                responseData.forEach(uriInfo -> {
                    UriInfo info = new UriInfo();
                    try {
                        BeanUtils.populate(info, uriInfo);
                    } catch (Exception e) {

                    }

                    uriInfos.add(info);
                });

                // if use lark and not auto upload file, you can upload the file
                // for each featureId by "uploadUrl", and adding "headers" in header
                uriInfos.forEach(System.out::println);
            } else {
                System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(response));
            }
        } catch (IOException | EnvisionException e) {
            e.printStackTrace();
            System.out.println((System.currentTimeMillis() - start) + " milliseconds");
            System.exit(1);
        }

    }
}
