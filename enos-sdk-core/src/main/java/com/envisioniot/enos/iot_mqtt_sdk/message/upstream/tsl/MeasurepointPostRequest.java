package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl;

import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.DeliveryTopicFormat;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.MethodConstants;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.envisioniot.enos.iot_mqtt_sdk.util.FileUtil;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.apache.commons.beanutils.BeanMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.FeatureType.MEASUREPOINT;

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
        final String LOCAL_FILE_SCHEMA = "local://";

        public Builder()
        {
            params.put("measurepoints", new HashMap<>());
            params.put("time", System.currentTimeMillis());
        }


        /**
         * Provide a structure measuring point value through the bean, and support file fields
         * @param key
         * @param bean
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
         *
         * @param key
         * @param value
         * @return
         */
        @SuppressWarnings("unchecked")
        public Builder addStructMeasurePoint(String key, Map<String, Object> value) {

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
        @SuppressWarnings("unchecked")
        public Builder addMeasurePoint(String key, Object value)
        {
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
        @SuppressWarnings("unchecked")
        public MeasurepointPostRequest build() {
            Map<String, Object> measurepoints = (Map<String, Object>) params.get("measurepoints");
            // Unified processing of measuring point files, replace with local: // xxx
            fileCheck(measurepoints);
            MeasurepointPostRequest request = super.build();
            request.setFiles(this.files);
            return request;
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
        private void fileCheck( Map<String, Object> measurepointMap) {
            for (Map.Entry<String, Object> entry : measurepointMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof File)
                {
                    // store value as file
                    String fileUri = LOCAL_FILE_SCHEMA + storeFile(key, (File) value);
                    measurepointMap.put(key, fileUri);
                }
                else if (value instanceof Map)
                {
                    HashMap<String, Object> replicaMap = Maps.newHashMap();
                    for (Map.Entry<String,Object> subEntry: ((Map<String,Object>) value).entrySet())
                    {
                        if (subEntry.getValue() instanceof File) {
                            // store sub-value as file
                            String fileUri = LOCAL_FILE_SCHEMA + storeFile(key, ((File) subEntry.getValue()));
                            replicaMap.put(subEntry.getKey(), fileUri);
                        } else {
                            replicaMap.put(subEntry.getKey(), subEntry.getValue());
                        }
                    }
                    measurepointMap.put(key, replicaMap);
                }
            }
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

    @Override
    protected Map<String, Object> getJsonPayload() {
        Map<String, Object> payload = super.getJsonPayload();
        if (this.getFiles() != null) {
            payload.put("files", CreateFilePayload());
        }
        return payload;
    }

    private Map<String, Object> CreateFilePayload() {
        Map<String, Object> disposition = Maps.newHashMap();
        for (UploadFileInfo fileInfo : this.getFiles()) {
            Map<String, String> map = Maps.newHashMap();
            map.put("featureId", fileInfo.getFeatureId());
            map.put("fileName", fileInfo.getFilename());
            map.put("fileLength", String.valueOf(fileInfo.getFile().length()));
            map.put("fileExt", getFileExt(fileInfo.getFilename()).get());

            try {
                HashCode md5 = Files.hash(fileInfo.getFile(), Hashing.md5());
                map.put("md5", md5.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            disposition.put(fileInfo.getFilename(), map);
        }
        return disposition;
    }

    private static Optional<String> getFileExt(String filename) {
        return FileUtil.getExtensionByStringHandling(filename)
                .transform(ext -> {
                    if (!ext.isEmpty())
                    {
                        return "." + ext;
                    }
                    else
                    {
                        return "";
                    }
                });
    }
}
