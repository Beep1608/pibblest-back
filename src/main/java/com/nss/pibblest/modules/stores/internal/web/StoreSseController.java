package com.nss.pibblest.modules.stores.internal.web;

import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.nss.pibblest.modules.stores.internal.core.StoreSseService;

@RestController
@RequestMapping("/api/stores/stream")
public class StoreSseController {
    
    private final StoreSseService storeSseService;

    public StoreSseController(StoreSseService storeSseService){
        this.storeSseService = storeSseService;
    }

    @GetMapping(value="/storePreview", produces= MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(){
        String userId = (String)(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return storeSseService.suscribe(userId);
    }
}
