package com.envisioniot.enos.iot_http_sdk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.UploadFileInfo;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import lombok.NonNull;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.MultipartBody.Part;
import okhttp3.RequestBody;

/**
 * This class defines helper function to generate form-data for files
 * @author shenjieyuan
 */
public class FileFormData
{
    static StringBuilder appendQuotedString(StringBuilder target, String key)
    {
        target.append('"');
        for (int i = 0, len = key.length(); i < len; i++)
        {
            char ch = key.charAt(i);
            switch (ch)
            {
            case '\n':
                target.append("%0A");
                break;
            case '\r':
                target.append("%0D");
                break;
            case '"':
                target.append("%22");
                break;
            default:
                target.append(ch);
                break;
            }
        }
        target.append('"');
        return target;
    }
    
    
    @SuppressWarnings("unused")
    private static void anotherMd5(String[] args) throws IOException, NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = java.nio.file.Files.newInputStream(Paths.get("pom.xml"));
             DigestInputStream dis = new DigestInputStream(is, md)) 
        {
            ReadableByteChannel channel = Channels.newChannel(dis);
            
            ByteBuffer buffer = ByteBuffer.allocate(300);
            do
            {
                int result = channel.read(buffer);
                if (result > 0)
                {
                    buffer.flip();
                    System.out.print(StandardCharsets.UTF_8.decode(buffer).toString());
                    buffer.clear();
                }
                else
                {
                    break;
                }
            } while (true);

          /* Read decorated stream (dis) to EOF as normal... */
        }
        System.out.println();
        byte[] digest = md.digest();
        System.out.println(new String(Hex.encodeHexString(digest)));
    }
    
    
    public static String md5(File file) throws IOException
    {
        HashCode md5 = Files.hash(file, Hashing.md5());
        return md5.toString();
    }

    public static Part createFormData(@NonNull UploadFileInfo fileInfo) throws IOException
    {
        StringBuilder disposition = new StringBuilder("form-data; name=");
        appendQuotedString(disposition, fileInfo.getFilename());

        disposition.append("; filename=");
        appendQuotedString(disposition, fileInfo.getFilename());
        
        disposition.append("; md5=");
        appendQuotedString(disposition, md5(fileInfo.getFile()));

        disposition.append("; feature-type=");
        appendQuotedString(disposition, fileInfo.getFeatureType());

        disposition.append("; feature-id=");
        appendQuotedString(disposition, fileInfo.getFeatureId());

        Headers headers = new Headers.Builder()
                .addUnsafeNonAscii("Content-Disposition", disposition.toString())
                .build();

        return MultipartBody.Part.create(headers, 
                RequestBody.create(MediaType.parse(HttpConnection.MEDIA_TYPE_OCTET_STREAM), fileInfo.getFile()));
    }
}
