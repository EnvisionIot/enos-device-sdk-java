package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl;

import java.io.File;

import lombok.Data;

/**
 * This class defines files to be uploaded 
 * @author shenjieyuan
 */
@Data
public class UploadFileInfo
{
    String filename;

    File file;

    String featureType;
    
    String featureId;
}
