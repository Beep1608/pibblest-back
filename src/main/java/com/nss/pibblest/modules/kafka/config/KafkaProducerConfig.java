package com.nss.pibblest.modules.kafka.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.DelegatingByTypeSerializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaProducerConfig {
    @Bean
    public ProducerFactory<Object, Object> producerFactory (KafkaProperties kafkaProperties){
        Map<String, Object> props = kafkaProperties.buildProducerProperties();
        Map<Class<?>, Serializer<?>> delegates = new LinkedHashMap<>();

        delegates.put(byte[].class, new ByteArraySerializer());
        delegates.put(Object.class, new JacksonJsonSerializer<>());
       // DelegatingByTypeSerializer delegatingByTypeSerializer = new DelegatingByTypeSerializer(delegates);
        DefaultKafkaProducerFactory<Object, Object> defaultKafkaProducerFactory = new DefaultKafkaProducerFactory<>(props);
       // defaultKafkaProducerFactory.setKeySerializer(new SmartKafkaSerializer());
        defaultKafkaProducerFactory.setValueSerializer(new SmartKafkaSerializer());
        return defaultKafkaProducerFactory;
    }

    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate(ProducerFactory<Object, Object> producerFactory){
        return new KafkaTemplate<>(producerFactory);
    }
}
