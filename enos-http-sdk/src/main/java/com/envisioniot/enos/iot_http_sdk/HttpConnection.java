package com.envisioniot.enos.iot_http_sdk;

import static com.envisioniot.enos.iot_http_sdk.HttpConnectionError.CLIENT_ERROR;
import static com.envisioniot.enos.iot_http_sdk.HttpConnectionError.UNSUCCESSFUL_AUTH;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.google.common.net.MediaType.OCTET_STREAM;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.envisioniot.enos.iot_http_sdk.auth.AuthRequestBody;
import com.envisioniot.enos.iot_http_sdk.auth.AuthResponseBody;
import com.envisioniot.enos.iot_mqtt_sdk.core.IResponseCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignMethod;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignUtil;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.ModelUpRawRequest;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Monitor;
import com.google.gson.Gson;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This class provides a http connection towards EnOS IoT HTTP Broker
 * 
 * The connection should be re-used for continuous sending / receiving of HTTP
 * messages.
 * 
 * @author shenjieyuan
 */
@Slf4j
public class HttpConnection
{
    public static final String VERSION = "1.1";

    /**
     * Builder for http connection. A customized OkHttpClient can be provided, to
     * define specific connection pool, proxy etc. Find more at
     * {@link #OkHttpClient}
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

            CompletableFuture.runAsync(() ->
            {
                try
                {
                    instance.auth();
                } catch (EnvisionException e)
                {
                    // do nothing, already handled
                }
            });

            return instance;
        }
        
        /**
         * Fluent API to set session configuration
         * @param configuration
         * @return
         */
        public Builder sessionConfiguration(SessionConfiguration configuration)
        {
            this.sessionConfiguration = configuration;
            return this;
        }
    }

    private String brokerUrl;

    private ICredential credential;

    private SessionConfiguration sessionConfiguration;

    private OkHttpClient okHttpClient = null;

    // 用于自动上线设备
    private Monitor authMonitor = new Monitor();

    private volatile AuthResponseBody lastAuthResponse = null;
    
    @Getter
    private volatile String sessionId = null;

    @Getter
    private long lastPublishTimestamp;

    @Getter @Setter
    private AtomicInteger requestId = new AtomicInteger(0);

    /**
     * Generate sign for auth request, only use SHA-256 method
     * 
     * @return
     */
    private String sign()
    {
        Preconditions.checkArgument(credential instanceof StaticDeviceCredential, "unsupported credential format");
        return SignUtil.sign(((StaticDeviceCredential) credential).getDeviceSecret(),
                ImmutableMap.of("productKey", ((StaticDeviceCredential) credential).getProductKey(), "deviceKey",
                        ((StaticDeviceCredential) credential).getDeviceKey(), "lifetime",
                        String.valueOf(sessionConfiguration.getLifetime()), "signMethod", SignMethod.SHA256.getName()),
                SignMethod.SHA256);
    }

    static final String MEDIA_TYPE_JSON_UTF_8 = JSON_UTF_8.toString();
    static final String MEDIA_TYPE_OCTET_STREAM = OCTET_STREAM.toString();

    /**
     * 执行设备上线请求
     * 
     * @return auth response
     * @throws EnvisionException
     */
    public AuthResponseBody auth() throws EnvisionException
    {
        if (authMonitor.tryEnter())
        {
            // 执行auth请求
            try
            {
                // clean up old auth response
                lastAuthResponse = null;
                sessionId = null;

                RequestBody body = RequestBody.create(MediaType.parse(MEDIA_TYPE_JSON_UTF_8), new Gson().toJson(
                        new AuthRequestBody(SignMethod.SHA256.getName(), sessionConfiguration.getLifetime(), sign())));

                Request request = new Request.Builder().url(brokerUrl + credential.getAuthPath()).post(body).build();

                Call call = okHttpClient.newCall(request);
                Response response = call.execute();

                if (response.isSuccessful())
                {
                    lastAuthResponse = new Gson().fromJson(response.body().charStream(), AuthResponseBody.class);
                    // 更新sessionId
                    sessionId = lastAuthResponse.getData().getSessionId();
                    // 存储lastPostTimestamp
                    lastPublishTimestamp = System.currentTimeMillis();

                    log.info("auth success, store sessionId = " + sessionId);

                    return lastAuthResponse;
                } else
                {
                    // auth failed
                    AuthResponseBody failure = new AuthResponseBody();
                    failure.setCode(response.code());
                    failure.setMessage(response.message());

                    log.info("auth failed. " + failure);

                    return failure;
                }
            } catch (IOException e)
            {
                log.warn("failed to execut auth request", e);

                throw new EnvisionException(e, CLIENT_ERROR);
            } finally
            {
                authMonitor.leave();
            }
        } else if (authMonitor.enter(10L, TimeUnit.SECONDS))
        {
            // 至多等待10秒钟，尝试获取 Auth Response
            try
            {
                if (lastAuthResponse != null)
                {
                    return lastAuthResponse;
                }
            } finally
            {
                authMonitor.leave();
            }
        }
        throw new EnvisionException(UNSUCCESSFUL_AUTH);
        // 无法获取auth response
    }

    private void checkAuth() throws EnvisionException
    {
        // 如果没有sessionId，需要先登录获取sessionId
        if (Strings.isNullOrEmpty(sessionId) || 
            System.currentTimeMillis() - lastPublishTimestamp > sessionConfiguration.getLifetime())
        {
            auth();
            if (Strings.isNullOrEmpty(sessionId))
            {
                throw new EnvisionException(UNSUCCESSFUL_AUTH);
            }
        }
    }

    /**
     * complete a Request message
     */
    private void fillRequest(BaseMqttRequest<?> request)
    {
        if (Strings.isNullOrEmpty(request.getMessageId()))
        {
            request.setMessageId(String.valueOf(requestId.incrementAndGet()));
        }

        // Also populate request version for IMqttRequest
        request.setVersion(VERSION);

        if (Strings.isNullOrEmpty(request.getProductKey()) && Strings.isNullOrEmpty(request.getDeviceKey()))
        {
            request.setProductKey(((StaticDeviceCredential) credential).getProductKey());
            request.setDeviceKey(((StaticDeviceCredential) credential).getDeviceKey());
        }
    }

    /**
     * Check if the request is raw request
     * 
     * @param request
     * @return
     */
    private boolean isUpRaw(BaseMqttRequest<?> request)
    {
        return request instanceof ModelUpRawRequest;
    }
    
    /**
     * 执行okHttp Call，获取Response
     * @param <T>
     * @param call
     * @param request
     * @return
     * @throws EnvisionException
     */
    private <T extends BaseMqttResponse> T publishCall(Call call, BaseMqttRequest<T> request) throws EnvisionException
    {
        try
        {
            Response httpResponse = call.execute();

            T response = request.getAnswerType().newInstance();
            try
            {
                response.decode(request.getAnswerTopic(), httpResponse.body().bytes());
            } catch (Exception e)
            {
                log.info("failed to decode response", e);
                response.setId(request.getId());
                response.setCode(httpResponse.code());
                response.setMessage(httpResponse.message());
            }
            return response;
        } catch (Exception e)
        {
            log.warn("failed to execute request", e);
            throw new EnvisionException(CLIENT_ERROR);
        }
    }
    
    /**
     * 异步执行okHttp Call，通过Callback方法处理Response
     * @param <T>
     * @param call
     * @param request
     * @param callback
     */
    private <T extends BaseMqttResponse> void publishCallAsync(Call call, BaseMqttRequest<T> request, IResponseCallback<T> callback)
    {
        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response httpResponse) throws IOException
            {
                T response;
                try
                {
                    response = request.getAnswerType().newInstance();
                } catch (Exception e)
                {
                    log.error("failed to construct response", e);
                    throw new IOException("failed to construct response", e);
                }
                try
                {
                    response.decode(request.getAnswerTopic(), httpResponse.body().bytes());
                } catch (Exception e)
                {
                    log.info("failed to decode response");
                    response.setId(request.getId());
                    response.setCode(httpResponse.code());
                    response.setMessage(httpResponse.message());
                }
                callback.onResponse(response);
            }
        });
    }

    /**
     * 生成一个用于okHttp的请求消息
     * @param <T>
     * @param request
     * @return
     * @throws EnvisionException
     */
    private <T extends BaseMqttResponse> Call generatePublishCall(BaseMqttRequest<T> request) throws EnvisionException
    {
        checkAuth();
        // 将请求消息设置完整
        fillRequest(request);

        RequestBody body;
        if (!isUpRaw(request))
        {
            // 准备请求消息
            body = RequestBody.create(MediaType.parse(MEDIA_TYPE_JSON_UTF_8), new String(request.encode(), UTF_8));
        } else
        {
            body = RequestBody.create(MediaType.parse(MEDIA_TYPE_OCTET_STREAM), request.encode());
        }

        Request httpRequest = new Request.Builder()
                .url(brokerUrl + "/topic" + request.getMessageTopic() + "?sessionId=" + sessionId).post(body).build();

        Call call = okHttpClient.newCall(httpRequest);
        return call;
    }
    
    
    private <T extends BaseMqttResponse> Call generateMultipartPublishCall(BaseMqttRequest<T> request, 
            Map<String, File> files) throws EnvisionException, IOException
    {
        checkAuth();

        // 将请求消息设置完整
        fillRequest(request);
        
        // 准备一个Multipart请求消息
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("enos-message", new String(request.encode(), UTF_8));
        
        for (Entry<String,File> entry: files.entrySet())
        {
            String filename = entry.getKey();
            File file = entry.getValue();
            
            builder.addPart(FileFormData.createFormData(filename, filename, file));
        }
        
        RequestBody body = builder.build();

        Request httpRequest = new Request.Builder()
                .url(brokerUrl + "/multipart" + request.getMessageTopic() + "?sessionId=" + sessionId).post(body).build();

        Call call = okHttpClient.newCall(httpRequest);
        return call;
    }

    /**
     * Publish a request to EnOS IOT HTTP broker
     * 
     * @param <T>
     *            Response
     * @param request
     * @return response
     * @throws EnvisionException
     */
    public <T extends BaseMqttResponse> T publish(BaseMqttRequest<T> request) throws EnvisionException
    {
        Call call = generatePublishCall(request);
        return publishCall(call, request);
    }

    /**
     * Publish a request to EnOS IOT HTTP broker, asynchronously with a callback
     * 
     * @param <T>
     *            Response
     * @param request
     * @param callback
     * @throws EnvisionException
     */
    public <T extends BaseMqttResponse> void publish(BaseMqttRequest<T> request, IResponseCallback<T> callback)
            throws EnvisionException
    {
        Call call = generatePublishCall(request);
        publishCallAsync(call, request, callback);
    }

    /**
     * Post a request to EnOS IOT HTTP Broker with files
     * 
     * Inside the request, the file should be identified by a prefix "local://".
     * <br>
     * For example, to post a file named "ameter.jpg" as measure point
     * <i>camera</i>'s value: <br>
     * "camera" : "local://ameter.jpg"
     * 
     * @param <T>
     *            Response
     * @param request
     * @param files
     *            file name and files
     * @return response
     * @throws EnvisionException
     * @throws IOException error to read files
     */
    public <T extends BaseMqttResponse> T publishMultipart(BaseMqttRequest<T> request, Map<String, File> files)
            throws EnvisionException, IOException
    {
        Call call = generateMultipartPublishCall(request, files);
        return publishCall(call, request);
    }
    
    
    /**
     * Post a request to EnOS IOT HTTP Broker with files, handle the response in a callback function
     * 
     * Inside the request, the file should be identified by a prefix "local://".
     * <br>
     * For example, to post a file named "ameter.jpg" as measure point
     * <i>camera</i>'s value: <br>
     * "camera" : "local://ameter.jpg"
     * 
     * @param <T>
     *            Response
     * @param request
     * @param files
     *            file name and files
     * @param callback
     * @return response
     * @throws EnvisionException
     * @throws IOException error to read files
     */
    public <T extends BaseMqttResponse> void publishMultipart(BaseMqttRequest<T> request, Map<String, File> files,
            IResponseCallback<T> callback)
            throws EnvisionException, IOException
    {
        Call call = generateMultipartPublishCall(request, files);
        publishCallAsync(call, request, callback);
    }
}
