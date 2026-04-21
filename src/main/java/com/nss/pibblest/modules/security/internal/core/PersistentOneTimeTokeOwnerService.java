package com.nss.pibblest.modules.security.internal.core;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.ott.DefaultOneTimeToken;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.security.internal.infrastructure.data.OneTimeTokenOwnerEntity;
import com.nss.pibblest.modules.security.internal.infrastructure.data.OneTimeTokenOwnerRepository;

@Service
public class PersistentOneTimeTokeOwnerService implements OneTimeTokenService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final OneTimeTokenOwnerRepository oneTimeTokenOwnerRepository;

    public PersistentOneTimeTokeOwnerService(OneTimeTokenOwnerRepository oneTimeTokenOwnerRepository) {
        this.oneTimeTokenOwnerRepository = oneTimeTokenOwnerRepository;
    }

    @Override
    public OneTimeToken generate(GenerateOneTimeTokenRequest request) {
       
        String requestedUser = request.getUsername();

        if (requestedUser == null || requestedUser.trim().isEmpty()) {

            throw new IllegalArgumentException("El identificador del usuario no puede ser nulo o vacío.");
        }

        UUID ownerId;
        try {
        
            ownerId = UUID.fromString(requestedUser);
        } catch (IllegalArgumentException e) {
           
            throw new IllegalArgumentException(
                    "El identificador proporcionado no tiene un formato UUID válido: " + requestedUser, e);
        }

    
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String tokenValue = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireAt = now.plusMinutes(2);

        OneTimeTokenOwnerEntity entity = new OneTimeTokenOwnerEntity();
        entity.setTokenValue(tokenValue);
        entity.setCreatedAt(now);
        entity.setExpiresAt(expireAt);
        entity.setUsed(false);


        entity.setOwnerId(ownerId);

     
        try {
            oneTimeTokenOwnerRepository.save(entity);
        } catch (DataAccessException e) {

            System.err.println("Error crítico al guardar el token en la base de datos: " + e.getMessage());
            throw new RuntimeException("No se pudo generar el token de acceso en este momento. Intente más tarde.");
        }

        return new DefaultOneTimeToken(tokenValue, requestedUser, expireAt.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public @Nullable OneTimeToken consume(OneTimeTokenAuthenticationToken authenticationToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'consume'");
    }

}
