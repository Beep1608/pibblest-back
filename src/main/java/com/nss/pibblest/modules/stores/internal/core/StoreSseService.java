package com.nss.pibblest.modules.stores.internal.core;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.nss.pibblest.modules.stores.api.dtos.StorePreviewDto;

@Service
public class StoreSseService {

    private final Map<String, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter suscribe(String username) {
        SseEmitter emitter = new SseEmitter(1800000L);

        userEmitters.computeIfAbsent(username, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(username, emitter));
        emitter.onTimeout(() -> removeEmitter(username, emitter));
        emitter.onError(e -> removeEmitter(username, emitter));

        try{
            emitter.send(SseEmitter.event().name("INIT").data("SSE Conectado"));

        }catch(IOException e){
            removeEmitter(username, emitter);
        }

        return emitter;
    }

    public void broadcastStoreUpdate(String token, StorePreviewDto updateStore) {
        List<SseEmitter> emitters = userEmitters.get(token);

        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("STORE_UPDATE")
                            .data(updateStore));
                } catch (IOException e) {
                    emitter.complete();
                    removeEmitter(token, emitter);
                }
            }
        }
    }

    private void removeEmitter(String username, SseEmitter emitter) {
        List<SseEmitter> emitters = userEmitters.get(username);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(username);
            }
        }
    }

}
