package com.nss.pibblest.modules.tenant;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class SessionTrackerService {
    
    private static final int MAX_SESSIONS = 2;
    private static final long JWT_EXPIRATION_HOURS = 2;

    private final StringRedisTemplate redisTemplate;

    public SessionTrackerService(StringRedisTemplate redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    public void registerNewSession(String username, String tokenId){
        String redisKey = "active_sessions:"+username;

        redisTemplate.opsForList().rightPush(redisKey, tokenId);

        Long currentSessions = redisTemplate.opsForList().size(redisKey);

        while(currentSessions != null && currentSessions > MAX_SESSIONS){
            redisTemplate.opsForList().leftPop(redisKey);
            currentSessions = redisTemplate.opsForList().size(redisKey);
        }

        redisTemplate.expire(redisKey, JWT_EXPIRATION_HOURS, TimeUnit.HOURS);
    }

    public boolean isSessionValid(String username, String tokenId){
        String redisKey = "active_sessions:"+username;


        List<String> activeSessions = redisTemplate.opsForList().range(redisKey, 0, -1);
       

        return activeSessions != null && activeSessions.contains(tokenId);
    }


    /**
     * Verifica instantáneamente si el usuario tiene sesiones activas en Redis.
     * Si la llave existe, significa que tiene al menos un token registrado.
     */
    public boolean isUserOnline(String username) {
        String redisKey = "active_sessions:" + username;
        Boolean exists = redisTemplate.hasKey(redisKey);
        return exists != null && exists;
    }
}
