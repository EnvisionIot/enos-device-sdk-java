package com.envisioniot.enos.iot_http_sdk.progress;

import java.io.IOException;

import lombok.Data;
import lombok.EqualsAndHashCode;
import okio.Buffer;
import okio.ForwardingSink;
import okio.Sink;

/**
 * 一个可以统计数据流量的Sink继承类
 * @author shenjieyuan
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CountingSink extends ForwardingSink
{
    private long bytesWritten = 0L;
    private ProgressListener listener;
    private long contentLength = 0L;

    public CountingSink(Sink delegate)
    {
        super(delegate);
    }

    public CountingSink(Sink delegate, ProgressListener listener, long contentLength)
    {
        super(delegate);
        this.listener = listener;
        this.contentLength = contentLength;
    }

    @Override
    public void write(Buffer source, long byteCount) throws IOException
    {
        super.write(source, byteCount);

        bytesWritten += byteCount;
        if (listener != null)
        {
            listener.onRequestProgress(bytesWritten, contentLength);
        }
    }
}