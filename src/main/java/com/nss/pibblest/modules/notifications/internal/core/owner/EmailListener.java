package com.nss.pibblest.modules.notifications.internal.core.owner;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.nss.pibblest.modules.owners.api.events.OwnerRegisteredEvent;

@Component
public class EmailListener {


    @KafkaListener(topics="owners-registered-topic", groupId="notifications-group")
    public void onOwnerRegistered(OwnerRegisteredEvent event)
    {
        System.out.println("¡Evento recibido desde Kafka!");
        System.out.println("Enviando correo de bienvenida a: " + event.email());

    }
}
