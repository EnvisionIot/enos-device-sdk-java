package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.DeliveryTopicFormat;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.MethodConstants;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.envisioniot.enos.iot_mqtt_sdk.util.FileUtil;

import static com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.FeatureType.MEASUREPOINT;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * { "id": "123", "version": "1.0", "params": { "Power": { "value": "on",
 * "time": 1524448722000 }, "WF": { "value": 23.6, "time": 1524448722000 } },
 * "method": "thing.event.property.post" } ----------------------------
 * "params":{ "measurepoints":{ "Power":{ "value":"1.0, "quality": "9" },
 * "temp":1.02 , "branchCurr":[ "1.02","2.02","7.93" ] } "time":123456 }
 *
 * @author zhensheng.cai
 * @date 2018/7/10.
 */
public class MeasurepointPostRequest extends BaseMqttRequest<MeasurepointPostResponse>
{
    private static final long serialVersionUID = 4018722889739885894L;

    public static MeasurepointPostRequest.Builder builder()
    {
        return new Builder();
    }

    public static class Builder extends BaseMqttRequest.Builder<Builder, MeasurepointPostRequest>
    {
        private Map<String, Object> params = new HashMap<>();
        private List<UploadFileInfo> files;

        public Builder()
        {
            params.put("measurepoints", new HashMap<>());
            params.put("time", System.currentTimeMillis());
        }
        
        private String storeFile(String measurepointId, File file)
        {
            UploadFileInfo fileInfo = new UploadFileInfo();
            String filename = FileUtil.generateFileName(file);
            fileInfo.setFilename(filename);
            fileInfo.setFile(file);
            fileInfo.setFeatureType(MEASUREPOINT);
            fileInfo.setFeatureId(measurepointId);
            
            if (files == null)
            {
                files = new ArrayList<>();
            }
            files.add(fileInfo);
            return filename;
        }

        @SuppressWarnings("unchecked")
        public Builder addMeasurePoint(String key, Object value)
        {
            // TODO: 目前为每个文件都单独生成一个文件对象；后续可以考虑优化合并相同的文件
            if (value instanceof File)
            {
                // store value as file
                value = "local://" + storeFile(key, (File) value);
            }
            else if (value instanceof Map)
            {
                for (Entry<String,Object> subEntry: ((Map<String,Object>) value).entrySet())
                {
                    if (subEntry.getValue() instanceof File)
                    {
                        // store sub-value as file
                        String filename = storeFile(key, ((File) subEntry.getValue()));
                        ((Map<String,Object>) value).put(subEntry.getKey(), "local://" + filename);
                    }
                }
            }
            // TODO: 目前还不考虑 传入一个带有File Bean的对象。
            
            Map<String, Object> values = (Map<String, Object>) params.get("measurepoints");
            values.put(key, value);
            return this;
        }

        /**
         * @param key
         *            measurepoint identifier
         * @param value
         *            value of measurepoint
         * @param quality
         *            quality of measurepoint
         * @return builder
         */
        @SuppressWarnings("unchecked")
        public Builder addMeasreuPointWithQuality(String key, Object value, Object quality)
        {
            Map<String, Object> values = (Map<String, Object>) params.get("measurepoints");
            Map<String, Object> valueWithQuality = new HashMap<>();
            valueWithQuality.put("value", value);
            valueWithQuality.put("quality", quality);
            values.put(key, valueWithQuality);
            return this;
        }

        public Builder addMeasurePoints(Map<String, Object> value)
        {
            for (Entry<String,Object> entry: value.entrySet())
            {
                String mpKey = entry.getKey();
                Object mpValue = entry.getValue();
                addMeasurePoint(mpKey, mpValue);
            }
            return this;
        }

        public Builder setMeasurePoints(Map<String, Object> value)
        {
            params.put("measurepoints", value);
            return this;
        }

        public Builder setTimestamp(long timestamp)
        {
            params.put("time", timestamp);
            return this;
        }

        @Override
        protected String createMethod()
        {
            return MethodConstants.MEASUREPOINT_POST;
        }

        @Override
        protected Object createParams()
        {
            return params;
        }

        @Override
        protected MeasurepointPostRequest createRequestInstance()
        {
            return new MeasurepointPostRequest();
        }

        @Override
        public MeasurepointPostRequest build() {
            MeasurepointPostRequest request = super.build();
            request.setFiles(this.files);
            return request;
        }
    }

    private MeasurepointPostRequest()
    {
    }

    @Override
    public Class<MeasurepointPostResponse> getAnswerType()
    {
        return MeasurepointPostResponse.class;
    }

    @Override
    protected String _getPK_DK_FormatTopic()
    {
        return DeliveryTopicFormat.MEASUREPOINT_POST;
    }

}
