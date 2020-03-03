package com.envisioniot.enos.iot_mqtt_sdk;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.DecoderRegistry;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttArrivedMessage;
import com.envisioniot.enos.iot_mqtt_sdk.util.PackageScanUtil;

public class DecoderRegistryTest
{

    /**
     * check the integrity of decoder registry, all decoders are specified in registry 
     * initialization
     */
    @Test
    public void testIntegrity()
    {
        try
        {
            List<Class<?>> decoderClazzes = PackageScanUtil.scan(DecoderRegistry.DECODER_PACKAGE, IMqttArrivedMessage.class);

            assertThat(DecoderRegistry.getDecoderList(), containsInAnyOrder(
                    decoderClazzes.stream()
                    .map(cls -> instanceOf(cls))
                    .collect(Collectors.toList())));
        } catch (Exception e)
        {
            fail("exception in package scan");
        }
    }
}
