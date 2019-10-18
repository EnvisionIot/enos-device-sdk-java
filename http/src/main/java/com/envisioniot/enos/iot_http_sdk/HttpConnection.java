package com.envisioniot.enos.iot_http_sdk;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.envisioniot.enos.iot_http_sdk.auth.AuthResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.Monitor;

import lombok.Data;
import lombok.NonNull;
import okhttp3.OkHttpClient;

/**
 * This class provides a http connection towards EnOS IoT HTTP Broker
 * 
 * The connection should be re-used for continuous sending / receiving of HTTP messages.
 * 
 * @author shenjieyuan
 */
public class HttpConnection
{
    /**
     * Builder for http connection.
     * A customized OkHttpClient can be provided, to define specific connection pool, proxy etc.
     * Find more at {@link #OkHttpClient}
     * 
     * @author shenjieyuan
     */
    @Data
    public static class Builder
    {
        @NonNull
        private String brokerUrl;
        
        @NonNull
        private ICredential credential;
        
        private SessionConfiguration sessionConfiguration;
        
        private OkHttpClient okHttpClient;
        
        public HttpConnection build()
        {
            HttpConnection instance = new HttpConnection();

            Preconditions.checkNotNull(brokerUrl);
            instance.brokerUrl = brokerUrl;

            Preconditions.checkNotNull(credential);
            instance.credential = credential;

            if (sessionConfiguration == null)
            {
                sessionConfiguration = SessionConfiguration.builder().build();
            }
            instance.sessionConfiguration = sessionConfiguration;

            // allocate client
            if (okHttpClient == null)
            {
                okHttpClient = new OkHttpClient();
            }
            instance.okHttpClient = okHttpClient;
            
            CompletableFuture.runAsync(() -> instance.auth());

            return instance;
        }
    }

    private String brokerUrl;
    
    private ICredential credential;
    
    private SessionConfiguration sessionConfiguration;

    private OkHttpClient okHttpClient = null;
    
    // 用于自动上线设备
    private Monitor authMonitor = new Monitor();

    private volatile AuthResponse lastAuthResponse = null;
    private volatile String sessionId = null;

    private long lastPostTimestamp;
    
    /**
     * 执行设备上线请求
     * @return auth response
     */
    public AuthResponse auth()
    {
        if (authMonitor.tryEnter())
        {
            // 执行auth请求
            try
            {
                // TODO: invoke  auth interface, save session id if success
                
                lastAuthResponse = null; //  FIXME

                //更新sessionId
                sessionId = lastAuthResponse.getData().getSessionId();
                //存储lastPostTimestamp
                lastPostTimestamp = System.currentTimeMillis();
            }
            finally
            {
                authMonitor.leave();
            }
        }
        else if (authMonitor.enter(10L, TimeUnit.SECONDS))
        {
            // 至多等待10秒钟，尝试获取 Auth Response
            try
            {
                return lastAuthResponse;
            }
            finally
            {
                authMonitor.leave();
            }
        }
        return null;        // 无法获取auth response
    }
    
    private void checkAuth()
    {
        // 如果没有sessionId，需要先登录获取sessionId
        if (Strings.isNullOrEmpty(sessionId) || 
            System.currentTimeMillis() - lastPostTimestamp > sessionConfiguration.getLifetime())
        {
            auth();
            if (Strings.isNullOrEmpty(sessionId))
            {
                // FIXME 执行出错处理，无法登录
            }
        }
    }


    /**
     * Post a request to EnOS IOT HTTP broker
     * @param <T> Response
     * @param request 
     * @return response
     */
    public <T extends BaseMqttResponse> T postRequest(BaseMqttRequest<T> request)
    {
        checkAuth();
        
        // TODO: 使用session ID 发送请求，获取响应结果
        T response = null;
        
        return response;
    }
    
    
    /**
     * Post a request to EnOS IOT HTTP Broker with files
     * 
     * Inside the request, the file should be identified by a prefix "local://". <br>
     * For example, to post a file named "ameter.jpg" as measure point <i>camera</i>'s value: <br>
     *     "camera" : "local://ameter.jpg"
     * 
     * @param <T> Response
     * @param request
     * @param files file name and files
     * @return response
     */
    public <T extends BaseMqttResponse> T postMultipartRequest(BaseMqttRequest<T> request, 
            Map<String, File> files)
    {
        checkAuth();
        
        // TODO: 使用session ID发送请求，获取响应结果
        // 读取文件，变成Multipart组成部分。
        // 准备一个专门的contentLength、 MD5 参数。
        T response = null;
        
        return response;
    }
}
