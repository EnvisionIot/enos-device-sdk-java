package http;

import java.io.File;
import java.io.IOException;

import com.envisioniot.enos.iot_http_sdk.HttpConnection;
import com.envisioniot.enos.iot_http_sdk.SessionConfiguration;
import com.envisioniot.enos.iot_http_sdk.StaticDeviceCredential;
import com.envisioniot.enos.iot_mqtt_sdk.core.IResponseCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostResponse;
import com.google.gson.GsonBuilder;

/**
 * This sample shows how to post files (file as measurepoint) over HTTP to EnOS IoT.
 * @author shenjieyuan
 */
public class PostFileSample
{
    // EnOS HTTP Broker URL, which can be obtained from Environment Information page in EnOS Console
    static final String BROKER_URL = "https://broker_url/";

    // Device credentials, which can be obtained from Device Details page in EnOS Console
    static final String PRODUCT_KEY = "productKey";
    static final String DEVICE_KEY = "deviceKey";
    static final String DEVICE_SECRET = "deviceSecret";
    
    private static MeasurepointPostRequest buildMeasurepointPostRequest()
    {
        // Measurepoints are defined in ThingModel
        // newFile is a file-type measurepoint
        return MeasurepointPostRequest.builder()
              .addMeasurePoint("newFile", new File("small_text.txt"))
              .addMeasurePoint("DI_value_01", 4)
              .build();
    }
    
    public static void main(String[] args) throws InterruptedException
    {
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

        // we are going to post a file "example.jpg"
        MeasurepointPostRequest request = buildMeasurepointPostRequest();

        long start = System.currentTimeMillis();
        try
        {
            MeasurepointPostResponse response = connection.publish(request,
                    (bytes, length) ->
            {
                System.out.println(String.format("Progress: %.2f %%", (float) bytes / length * 100.0));
            });
            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(response));
        } catch (IOException | EnvisionException e)
        {
            e.printStackTrace();
            System.out.println((System.currentTimeMillis() - start) + " milliseconds");
            System.exit(1);
        }
        
        // Asynchronously call the measurepoint post
        request = buildMeasurepointPostRequest();
        try
        {
            connection.publish(request, new IResponseCallback<MeasurepointPostResponse>()
            {
                @Override
                public void onResponse(MeasurepointPostResponse response)
                {
                    System.out.println("receive response asynchronously");
                    System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(response));
                }

                @Override
                public void onFailure(Exception failure)
                {
                    failure.printStackTrace();
                }
            }, (bytes, length) ->
            {
                System.out.println(String.format("Progress: %.2f %%", (float) bytes / length * 100.0));
            });
        } catch (IOException | EnvisionException e)
        {
            e.printStackTrace();
        }
    }

}
