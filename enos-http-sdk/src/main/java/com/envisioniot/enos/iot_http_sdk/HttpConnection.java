package com.envisioniot.enos.iot_http_sdk;

import com.envisioniot.enos.iot_http_sdk.auth.AuthRequestBody;
import com.envisioniot.enos.iot_http_sdk.auth.AuthResponseBody;
import com.envisioniot.enos.iot_http_sdk.file.*;
import com.envisioniot.enos.iot_http_sdk.progress.IProgressListener;
import com.envisioniot.enos.iot_http_sdk.progress.ProgressRequestWrapper;
import com.envisioniot.enos.iot_mqtt_sdk.core.IResponseCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignMethod;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignUtil;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.FileScheme;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMessageHandler;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttArrivedMessage;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttArrivedMessage.DecodeResult;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttDeliveryMessage;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.BaseMqttCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.ModelUpRawRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.UploadFileInfo;
import com.envisioniot.enos.iot_mqtt_sdk.util.GsonUtil;
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
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.envisioniot.enos.iot_http_sdk.HttpConnectionError.*;
import static com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.FormDataConstants.ENOS_MESSAGE;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.google.common.net.MediaType.OCTET_STREAM;
import static java.nio.charset.StandardCharsets.UTF_8;

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

    public static final String DOWNLOAD_PATH_FORMAT = "/sys/%s/%s/file/download";
    public static final String GET_DOWNLOAD_URL_PATH_FORMAT = "/sys/%s/%s/file/download/url";
    public static final String DELETE_PATH_FORMAT = "/sys/%s/%s/file/delete";

    public static final String MEDIA_TYPE_JSON_UTF_8 = JSON_UTF_8.toString();
    public static final String MEDIA_TYPE_OCTET_STREAM = OCTET_STREAM.toString();

    private static final String CMD_PAYLOAD =  "command-payload";

    /**
     * Builder for http connection. A customized OkHttpClient can be provided, to
     * define specific connection pool, proxy etc. Find more at
     * {@link #okHttpClient}
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

        private boolean useLark = false;

        private boolean autoUpload = true;

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
                okHttpClient = new OkHttpClient.Builder()
                        .connectTimeout(10L, TimeUnit.SECONDS)
                        .readTimeout(2L, TimeUnit.MINUTES)
                        .writeTimeout(2L, TimeUnit.MINUTES)
                        .retryOnConnectionFailure(false)
                        .build();
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

            instance.setUseLark(this.useLark);
            instance.setAutoUpload(this.autoUpload);

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

        public Builder setUseLark(boolean useLark) {
            this.useLark = useLark;
            return this;
        }

        public Builder setAutoUpload(boolean autoUpload) {
            this.autoUpload = autoUpload;
            return this;
        }
    }

    private String brokerUrl;

    private ICredential credential;

    private SessionConfiguration sessionConfiguration;

    @Getter @Setter
    private boolean autoUpload = true;

    @Getter @Setter
    private boolean useLark = false;

    @Getter
    private OkHttpClient okHttpClient = null;

    // For automatic online devices
    private Monitor authMonitor = new Monitor();

    private volatile AuthResponseBody lastAuthResponse = null;
    
    @Getter
    private volatile String sessionId = null;

    @Getter
    private long lastPublishTimestamp;

    @Getter @Setter
    private AtomicInteger requestId = new AtomicInteger(0);

    private Map<Class<? extends IMqttArrivedMessage>, IMessageHandler<?, ?>> arrivedMsgHandlerMap = new ConcurrentHashMap<>();

    public void setArrivedMsgHandler(Class<? extends IMqttArrivedMessage> arrivedMsgCls, IMessageHandler<?, ?> handler) {
        arrivedMsgHandlerMap.put(arrivedMsgCls, handler);
    }

    public void removeArrivedMsgHandler(Class<? extends IMqttArrivedMessage> arrivedMsgCls) {
        arrivedMsgHandlerMap.remove(arrivedMsgCls);
    }

    @SuppressWarnings("unchecked")
    public void handleAdditionalMsg(Headers headers){
        if(headers == null){
            return;
        }
        String msg = headers.get(CMD_PAYLOAD);
        if(msg == null){
            return;
        }
        BaseMqttCommand<?> command = MethodClassMap.convertFromJson(msg);
        if(command == null){
            return;
        }

        final IMessageHandler<BaseMqttCommand<?>, IMqttDeliveryMessage> handler =
                (IMessageHandler<BaseMqttCommand<?>, IMqttDeliveryMessage>) arrivedMsgHandlerMap.get(command.getClass());
        if(handler == null){
            return;
        }
        try {
            handler.onMessage(command, null);
        } catch (Exception e) {
            log.warn("Failed to run : " + headers.get(CMD_PAYLOAD), e);
        }
    }

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

    private ClientInfo clientInfo() throws IOException {

        final Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("application.properties"));

        String sdkVersion = properties.getProperty("version");
        InetAddress ip4 = Inet4Address.getLocalHost();
        String loginIpAddress = ip4.getHostAddress();
        return new ClientInfo(loginIpAddress, sdkVersion);
    }

    /**
     * Perform device login request
     *
     * @return auth response
     * @throws EnvisionException
     */
    public AuthResponseBody auth() throws EnvisionException
    {
        if (authMonitor.tryEnter())
        {
            // Execute auth request
            try
            {
                // clean up old auth response
                lastAuthResponse = null;
                sessionId = null;

                RequestBody body = RequestBody.create(MediaType.parse(MEDIA_TYPE_JSON_UTF_8), new Gson().toJson(
                        new AuthRequestBody(SignMethod.SHA256.getName(), sessionConfiguration.getLifetime(),
                                sign(), clientInfo())));

                Request request = new Request.Builder().url(brokerUrl + credential.getAuthPath()).post(body).build();

                Call call = okHttpClient.newCall(request);
                Response response = call.execute();

                if (response.isSuccessful())
                {
                    lastAuthResponse = new Gson().fromJson(response.body().charStream(), AuthResponseBody.class);
                    // update sessionId
                    if (!lastAuthResponse.isSuccess()) {
                        throw new EnvisionException(lastAuthResponse.getCode(), lastAuthResponse.getMessage());
                    }

                    sessionId = lastAuthResponse.getData().getSessionId();

                    // save lastPostTimestamp
                    lastPublishTimestamp = System.currentTimeMillis();

                    log.info("auth success, store sessionId = " + sessionId);

                    authMonitor.leave();
                    handleAdditionalMsg(response.headers());
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
                if(authMonitor.isOccupied()) {
                    authMonitor.leave();
                }
            }
        } else if (authMonitor.enter(10L, TimeUnit.SECONDS))
        {
            // Wait at most 10 seconds and try to get Auth Response
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
        // Unable to get auth response
    }

    private void checkAuth() throws EnvisionException
    {
        // If there is no sessionId, you need to log in first to obtain the sessionId
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
        // generate a message id is not generated yet
        if (Strings.isNullOrEmpty(request.getMessageId()))
        {
            request.setMessageId(String.valueOf(requestId.incrementAndGet()));
        }

        // Also populate request version for IMqttRequest
        request.setVersion(VERSION);

        // use credential pk / dk as request pk / dk
        if (Strings.isNullOrEmpty(request.getProductKey()) && Strings.isNullOrEmpty(request.getDeviceKey()))
        {
            request.setProductKey(((StaticDeviceCredential) credential).getProductKey());
            request.setDeviceKey(((StaticDeviceCredential) credential).getDeviceKey());
        }
        
        // fill pk / dk in upload files
        if (request.getFiles() != null)
        {
            request.getFiles().stream()
            .forEach(fileInfo -> {
                fileInfo.setProductKey(((StaticDeviceCredential) credential).getProductKey());
                fileInfo.setDeviceKey(((StaticDeviceCredential) credential).getDeviceKey());
            });
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
     * OkHttp Call to get Response
     * @param <T>
     * @param call
     * @param request
     * @return
     * @throws EnvisionException
     */
    private <T extends BaseMqttResponse> T publishCall(Call call, BaseMqttRequest<T> request) throws EnvisionException
    {
        try(Response httpResponse = call.execute())
        {
            T response = request.getAnswerType().newInstance();
            try
            {
                handleAdditionalMsg(httpResponse.headers());
                DecodeResult result = response.decode(request.getAnswerTopic(),
                        httpResponse.body() == null ? null : httpResponse.body().bytes());
                T rsp = result.getArrivedMsg();
                if (request.getFiles() != null && this.isUseLark()) {
                    List<Map> uriInfos = rsp.getData();
                    List<UploadFileInfo> fileInfos = request.getFiles();

                    Map<String, File> featureIdAndFileMap = new HashMap<>();
                    fileInfos.forEach(fileInfo -> featureIdAndFileMap.put(fileInfo.getFilename(), fileInfo.getFile()));
                    uriInfos.forEach(uriInfoMap -> {
                        try {
                            UriInfo uriInfo = GsonUtil.fromJson(GsonUtil.toJson(uriInfoMap), UriInfo.class);
                            String filename = uriInfo.getFilename();
                            uriInfoMap.put("filename", featureIdAndFileMap.get(filename).getName());
                            if (this.isAutoUpload()) {
                                Response uploadFileRsp = FileUtil.uploadFile(uriInfo.getUploadUrl(),
                                        featureIdAndFileMap.get(filename), uriInfo.getHeaders());
                                if (!uploadFileRsp.isSuccessful()) {
                                    log.error("Fail to upload file automatically, filename: {}, uploadUrl: {}, msg: {}",
                                            featureIdAndFileMap.get(filename).getName(),
                                            uriInfo.getUploadUrl(),
                                            uploadFileRsp.message());
                                }
                            }
                        } catch (Exception e) {
                            log.error("Fail to upload file, uri info: {}, exception: {}", uriInfoMap, e);
                        }
                    });
                    rsp.setData(uriInfos);
                }
                return rsp;
            } catch (Exception e)
            {
                log.info("failed to decode response: " + response, e);
                throw new EnvisionException(CLIENT_ERROR);
            }
        } catch (SocketException e)
        {
            log.info("failed to execute request due to socket error {}", e.getMessage());
            throw new EnvisionException(SOCKET_ERROR);
        } catch (Exception e)
        {
            log.warn("failed to execute request", e);
            throw new EnvisionException(CLIENT_ERROR);
        }
    }
    
    /**
     * OkHttp Call is executed asynchronously, and Response is handled through the Callback method
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
                    callback.onFailure(new IOException("failed to construct response", e));
                    return;
                }
                try
                {
                    handleAdditionalMsg(httpResponse.headers());
                    DecodeResult result = response.decode(request.getAnswerTopic(), httpResponse.body().bytes());
                    response = result.getArrivedMsg();
                } catch (Exception e)
                {
                    callback.onFailure(new IOException("failed to decode response: " + response, e));
                    return;
                }
                callback.onResponse(response);
            }
        });
    }


    /**
     * Generate a request message for okHttp
     * @param <T>
     * @param request
     * @return
     * @throws EnvisionException
     */
    private <T extends BaseMqttResponse> Call generatePlainPublishCall(BaseMqttRequest<T> request) throws EnvisionException
    {
        checkAuth();
        // complete the request message 
        fillRequest(request);

        RequestBody body;
        if (!isUpRaw(request))
        {
            // Prepare request message
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
    
    
    private <T extends BaseMqttResponse> Call generateMultipartPublishCall(
            BaseMqttRequest<T> request, IProgressListener progressListener)
    throws EnvisionException, IOException
    {
        checkAuth();

        // complete the request message 
        fillRequest(request);
        
        // Prepare a Multipart request message
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(ENOS_MESSAGE, new String(request.encode(), UTF_8));

        if (!isUseLark()) {
            for (UploadFileInfo uploadFile : request.getFiles()) {
                builder.addPart(FileFormData.createFormData(uploadFile));
            }
        }
        
        RequestBody body;
        if (progressListener == null)
        {
            body = builder.build();
        }
        else
        {
            body = new ProgressRequestWrapper(builder.build(), progressListener);
        }
        
        Request httpRequest = new Request.Builder()
                .url(brokerUrl + "/multipart" + request.getMessageTopic() + "?sessionId=" + sessionId + useLarkPart())
                .post(body)
                .build();

        return okHttpClient.newCall(httpRequest);
    }

    private String useLarkPart() {
        return this.isUseLark()? "&useLark=" + true : "";
    }
    
    /**
     * Generate an okHttp Call for HTTP request
     * @param <T>
     * @param request
     * @return
     * @throws EnvisionException
     * @throws IOException
     */
    private <T extends BaseMqttResponse> Call generatePublishCall(BaseMqttRequest<T> request,
            IProgressListener progressListener) throws EnvisionException, IOException
    {
        if (request.getFiles() != null && !request.getFiles().isEmpty())
        {
            //Request including file points
            return generateMultipartPublishCall(request, progressListener);
        }
        else
        {
            //Request without file points
            return generatePlainPublishCall(request);
        }
    }

    /**
     * download file of specific fileUri and category
     * @param fileUri
     * @param category specify feature or ota file
     * @return
     * @throws EnvisionException
     */
    public InputStream downloadFile(String fileUri, FileCategory category) throws EnvisionException, IOException {

        if (fileUri.startsWith(FileScheme.ENOS_LARK_URI_SCHEME)) {
            String downloadUrl = getDownloadUrl(fileUri, category);
            Response response =  FileUtil.downloadFile(downloadUrl);
            Preconditions.checkArgument(response.isSuccessful(),
                    "fail to download file, downloadUrl: %s, msg: %s",
                    downloadUrl, response.message());
            Preconditions.checkNotNull(response.body(),
                    "response body is null, downloadUrl: %s", downloadUrl);
            return response.body().byteStream();
        }

        Call call = generateDownloadCall(fileUri, category);

        Response httpResponse;
        try {
            httpResponse = call.execute();

            if (!httpResponse.isSuccessful()) {
                throw new EnvisionException(httpResponse.code(), httpResponse.message());
            }

            try {
                Preconditions.checkNotNull(httpResponse);
                Preconditions.checkNotNull(httpResponse.body());

                return httpResponse.body().byteStream();
            } catch (Exception e) {
                log.info("failed to get response: " + httpResponse, e);
                throw new EnvisionException(CLIENT_ERROR);
            }
        } catch (SocketException e)
        {
            log.info("failed to execute request due to socket error {}", e.getMessage());
            throw new EnvisionException(SOCKET_ERROR, e.getMessage());
        } catch (EnvisionException e) {
            throw e;
        } catch (Exception e) {
            log.warn("failed to execute request", e);
            throw new EnvisionException(CLIENT_ERROR);
        }
    }

    public void downloadFileAsync(String fileUri, FileCategory category, IFileCallback callback) throws EnvisionException {
        Call call;
        if (fileUri.startsWith(FileScheme.ENOS_LARK_URI_SCHEME)) {
            call = generateGetDownloadUrlCall(fileUri, category);
        } else {
            call = generateDownloadCall(fileUri, category);
        }

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onFailure(new EnvisionException(response.code(), response.message()));
                }

                try {
                    Preconditions.checkNotNull(response);
                    Preconditions.checkNotNull(response.body());
                    if (response.isSuccessful() && fileUri.startsWith(FileScheme.ENOS_LARK_URI_SCHEME)) {
                        FileDownloadResponse fileDownloadResponse = GsonUtil.fromJson(
                                response.body().string(), FileDownloadResponse.class);
                        String fileDownloadUrl = fileDownloadResponse.getData();
                        response = FileUtil.downloadFile(fileDownloadUrl);
                    }
                    if (response.isSuccessful() && response.body() != null) {
                        callback.onResponse(response.body().byteStream());
                    }
                } catch (Exception e) {
                    log.info("failed to get response: " + response, e);
                    callback.onFailure(new EnvisionException(CLIENT_ERROR));
                }
            }
        });
    }

    public String getDownloadUrl(String fileUri, FileCategory category) throws EnvisionException {
        Call call = generateGetDownloadUrlCall(fileUri, category);

        try {
            Response httpResponse = call.execute();

            Preconditions.checkNotNull(httpResponse);
            Preconditions.checkNotNull(httpResponse.body());

            FileDownloadResponse response = GsonUtil.fromJson(httpResponse.body().string(), FileDownloadResponse.class);
            if (!response.isSuccess()) {
                throw new EnvisionException(response.getCode(), response.getMessage());
            }
            return response.getData();
        } catch (SocketException e) {
            log.info("failed to execute request due to socket error {}", e.getMessage());
            throw new EnvisionException(SOCKET_ERROR, e.getMessage());
        } catch (EnvisionException e) {
            throw e;
        } catch (Exception e) {
            log.warn("failed to execute request", e);
            throw new EnvisionException(CLIENT_ERROR);
        }
    }

    /**
     * delete file of specific fileUri
     * @param fileUri
     * @return FileDeleteResponse
     * @throws EnvisionException
     */
    public FileDeleteResponse deleteFile(String fileUri) throws EnvisionException {
        Call call = generateDeleteCall(fileUri);

        Response httpResponse;
        try {
            httpResponse = call.execute();
            if (!httpResponse.isSuccessful()) {
                throw new EnvisionException(httpResponse.code(), httpResponse.message());
            }

            try {
                Preconditions.checkNotNull(httpResponse);
                Preconditions.checkNotNull(httpResponse.body());
                byte[] payload = httpResponse.body().bytes();
                String msg = new String(payload, UTF_8);
                return GsonUtil.fromJson(msg, FileDeleteResponse.class);
            } catch (Exception e) {
                log.info("failed to decode response: " + httpResponse, e);
                throw new EnvisionException(CLIENT_ERROR);
            }
        } catch (SocketException e) {
            log.info("failed to execute request due to socket error {}", e.getMessage());
            throw new EnvisionException(SOCKET_ERROR, e.getMessage());
        } catch (EnvisionException e) {
            throw e;
        } catch (Exception e) {
            log.warn("failed to execute request", e);
            throw new EnvisionException(CLIENT_ERROR);
        }
    }

    public void deleteFileAsync(String fileUri, IResponseCallback<FileDeleteResponse> callback) throws EnvisionException {
        Call call = generateDeleteCall(fileUri);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    callback.onFailure(new EnvisionException(response.code(), response.message()));
                }
                try
                {
                    Preconditions.checkNotNull(response);
                    Preconditions.checkNotNull(response.body());
                    byte[] payload = response.body().bytes();
                    String msg = new String(payload, UTF_8);
                    callback.onResponse(GsonUtil.fromJson(msg, FileDeleteResponse.class));
                } catch (Exception e)
                {
                    log.info("failed to decode response: " + response, e);
                    callback.onFailure(new EnvisionException(CLIENT_ERROR));
                }
            }
        });

    }

    private Call generateGetDownloadUrlCall(String fileUri, FileCategory category) throws EnvisionException {
        checkAuth();

        StaticDeviceCredential staticDeviceCredential = (StaticDeviceCredential) credential;

        StringBuilder uriBuilder = new StringBuilder()
                .append(brokerUrl)
                .append("/multipart")
                .append(String.format(GET_DOWNLOAD_URL_PATH_FORMAT,
                        staticDeviceCredential.getProductKey(),
                        staticDeviceCredential.getDeviceKey()))
                .append("?sessionId=").append(sessionId)
                .append("&fileUri=").append(fileUri)
                .append("&category=").append(category.getName());

        Request httpRequest = new Request.Builder()
                .url(uriBuilder.toString())
                .get()
                .build();

        return okHttpClient.newCall(httpRequest);
    }

    private Call generateDownloadCall(String fileUri, FileCategory category) throws EnvisionException {
        checkAuth();

        StaticDeviceCredential staticDeviceCredential = (StaticDeviceCredential) credential;

        StringBuilder uriBuilder = new StringBuilder()
                .append(brokerUrl)
                .append("/multipart")
                .append(String.format(DOWNLOAD_PATH_FORMAT, staticDeviceCredential.getProductKey(),staticDeviceCredential.getDeviceKey()))
                .append("?sessionId=").append(sessionId)
                .append("&fileUri=").append(fileUri)
                .append("&category=").append(category.getName());

        Request httpRequest = new Request.Builder()
                .url(uriBuilder.toString())
                .get()
                .build();

        return okHttpClient.newCall(httpRequest);
    }

    private Call generateDeleteCall(String fileUri) throws EnvisionException {
        checkAuth();

        StaticDeviceCredential staticDeviceCredential = (StaticDeviceCredential) credential;

        StringBuilder uriBuilder = new StringBuilder()
                .append(brokerUrl)
                .append("/multipart")
                .append(String.format(DELETE_PATH_FORMAT, staticDeviceCredential.getProductKey(), staticDeviceCredential.getDeviceKey()))
                .append("?sessionId=").append(sessionId)
                .append("&fileUri=").append(fileUri);

        Request httpRequest = new Request.Builder()
                .url(uriBuilder.toString())
                .post(RequestBody.create(null, ""))
                .build();

        return okHttpClient.newCall(httpRequest);
    }

    /**
     * Publish a request to EnOS IOT HTTP broker
     * 
     * @param <T>
     *            Response
     * @param request
     * @param progressListener used to handle file uploading progress, {@code null} if not available
     * @return response
     * @throws EnvisionException
     * @throws IOException 
     */
    public <T extends BaseMqttResponse> T publish(BaseMqttRequest<T> request, IProgressListener progressListener) 
            throws EnvisionException, IOException
    {
        Call call = generatePublishCall(request, progressListener);
        return publishCall(call, request);
    }

    /**
     * Publish a request to EnOS IOT HTTP broker, asynchronously with a callback
     * 
     * @param <T>
     *            Response
     * @param request
     * @param callback
     * @param progressListener used to handle file uploading progress, {@code null} if not available
     * @throws EnvisionException
     * @throws IOException 
     */
    public <T extends BaseMqttResponse> void publish(BaseMqttRequest<T> request, 
            IResponseCallback<T> callback, IProgressListener progressListener)
            throws EnvisionException, IOException
    {
        Call call = generatePublishCall(request, progressListener);
        publishCallAsync(call, request, callback);
    }
}
