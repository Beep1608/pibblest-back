package com.nss.pibblest.modules.owners.unit;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nss.pibblest.modules.owners.internal.core.OwnerService;
import com.nss.pibblest.modules.owners.internal.core.exceptions.OwnerAlreadyVerified;
import com.nss.pibblest.modules.owners.internal.core.exceptions.OwnerNotExists;
import com.nss.pibblest.modules.owners.internal.infrastructure.data.OwnerEntity;
import com.nss.pibblest.modules.owners.internal.infrastructure.data.OwnerRepository;
import com.nss.pibblest.modules.owners.internal.web.request.verifyOwner.VerifyOwnerRequest;
import com.nss.pibblest.modules.owners.internal.web.request.verifyOwner.VerifyOwnerResponse;
import com.nss.pibblest.modules.security.internal.core.exceptions.OneTimeTokenExpired;
import com.nss.pibblest.modules.security.internal.core.exceptions.OneTimeTokenInvalid;
import com.nss.pibblest.modules.security.internal.infrastructure.data.OneTimeTokenOwnerEntity;
import com.nss.pibblest.modules.security.internal.infrastructure.data.OneTimeTokenOwnerRepository;

@ExtendWith(MockitoExtension.class)
public class OwnerVerificationServiceTest {

    @Mock
    private OneTimeTokenOwnerRepository oneTimeTokenOwnerRepository;

    @Mock
    private OwnerRepository ownerRepository;


    @InjectMocks
    private OwnerService ownerService;


    private final String VALID_TOKEN = "token-secreto-123";
    private final UUID OWNER_ID = UUID.randomUUID();
    
    private VerifyOwnerRequest request;
    private OneTimeTokenOwnerEntity mockTokenEntity;
    private OwnerEntity mockOwnerEntity;


    @BeforeEach
    void setUp() {
        request = new VerifyOwnerRequest();
        request.setToken(VALID_TOKEN);

        mockTokenEntity = new OneTimeTokenOwnerEntity();
        mockTokenEntity.setOwnerId(OWNER_ID);
        mockTokenEntity.setTokenValue(VALID_TOKEN);
        mockTokenEntity.setUsed(false);

        mockTokenEntity.setExpiresAt(LocalDateTime.now().plusMinutes(5)); 

        mockOwnerEntity = new OwnerEntity();
        mockOwnerEntity.setId(OWNER_ID);
        mockOwnerEntity.setName("Empresa Test SA");
    }

    @Test
    @DisplayName("Debe verificar al owner exitosamente y retornar la respuesta")
    void testVerifyOwner_Success() {

        when(oneTimeTokenOwnerRepository.findByTokenValue(VALID_TOKEN))
                .thenReturn(Optional.of(mockTokenEntity));
        when(ownerRepository.findById(OWNER_ID))
                .thenReturn(Optional.of(mockOwnerEntity));

        VerifyOwnerResponse response = ownerService.verifyOwner(request).getBody();

        assertNotNull(response);
        assertEquals(OWNER_ID.toString(), response.getMessage());


        assertTrue(mockTokenEntity.isUsed(), "El token debe marcarse como usado");
        assertNotNull(mockOwnerEntity.getVerifiedAt(), "El owner debe tener fecha de verificacion");

        verify(ownerRepository, times(1)).save(mockOwnerEntity);
        verify(oneTimeTokenOwnerRepository, times(1)).save(mockTokenEntity);
    }

    @Test
    @DisplayName("Debe lanzar OneTimeTokenInvalid si el token no existe")
    void testVerifyOwner_TokenNotFound() {

        when(oneTimeTokenOwnerRepository.findByTokenValue(VALID_TOKEN))
                .thenReturn(Optional.empty());

        assertThrows(OneTimeTokenInvalid.class, () -> {
            ownerService.verifyOwner(request);
        });

        verify(ownerRepository, never()).findById(any());
        verify(ownerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar OneTimeTokenExpired si la fecha ya pasó")
    void testVerifyOwner_TokenExpired() {
       
        mockTokenEntity.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        
        when(oneTimeTokenOwnerRepository.findByTokenValue(VALID_TOKEN))
                .thenReturn(Optional.of(mockTokenEntity));

        assertThrows(OneTimeTokenExpired.class, () -> {
            ownerService.verifyOwner(request);
        });

        verify(ownerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar OwnerAlreadyVerified si el token ya fue consumido")
    void testVerifyOwner_TokenAlreadyUsed() {
  
        mockTokenEntity.setUsed(true);
        
        when(oneTimeTokenOwnerRepository.findByTokenValue(VALID_TOKEN))
                .thenReturn(Optional.of(mockTokenEntity));


        assertThrows(OwnerAlreadyVerified.class, () -> {
            ownerService.verifyOwner(request);
        });
        
        verify(ownerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar OwnerNotExists si el token es válido pero el owner no se encuentra")
    void testVerifyOwner_OwnerNotFound() {

        when(oneTimeTokenOwnerRepository.findByTokenValue(VALID_TOKEN))
                .thenReturn(Optional.of(mockTokenEntity));

        when(ownerRepository.findById(OWNER_ID))
                .thenReturn(Optional.empty()); 


        assertThrows(OwnerNotExists.class, () -> {
            ownerService.verifyOwner(request);
        });


        verify(oneTimeTokenOwnerRepository, never()).save(mockTokenEntity);
    }
}
