package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.ArrivedTopicPattern;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;
import com.google.common.base.Preconditions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Note that user MUST check {@link SubDeviceLoginBatchResponse#hasSevereError()}
 * before calling {@link SubDeviceLoginBatchResponse#getSuccessResults()} and
 * {@link SubDeviceLoginBatchResponse#getFailureResults()}. Otherwise, it throws.
 *
 * @author jian.zhang4
 */
public class SubDeviceLoginBatchResponse extends BaseMqttResponse {
    private static final long serialVersionUID = -1L;

    @SuppressWarnings("unused")
    private static final int HORRIBLE_ERROR_CODE = 500;

    private static Pattern pattern = Pattern.compile(ArrivedTopicPattern.SUB_DEVICE_LOGIN_BATCH_REPLY);

    private List<LoginSuccessResult> successResults;
    private List<LoginFailureResult> failureResults;

    public boolean hasSevereError() {
        return !isSuccess() && getSuccessResults().isEmpty() && getFailureResults().isEmpty();
    }

    /**
     * It throws if batch login request has format error
     * @return all logined sub-devices
     */
    public List<LoginSuccessResult> getSuccessResults() {
        if (successResults != null) {
            return successResults;
        }

        successResults = getResults("loginedSubDevices", subDev ->
                new LoginSuccessResult(
                        subDev.get("productKey"),
                        subDev.get("deviceKey"),
                        subDev.get("assetId"),
                        subDev.get("deviceSecret"))
        );

        return successResults;
    }

    /**
     * It throws if batch login request has format error
     * @return all sub-devices that failed to login
     */
    public List<LoginFailureResult> getFailureResults() {
        if (failureResults != null) {
            return failureResults;
        }

        failureResults = getResults("failedSubDevices", subDev ->
                new LoginFailureResult(
                        subDev.get("productKey"),
                        subDev.get("deviceKey"))
        );

        return failureResults;
    }

    private <T> List<T> getResults(String field, Function<Map<String, String>, T> conv) {
        List<T> results = new LinkedList<>();

        Map<String, Object> data = getData();
        if (data == null || !data.containsKey(field)) {
            return results;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, String>> subDevices = (List<Map<String, String>>) data.get(field);
        subDevices.forEach(subDev -> results.add(conv.apply(subDev)));
        return results;
    }

    @Override
    public Pattern getMatchTopicPattern() {
        return pattern;
    }

    public static class LoginSuccessResult {
        public final String productKey;
        public final String deviceKey;
        public final String assetId;

        // This is only available when dynamic activation is applied
        public final String deviceSecret;

        public LoginSuccessResult(String productKey, String deviceKey, String assetId, String deviceSecret) {
            Preconditions.checkNotNull(productKey);
            Preconditions.checkNotNull(deviceKey);
            Preconditions.checkNotNull(assetId);

            this.productKey = productKey;
            this.deviceKey = deviceKey;
            this.assetId = assetId;
            this.deviceSecret = deviceSecret;
        }

        @Override
        public String toString() {
            return "LoginSuccessResult{" +
                    "productKey='" + productKey + '\'' +
                    ", deviceKey='" + deviceKey + '\'' +
                    ", assetId='" + assetId + '\'' +
                    ", deviceSecret='" + deviceSecret + '\'' +
                    '}';
        }
    }

    public static class LoginFailureResult {
        public final String productKey;
        public final String deviceKey;

        public LoginFailureResult(String productKey, String deviceKey) {
            Preconditions.checkNotNull(productKey);
            Preconditions.checkNotNull(deviceKey);

            this.productKey = productKey;
            this.deviceKey = deviceKey;
        }

        @Override
        public String toString() {
            return "LoginFailureResult{" +
                    "productKey='" + productKey + '\'' +
                    ", deviceKey='" + deviceKey + '\'' +
                    '}';
        }
    }
}
