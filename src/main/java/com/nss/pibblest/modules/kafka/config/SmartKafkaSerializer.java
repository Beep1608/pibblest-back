package com.nss.pibblest.modules.kafka.config;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Map;

public class SmartKafkaSerializer implements Serializer<Object> {

    private final ByteArraySerializer byteArraySerializer = new ByteArraySerializer();
    private final JacksonJsonSerializer<Object> jsonSerializer = new JacksonJsonSerializer<>();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        byteArraySerializer.configure(configs, isKey);
        jsonSerializer.configure(configs, isKey);
    }

    @Override
    public byte[] serialize(String topic, Object data) {
        // Si por alguna razón Modulith manda bytes puros, los dejamos pasar tal cual
        if (data instanceof byte[]) {
            return byteArraySerializer.serialize(topic, (byte[]) data);
        }
        // Si manda cualquier otra cosa (StorePreviewDto, OwnerRegisteredEvent, etc.), lo pasamos a JSON
        return jsonSerializer.serialize(topic, data);
    }

    @Override
    public byte[] serialize(String topic, Headers headers, Object data) {
        if (data instanceof byte[]) {
            return byteArraySerializer.serialize(topic, headers, (byte[]) data);
        }
        return jsonSerializer.serialize(topic, headers, data);
    }

    @Override
    public void close() {
        byteArraySerializer.close();
        jsonSerializer.close();
    }
}