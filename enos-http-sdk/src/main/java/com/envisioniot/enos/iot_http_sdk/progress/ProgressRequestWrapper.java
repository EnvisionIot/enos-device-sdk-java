package com.envisioniot.enos.iot_http_sdk.progress;

import java.io.IOException;

import lombok.EqualsAndHashCode;
import lombok.Value;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;

@Value
@EqualsAndHashCode(callSuper = true)
public class ProgressRequestWrapper extends RequestBody
{
    RequestBody delegate;
    IProgressListener listener;

    @Override
    public MediaType contentType()
    {
        return delegate.contentType();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException
    {
        BufferedSink bufferedSink;

        CountingSink countingSink = new CountingSink(sink, listener, delegate.contentLength());
        bufferedSink = Okio.buffer(countingSink);

        delegate.writeTo(bufferedSink);

        bufferedSink.flush();
    }
}
