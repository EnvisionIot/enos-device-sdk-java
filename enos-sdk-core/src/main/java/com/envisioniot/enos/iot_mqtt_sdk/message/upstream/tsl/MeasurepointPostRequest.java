package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl;

import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.DeliveryTopicFormat;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.MethodConstants;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.envisioniot.enos.iot_mqtt_sdk.util.FileUtil;
import com.google.common.collect.Maps;


import static com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.FeatureType.MEASUREPOINT;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanMap;

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
        
        
        /**
         * Provide a structure measuring point value through the bean, and support file fields
         * @param key
         * @param value
         * @return
         * @throws EnvisionException 
         */
        public Builder addStructMeasurePoint(String key, Object bean) 
        {
            BeanMap beanMap = new BeanMap(bean);
            Map<String,Object> value = Maps.newHashMap();
            for (Entry<Object, Object> subEntry: beanMap.entrySet())
            {
                String subKey = (String) subEntry.getKey();
                // ignore class
                if (!subKey.equals("class"))
                {
                    if (subEntry.getValue() instanceof File)
                    {
                        // store sub-value as file
                        String filename = storeFile(key, ((File) subEntry.getValue()));
                        value.put(subKey, "local://" + filename);
                    }
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> values = (Map<String, Object>) params.get("measurepoints");
            values.put(key, value);
            return this;
        }
        
        /**
         * Provide a structure measurement point value through Map and support file fields
         * @param key
         * @param value
         * @return
         */
        public Builder addStructMeasurePoint(String key, Map<String,Object> value)
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

            @SuppressWarnings("unchecked")
            Map<String, Object> values = (Map<String, Object>) params.get("measurepoints");
            values.put(key, value);
            return this;
        }
        

        /**
         * Add a test point value, support file type
         * @param key
         * @param value
         * @return
         */
        public Builder addMeasurePoint(String key, Object value)
        {
            // TODO: A separate file object is currently generated for each file; you can consider optimizing and merging the same files later
            if (value instanceof File)
            {
                // store value as file
                value = "local://" + storeFile(key, (File) value);
            }
            
            @SuppressWarnings("unchecked")
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
