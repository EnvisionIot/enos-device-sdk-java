package http;

import com.envisioniot.enos.iot_http_sdk.HttpConnection;
import com.envisioniot.enos.iot_http_sdk.SessionConfiguration;
import com.envisioniot.enos.iot_http_sdk.StaticDeviceCredential;
import com.envisioniot.enos.iot_mqtt_sdk.core.IResponseCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostResponse;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * This sample shows how to post measurepoints over HTTP to EnOS IoT.
 * @author shenjieyuan
 */
public class MeasurepointPostSample
{
//    static final String BROKER_URL = "http://10.16.13.192:8080";
//    static final String PRODUCT_KEY = "product_key";
//    static final String DEVICE_KEY = "device_key";
//    static final String DEVICE_SECRET = "device_secret";

    static final String BROKER_URL = "http://iot-http-broker.alpha-k8s-cn4.eniot.io";
    static final String PRODUCT_KEY = "FZEYbbGq";
    static final String DEVICE_KEY = "sample_device";
    static final String DEVICE_SECRET = "sRlGhkC0TCGBz5sdSrkU";
    
    public static void main(String[] args) throws InterruptedException
    {
        // construct a static device credential via ProductKey, DeviceKey and DeviceSecret
        StaticDeviceCredential credential = new StaticDeviceCredential(
                PRODUCT_KEY, DEVICE_KEY, DEVICE_SECRET);
        
        // construct a http connection
        SessionConfiguration configuration = SessionConfiguration.builder().lifetime(10_000).build();

        HttpConnection connection = new HttpConnection.Builder(BROKER_URL, credential)
                .sessionConfiguration(configuration)
                .build();

        MeasurepointPostRequest request = MeasurepointPostRequest.builder()
                .addMeasurePoint("mp_int", 4)
//                .addMeasurePoint("voltage", 5.0)
                .build();
        try
        {
            MeasurepointPostResponse response = connection.publish(request);
            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(response));
        } catch (EnvisionException | IOException e)
        {
            e.printStackTrace();
        }
        
        // Wait for more than life time, the connection shall automatically re-auth
        System.out.println("current sessionId: " + connection.getSessionId());
        TimeUnit.SECONDS.sleep(15L);
        try
        {
            MeasurepointPostResponse response = connection.publish(request);
            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(response));
        } catch (EnvisionException | IOException e)
        {
            e.printStackTrace();
        }
        System.out.println("new sessionId: " + connection.getSessionId());
        
        // Asynchronously call the measurepoint post
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
            });
        } catch (EnvisionException | IOException e)
        {
            e.printStackTrace();
        }
    }

}
