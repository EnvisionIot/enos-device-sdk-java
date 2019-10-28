package com.envisioniot.enos.iot_mqtt_sdk.util;

import java.io.File;
import java.security.SecureRandom;

import com.google.common.base.Optional;
import com.google.common.io.BaseEncoding;

public class FileUtil
{
    private static final SecureRandom rand = new SecureRandom();
    
    public static String nextFilename()
    {
        byte[] randomBytes = new byte[8];
        rand.nextBytes(randomBytes);
        return BaseEncoding.base64Url().omitPadding().encode(randomBytes);
    }
    
    public static Optional<String> getExtensionByStringHandling(String filename)
    {
        return Optional.fromNullable(filename).transform(
                f -> {
                    if (f.contains("."))
                    {
                        return f.substring(filename.lastIndexOf(".") + 1);
                    }
                    else
                    {
                        return "";
                    }
                });
    }
    
    /**
     * 生成一个可以用于传输的文件名
     * @param file
     * @return
     */
    public static String generateFileName(File file)
    {
        String origFilename = file.getName();
        String ext = getExtensionByStringHandling(origFilename).or("");
        
        String newFilename = nextFilename();
        if (!ext.isEmpty())
        {
            newFilename += ("." + ext);
        }
        
        return newFilename;
    }
}
