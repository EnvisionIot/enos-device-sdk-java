package com.envisioniot.enos.iot_http_sdk.file;

import lombok.*;

import java.io.Serializable;
import java.util.Map;

/**
 * @author mengyuantan
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UriInfo implements Serializable {
    String fileUri;
    String uploadUrl;
    String filename;
    Map<String, String> headers;
}
