package com.nss.pibblest.modules.stores.internal.core.kafka.consumers;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.nss.pibblest.modules.stores.api.dtos.StorePreviewDto;
import com.nss.pibblest.modules.stores.internal.core.StoreSseService;

@Component
public class StoreKafkaConsumer {
    private final StoreSseService storeSseService;

    public StoreKafkaConsumer(StoreSseService storeSseService){
        this.storeSseService =storeSseService;
    }

    @KafkaListener(
        topics="store-realtime-updates",
        groupId="sse-node-#{T(java.util.UUID).randomUUID().toString()}"
    )
    public void consumeStoreUpdate(@Payload StorePreviewDto updateStore, @Header("token") String token){
        if(token != null){
            storeSseService.broadcastStoreUpdate(token, updateStore);
        }else{
            System.out.println("Vacioooo");
        }
        
    }
    
}
