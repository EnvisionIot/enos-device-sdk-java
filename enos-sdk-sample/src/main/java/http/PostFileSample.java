package http;

import java.io.File;
import java.io.IOException;
import java.util.Random;

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
//    static final String BROKER_URL = "http://10.16.13.192:8080";
//    static final String PRODUCT_KEY = "product_key";
//    static final String DEVICE_KEY = "device_key";
//    static final String DEVICE_SECRET = "device_secret";
//
//    static final String BROKER_URL = "http://iot-http-broker.beta-k8s-cn4.eniot.io/";
//    static final String PRODUCT_KEY = "TZrXVtm5";
//    static final String DEVICE_KEY = "DynamicActivating1";
//    static final String DEVICE_SECRET = "EWdFnTEalsndjrPqEGjL";

    static final String BROKER_URL = "http://iot-http-broker.alpha-k8s-cn4.eniot.io/";
    static final String PRODUCT_KEY = "nAMs31QI";
    static final String DEVICE_KEY = "fBbhyb13Hn";
    static final String DEVICE_SECRET = "TTL1oiKgyfxqq2GLhu3w";
    
    private static MeasurepointPostRequest buildMeasurepointPostRequest()
    {
        return MeasurepointPostRequest.builder()
                .addMeasurePoint("Int_value", new Random().nextInt(100))
                .addMeasurePoint("File_value", new File("small_text.txt"))
//              .addMeasurePoint("voltage", 5.0)
              .build();
    }
    
    public static void main(String[] args) throws InterruptedException
    {
        // construct a static device credential via ProductKey, DeviceKey and DeviceSecret
        StaticDeviceCredential credential = new StaticDeviceCredential(
                PRODUCT_KEY, DEVICE_KEY, DEVICE_SECRET);
        
        // construct a http connection
        HttpConnection connection = new HttpConnection.Builder(BROKER_URL, credential).build();

        // we are going to post a file "example.jpg"
        MeasurepointPostRequest request = buildMeasurepointPostRequest();

        try
        {
            MeasurepointPostResponse response = connection.publish(request,
                    (bytes, length) ->
            {
                System.out.println(String.format("Progress: %.2f %%", (float) bytes / length));
            });
            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(response));
        } catch (IOException | EnvisionException e)
        {
            e.printStackTrace();
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
                System.out.println(String.format("Progress: %.2f %%", (float) bytes / length));
            });
        } catch (IOException | EnvisionException e)
        {
            e.printStackTrace();
        }
    }

}
