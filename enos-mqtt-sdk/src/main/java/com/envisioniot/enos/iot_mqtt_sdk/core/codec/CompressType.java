package com.envisioniot.enos.iot_mqtt_sdk.core.codec;

/**
 * @author mengyuantan
 */

public enum CompressType {
    /**
     * don't compress payload
     */
    DEFAULT("default", new DefaultCompressor()),

    /**
     * use gzip to compress payload
     */
    GZIP("gzip", new GzipCompressor()),

    /**
     * use lz4 to compress payload
     */
    LZ4("lz4", new Lz4Compressor()),

    /**
     * use lzo to compress payload
     */
    LZO("lzo", new LzoCompressor());

    private String type;
    private ICompressor compressor;

    CompressType(String type, ICompressor compressor) {
        this.type = type;
        this.compressor = compressor;
    }

    public String getType() {
        return type;
    }

    public ICompressor getCompressor() {
        return compressor;
    }
}
