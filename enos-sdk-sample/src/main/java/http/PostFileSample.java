package http;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.envisioniot.enos.iot_http_sdk.HttpConnection;
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
    static final String BROKER_URL = "http://10.16.13.192:8080";
    static final String PRODUCT_KEY = "product_key";
    static final String DEVICE_KEY = "device_key";
    static final String DEVICE_SECRET = "device_secret";
    
    public static void main(String[] args) throws InterruptedException
    {
        // construct a static device credential via ProductKey, DeviceKey and DeviceSecret
        StaticDeviceCredential credential = new StaticDeviceCredential(
                PRODUCT_KEY, DEVICE_KEY, DEVICE_SECRET);
        
        // construct a http connection
        HttpConnection connection = new HttpConnection.Builder(BROKER_URL, credential).build();

        // we are going to post a file "example.jpg"
        MeasurepointPostRequest request = MeasurepointPostRequest.builder()
                .addMeasurePoint("current", 4.5)
                .addMeasurePoint("file", "local://example.jpg")
                .build();

        Map<String,File> files = new HashMap<>();
        files.put("example.jpg", new File("example.jpg"));

        try
        {
            MeasurepointPostResponse response = connection.publishMultipart(request, files);
            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(response));
        } catch (IOException | EnvisionException e)
        {
            e.printStackTrace();
        }
        
        // Asynchronously call the measurepoint post
        try
        {
            connection.publishMultipart(request, files, 
                    new IResponseCallback<MeasurepointPostResponse>()
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
            });
        } catch (IOException | EnvisionException e)
        {
            e.printStackTrace();
        }
    }

}
