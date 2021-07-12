package com.envisioniot.enos.iot_http_sdk;

import com.envisioniot.enos.iot_http_sdk.auth.AuthRequestBody;
import com.envisioniot.enos.iot_http_sdk.auth.AuthResponseBody;
import com.envisioniot.enos.iot_http_sdk.file.*;
import com.envisioniot.enos.iot_http_sdk.progress.IProgressListener;
import com.envisioniot.enos.iot_http_sdk.progress.ProgressRequestWrapper;
import com.envisioniot.enos.iot_http_sdk.registry.MethodDecoderRegistry;
import com.envisioniot.enos.iot_http_sdk.ssl.OkHttpUtil;
import com.envisioniot.enos.iot_mqtt_sdk.core.IResponseCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignMethod;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignUtil;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.RangeFileBody;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMessageHandler;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttArrivedMessage;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttArrivedMessage.DecodeResult;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttDeliveryMessage;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.BaseMqttCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.BaseMqttReply;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.ModelUpRawRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.UploadFileInfo;
import com.envisioniot.enos.iot_mqtt_sdk.util.GsonUtil;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Monitor;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

import static com.envisioniot.enos.iot_http_sdk.HttpConnectionError.*;
import static com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.FormDataConstants.ENOS_MESSAGE;
import static com.google.common.net.MediaType.JSON_UTF_8;
import static com.google.common.net.MediaType.OCTET_STREAM;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class provides a http connection towards EnOS IoT HTTP Broker
 * <p>
 * The connection should be re-used for continuous sending / receiving of HTTP
 * messages.
 *
 * @author shenjieyuan
 */
@Slf4j
public class HttpConnection {
    public static final String VERSION = "1.1";

    public static final String DOWNLOAD_PATH_FORMAT = "/sys/%s/%s/file/download";
    public static final String GET_DOWNLOAD_URL_PATH_FORMAT = "/sys/%s/%s/file/download/url";
    public static final String DELETE_PATH_FORMAT = "/sys/%s/%s/file/delete";

    public static final String MEDIA_TYPE_JSON_UTF_8 = JSON_UTF_8.toString();
    public static final String MEDIA_TYPE_OCTET_STREAM = OCTET_STREAM.toString();

    private static final String CMD_PAYLOAD = "command-payload";
    private static final String TOPIC = "topic";

    private static final String RANGE = "Range";
    private final static String METHOD_KEY = "method";

    /**
     * Builder for http connection. A customized OkHttpClient can be provided, to
     * define specific connection pool, proxy etc. Find more at
     * {@link #okHttpClient}
     *
     * @author shenjieyuan
     */
    @Data
    public static class Builder {
        @NonNull
        private String brokerUrl;

        @NonNull
        private ICredential credential;

        private SessionConfiguration sessionConfiguration;

        private OkHttpClient okHttpClient;

        private boolean useLark = false;

        private boolean autoUpload = true;

        public HttpConnection build() throws EnvisionException {
            HttpConnection instance = new HttpConnection();

            Preconditions.checkNotNull(brokerUrl);
            instance.brokerUrl = brokerUrl;

            Preconditions.checkNotNull(credential);
            instance.credential = credential;

            if (sessionConfiguration == null) {
                sessionConfiguration = SessionConfiguration.builder().build();
            }
            instance.sessionConfiguration = sessionConfiguration;

            // allocate client
            if (okHttpClient == null) {
                okHttpClient = OkHttpUtil.generateHttpsClient(sessionConfiguration.isSslSecured(), sessionConfiguration.isEccConnect(),
                        sessionConfiguration.getJksPath(), sessionConfiguration.getJksPassword());
            }
            instance.okHttpClient = okHttpClient;

            CompletableFuture.runAsync(() ->
            {
                try {
                    instance.auth();
                } catch (EnvisionException e) {
                    // do nothing, already handled
                }
            });

            instance.setUseLark(this.useLark);
            instance.setAutoUpload(this.autoUpload);

            return instance;
        }

        /**
         * Fluent API to set session configuration
         *
         * @param configuration
         * @return
         */
        public Builder sessionConfiguration(SessionConfiguration configuration) {
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

    @Getter
    @Setter
    private boolean autoUpload = true;

    @Getter
    @Setter
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

    @Getter
    @Setter
    private AtomicInteger requestId = new AtomicInteger(0);

    private Map<Class<? extends IMqttArrivedMessage>, IMessageHandler<?, ?>> arrivedMsgHandlerMap = new ConcurrentHashMap<>();

    public void setArrivedMsgHandler(Class<? extends IMqttArrivedMessage> arrivedMsgCls, IMessageHandler<?, ?> handler) {
        arrivedMsgHandlerMap.put(arrivedMsgCls, handler);
    }

    public void removeArrivedMsgHandler(Class<? extends IMqttArrivedMessage> arrivedMsgCls) {
        arrivedMsgHandlerMap.remove(arrivedMsgCls);
    }

    @SuppressWarnings("unchecked")
    public void handleAdditionalMsg(Response response) {
        Headers headers = response.headers();

        String msg = headers.get(CMD_PAYLOAD);

        String topic = headers.get(TOPIC);

        if (msg == null || topic == null) {
            return;
        }

        val decodes = MethodDecoderRegistry.getDecodeList();
        JsonObject object = new JsonParser().parse(msg).getAsJsonObject();
        final String method = object.get(METHOD_KEY).getAsString();
        IMqttArrivedMessage selectedDecode = null;
        List<String> pathList = Lists.newArrayList();
        for (IMqttArrivedMessage decode : decodes) {
            final boolean matched = Optional.ofNullable(decode.getMethodPattern())
                    .map(pattern -> pattern.matcher(method))
                    .map(Matcher::matches)
                    .orElse(false);

            if (matched) {
                selectedDecode = decode;
                val pattern = decode.getMatchTopicPattern();
                val matcher = pattern.matcher(topic);
                if (matcher.matches()) {
                    String[] groups = new String[matcher.groupCount()];
                    for (int i = 0; i < matcher.groupCount(); i++) {
                        groups[i] = matcher.group(i + 1);
                    }
                    pathList = Lists.newArrayList(groups);
                }
            }
        }

        if (selectedDecode == null) {
            return;
        }

        BaseMqttCommand<?> command = GsonUtil.fromJson(msg, selectedDecode.getClass());

        final IMessageHandler<BaseMqttCommand<?>, IMqttDeliveryMessage> handler =
                (IMessageHandler<BaseMqttCommand<?>, IMqttDeliveryMessage>) arrivedMsgHandlerMap.get(command.getClass());
        if (handler == null) {
            return;
        }
        try {
            IMqttDeliveryMessage deliveryMsg = handler.onMessage(command, pathList);
            replyMsg(command, pathList, deliveryMsg);
        } catch (Exception e) {
            log.warn("Failed to run : " + msg, e);
        }
    }

    private void replyMsg(BaseMqttCommand<?> msg, List<String> pathList, IMqttDeliveryMessage deliveryMsg) throws IOException, EnvisionException {
        if (deliveryMsg instanceof BaseMqttReply) {
            deliveryMsg.setMessageId(msg.getMessageId());
            ((BaseMqttReply) deliveryMsg).setTopicArgs(pathList);
            publish(deliveryMsg, null);
        }
    }

    /**
     * Generate sign for auth request, only use SHA-256 method
     *
     * @return
     */
    private String sign() {
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
    public AuthResponseBody auth() throws EnvisionException {
        if (authMonitor.tryEnter()) {
            // Execute auth request
            try {
                // clean up old auth response
                lastAuthResponse = null;
                sessionId = null;

                RequestBody body = RequestBody.create(MediaType.parse(MEDIA_TYPE_JSON_UTF_8), new Gson().toJson(
                        new AuthRequestBody(SignMethod.SHA256.getName(), sessionConfiguration.getLifetime(),
                                sign(), clientInfo(), sessionConfiguration.getAcceptCommandTypes())));

                Request request = new Request.Builder().url(brokerUrl + credential.getAuthPath()).post(body).build();

                Call call = okHttpClient.newCall(request);
                Response response = call.execute();

                if (response.isSuccessful()) {
                    lastAuthResponse = new Gson().fromJson(response.body().charStream(), AuthResponseBody.class);
                    // update sessionId
                    if (!lastAuthResponse.isSuccess()) {
                        log.warn(lastAuthResponse.toString());
                        throw new EnvisionException(lastAuthResponse.getCode(), lastAuthResponse.getMessage());
                    }

                    sessionId = lastAuthResponse.getData().getSessionId();

                    // save lastPostTimestamp
                    lastPublishTimestamp = System.currentTimeMillis();

                    log.info("auth success, store sessionId = " + sessionId);

                    authMonitor.leave();
                    handleAdditionalMsg(response);
                    return lastAuthResponse;
                } else {
                    // auth failed
                    AuthResponseBody failure = new AuthResponseBody();
                    failure.setCode(response.code());
                    failure.setMessage(response.message());

                    log.info("auth failed. " + failure);

                    return failure;
                }
            } catch (IOException e) {
                log.warn("failed to execut auth request", e);

                throw new EnvisionException(e, CLIENT_ERROR);
            } finally {
                if (authMonitor.isOccupied()) {
                    authMonitor.leave();
                }
            }
        } else if (authMonitor.enter(10L, TimeUnit.SECONDS)) {
            // Wait at most 10 seconds and try to get Auth Response
            try {
                if (lastAuthResponse != null) {
                    return lastAuthResponse;
                }
            } finally {
                authMonitor.leave();
            }
        }
        throw new EnvisionException(UNSUCCESSFUL_AUTH);
        // Unable to get auth response
    }

    private void checkAuth() throws EnvisionException {
        // If there is no sessionId, you need to log in first to obtain the sessionId
        if (Strings.isNullOrEmpty(sessionId) ||
                System.currentTimeMillis() - lastPublishTimestamp > sessionConfiguration.getLifetime()) {
            auth();
            if (Strings.isNullOrEmpty(sessionId)) {
                throw new EnvisionException(UNSUCCESSFUL_AUTH);
            }
        }
    }

    /**
     * complete a Request message
     */
    private void fillRequest(IMqttDeliveryMessage request) {
        // generate a message id is not generated yet
        if (Strings.isNullOrEmpty(request.getMessageId())) {
            request.setMessageId(String.valueOf(requestId.incrementAndGet()));
        }

        // use credential pk / dk as request pk / dk
        if (Strings.isNullOrEmpty(request.getProductKey()) && Strings.isNullOrEmpty(request.getDeviceKey())) {
            request.setProductKey(((StaticDeviceCredential) credential).getProductKey());
            request.setDeviceKey(((StaticDeviceCredential) credential).getDeviceKey());
        }

        if (request instanceof BaseMqttRequest) {
            // Also populate request version for IMqttRequest
            ((BaseMqttRequest<?>) request).setVersion(VERSION);

            // fill pk / dk in upload files
            if (((BaseMqttRequest<?>) request).getFiles() != null) {
                ((BaseMqttRequest<?>) request).getFiles().stream()
                        .forEach(fileInfo -> {
                            fileInfo.setProductKey(((StaticDeviceCredential) credential).getProductKey());
                            fileInfo.setDeviceKey(((StaticDeviceCredential) credential).getDeviceKey());
                        });
            }
        }
    }

    /**
     * Check if the request is raw request
     *
     * @param request
     * @return
     */
    private boolean isUpRaw(IMqttDeliveryMessage request) {
        return request instanceof ModelUpRawRequest;
    }

    /**
     * OkHttp Call to get Response
     *
     * @param <T>
     * @param call
     * @param request
     * @return
     * @throws EnvisionException
     */
    private <T extends BaseMqttResponse> T publishCall(Call call, IMqttDeliveryMessage request) throws EnvisionException {
        try (Response httpResponse = call.execute()) {
            if (request instanceof BaseMqttRequest) {
                T response = ((BaseMqttRequest<T>) request).getAnswerType().newInstance();
                try {
                    handleAdditionalMsg(httpResponse);
                    DecodeResult result = response.decode(((BaseMqttRequest<T>) request).getAnswerTopic(),
                            httpResponse.body() == null ? null : httpResponse.body().bytes());
                    T rsp = result.getArrivedMsg();
                    if (((BaseMqttRequest<T>) request).getFiles() != null && this.isUseLark()) {
                        List<Map> uriInfos = rsp.getData();
                        List<UploadFileInfo> fileInfos = ((BaseMqttRequest<T>) request).getFiles();

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
                } catch (Exception e) {
                    log.info("failed to decode response: " + response, e);
                    throw new EnvisionException(CLIENT_ERROR);
                }
            } else {
                Preconditions.checkState(httpResponse.isSuccessful(),
                        "fail to send reply message, error: %s", httpResponse.message());

                return null;
            }
        } catch (SocketException e) {
            log.info("failed to execute request due to socket error {}", e.getMessage());
            throw new EnvisionException(SOCKET_ERROR);
        } catch (Exception e) {
            log.warn("failed to execute request", e);
            throw new EnvisionException(CLIENT_ERROR);
        }
    }

    /**
     * OkHttp Call is executed asynchronously, and Response is handled through the Callback method
     *
     * @param <T>
     * @param call
     * @param request
     * @param callback
     */
    private <T extends BaseMqttResponse> void publishCallAsync(Call call, BaseMqttRequest<T> request, IResponseCallback<T> callback) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(e);
            }

            @Override
            public void onResponse(Call call, Response httpResponse) throws IOException {
                T response;
                try {
                    response = request.getAnswerType().newInstance();
                } catch (Exception e) {
                    callback.onFailure(new IOException("failed to construct response", e));
                    return;
                }
                try {
                    handleAdditionalMsg(httpResponse);
                    DecodeResult result = response.decode(request.getAnswerTopic(), httpResponse.body().bytes());
                    response = result.getArrivedMsg();
                } catch (Exception e) {
                    callback.onFailure(new IOException("failed to decode response: " + response, e));
                    return;
                }
                callback.onResponse(response);
            }
        });
    }


    /**
     * Generate a request message for okHttp
     *
     * @param <T>
     * @param request
     * @return
     * @throws EnvisionException
     */
    private <T extends BaseMqttResponse> Call generatePlainPublishCall(IMqttDeliveryMessage request) throws EnvisionException {
        checkAuth();
        // complete the request message
        fillRequest(request);

        RequestBody body;
        if (!isUpRaw(request)) {
            // Prepare request message
            body = RequestBody.create(MediaType.parse(MEDIA_TYPE_JSON_UTF_8), new String(request.encode(), UTF_8));
        } else {
            body = RequestBody.create(MediaType.parse(MEDIA_TYPE_OCTET_STREAM), request.encode());
        }

        Request httpRequest = new Request.Builder()
                .url(brokerUrl + "/topic" + request.getMessageTopic() + "?sessionId=" + sessionId).post(body).build();

        return okHttpClient.newCall(httpRequest);
    }


    private <T extends BaseMqttResponse> Call generateMultipartPublishCall(
            BaseMqttRequest<T> request, IProgressListener progressListener)
            throws EnvisionException, IOException {
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
        if (progressListener == null) {
            body = builder.build();
        } else {
            body = new ProgressRequestWrapper(builder.build(), progressListener);
        }

        Request httpRequest = new Request.Builder()
                .url(brokerUrl + "/multipart" + request.getMessageTopic() + "?sessionId=" + sessionId + useLarkPart())
                .post(body)
                .build();

        return okHttpClient.newCall(httpRequest);
    }

    private String useLarkPart() {
        return this.isUseLark() ? "&useLark=" + true : "";
    }

    /**
     * Generate an okHttp Call for HTTP request
     *
     * @param request
     * @return
     * @throws EnvisionException
     * @throws IOException
     */
    private Call generatePublishCall(IMqttDeliveryMessage request,
                                     IProgressListener progressListener) throws EnvisionException, IOException {
        if (request instanceof BaseMqttRequest &&
                ((BaseMqttRequest<?>) request).getFiles() != null &&
                !((BaseMqttRequest<?>) request).getFiles().isEmpty()
        ) {
            //Request including file points
            return generateMultipartPublishCall(((BaseMqttRequest<?>) request), progressListener);
        } else {
            //Request without file points
            return generatePlainPublishCall(request);
        }
    }

    public RangeFileBody downloadFile(String fileUri, FileCategory category, Long startRange, Long endRange) throws EnvisionException {
        RangeFileBody.RangeFileBodyBuilder builder = RangeFileBody.builder();

        Call call = generateDownloadCall(fileUri, category, startRange, endRange);

        Response httpResponse;
        try {
            httpResponse = call.execute();

            if (!httpResponse.isSuccessful()) {
                throw new EnvisionException(httpResponse.code(), httpResponse.message());
            }

            try {
                Preconditions.checkNotNull(httpResponse);
                Preconditions.checkNotNull(httpResponse.body());

                return builder.contentLength(Integer.parseInt(httpResponse.headers().get("Content-length")))
                        .contentRange(httpResponse.headers().get("Content-Range"))
                        .acceptRanges(httpResponse.headers().get("Accept-Ranges"))
                        .data(httpResponse.body().byteStream())
                        .build();
            } catch (Exception e) {
                log.info("failed to get response: " + httpResponse, e);
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

    /**
     * download file of specific fileUri and category
     *
     * @param fileUri
     * @param category specify feature or ota file
     * @return
     * @throws EnvisionException
     */
    public InputStream downloadFile(String fileUri, FileCategory category) throws EnvisionException, IOException {

        Call call = generateDownloadCall(fileUri, category, null, null);

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

    public void downloadFileAsync(String fileUri, FileCategory category, IFileCallback callback) throws EnvisionException {
        downloadFileAsync(fileUri, category, null, null, callback);
    }

    public void downloadFileAsync(String fileUri, FileCategory category, Long startRange, Long endRange, IFileCallback callback) throws EnvisionException {
        Call call = generateDownloadCall(fileUri, category, startRange, endRange);

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
                    if (response.isSuccessful() && response.body() != null) {
                        callback.onResponse(response.body().byteStream());

                        if (response.code() == 206) {
                            RangeFileBody.RangeFileBodyBuilder builder = RangeFileBody.builder();
                            RangeFileBody rangeFileBody = builder.contentLength(Integer.parseInt(response.headers().get("Content-length")))
                                    .contentRange(response.headers().get("Content-Range"))
                                    .acceptRanges(response.headers().get("Accept-Ranges"))
                                    .data(response.body().byteStream())
                                    .build();
                            callback.onRangeResponse(rangeFileBody);
                        }
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
     *
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
                try {
                    Preconditions.checkNotNull(response);
                    Preconditions.checkNotNull(response.body());
                    byte[] payload = response.body().bytes();
                    String msg = new String(payload, UTF_8);
                    callback.onResponse(GsonUtil.fromJson(msg, FileDeleteResponse.class));
                } catch (Exception e) {
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

    private Call generateDownloadCall(String fileUri, FileCategory category, Long startRange, Long endRange) throws EnvisionException {
        checkAuth();

        StaticDeviceCredential staticDeviceCredential = (StaticDeviceCredential) credential;

        StringBuilder uriBuilder = new StringBuilder()
                .append(brokerUrl)
                .append("/multipart")
                .append(String.format(DOWNLOAD_PATH_FORMAT, staticDeviceCredential.getProductKey(), staticDeviceCredential.getDeviceKey()))
                .append("?sessionId=").append(sessionId)
                .append("&fileUri=").append(fileUri)
                .append("&category=").append(category.getName());

        Request.Builder builder = new Request.Builder();

        if (startRange != null || endRange != null) {
            StringBuilder rangeBuilder = new StringBuilder()
                    .append("bytes=");
            if (startRange != null) {
                rangeBuilder.append(startRange);
            }
            rangeBuilder.append("-");
            if (endRange != null) {
                rangeBuilder.append(endRange);
            }
            builder.addHeader(RANGE, rangeBuilder.toString());
        }

        Request httpRequest = builder
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
     * @param <T>              Response
     * @param request
     * @param progressListener used to handle file uploading progress, {@code null} if not available
     * @return response
     * @throws EnvisionException
     * @throws IOException
     */
    public <T extends BaseMqttResponse> T publish(IMqttDeliveryMessage request, IProgressListener progressListener)
            throws EnvisionException, IOException {
        Call call = generatePublishCall(request, progressListener);
        return publishCall(call, request);
    }

    /**
     * Publish a request to EnOS IOT HTTP broker, asynchronously with a callback
     *
     * @param <T>              Response
     * @param request
     * @param callback
     * @param progressListener used to handle file uploading progress, {@code null} if not available
     * @throws EnvisionException
     * @throws IOException
     */
    public <T extends BaseMqttResponse> void publish(BaseMqttRequest<T> request,
                                                     IResponseCallback<T> callback, IProgressListener progressListener)
            throws EnvisionException, IOException {
        Call call = generatePublishCall(request, progressListener);
        publishCallAsync(call, request, callback);
    }
}
