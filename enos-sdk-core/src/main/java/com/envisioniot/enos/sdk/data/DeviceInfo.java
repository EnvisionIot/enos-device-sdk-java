package com.envisioniot.enos.sdk.data;

import com.envisioniot.enos.iot_mqtt_sdk.util.StringUtil;
import lombok.Getter;

import java.util.Objects;

/**
 * @author :charlescai
 * @date :2020-02-20
 */
@Getter
public class DeviceInfo {
    private String productKey = null;
    private String deviceKey = null;
    private String assetId = null;

    public DeviceInfo setAssetId(String assetId) {
        this.assetId = assetId;
        return this;
    }

    public DeviceInfo setKey(String productKey, String deviceKey) {
        this.productKey = productKey;
        this.deviceKey = deviceKey;
        return this;
    }

    public static boolean check(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return false;
        } else if (StringUtil.isNotEmpty(deviceInfo.assetId)) {
            return true;
        } else {
            return StringUtil.isNotEmpty(deviceInfo.productKey) && StringUtil.isNotEmpty(deviceInfo.deviceKey);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((deviceKey == null) ? 0 : deviceKey.hashCode());
        result = prime * result + ((assetId == null) ? 0 : assetId.hashCode());
        result = prime * result + ((productKey == null) ? 0 : productKey.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceInfo argsInfo = (DeviceInfo) o;
        if (StringUtil.isNotEmpty(assetId) && StringUtil.isNotEmpty(argsInfo.assetId)) {
            return Objects.equals(assetId, argsInfo.assetId);
        }
        if (StringUtil.isNotEmpty(productKey) || StringUtil.isNotEmpty(argsInfo.productKey)) {
            if (!Objects.equals(productKey, argsInfo.productKey)) {
                return false;
            }
            if (StringUtil.isNotEmpty(deviceKey) || StringUtil.isNotEmpty(argsInfo.deviceKey)) {
                return Objects.equals(deviceKey, argsInfo.deviceKey);
            }
        }
        return Objects.equals(deviceKey, argsInfo.deviceKey);
    }

}
