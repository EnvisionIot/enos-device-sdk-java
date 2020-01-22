package com.envisioniot.enos.iot_mqtt_sdk.core.internals;

import java.util.Arrays;
import java.util.Map;

public class SignUtil {

    public static final SignMethod DEFAULT_SIGN_METHOD = SignMethod.SHA256;

    public static String sign(String secret, Map<String, String> params, String signMethod)
    {
        return sign(secret, params, SignMethod.getSignMethod(signMethod));
    }

    public static String sign(String secret, Map<String, String> params, SignMethod signMethod)
    {
        StringBuilder stringBuilder = new StringBuilder();
        if(params != null) {
            // Sorting parameter names by dictionary
            String[] keyArray = params.keySet().toArray(new String[0]);
            Arrays.sort(keyArray);
            for (String key : keyArray)
            {
                stringBuilder.append(key).append(params.get(key));
            }
        }
        stringBuilder.append(secret);
        return signMethod.sign(stringBuilder.toString());
    }
}
