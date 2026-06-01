package com.nss.pibblest.modules.owners.unit.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;

import com.nss.pibblest.modules.owners.api.events.OwnerGenerateVerifyToken;
import com.nss.pibblest.modules.owners.api.events.OwnerRegisteredEvent;
import com.nss.pibblest.modules.owners.internal.core.OwnerService;
import com.nss.pibblest.modules.owners.internal.core.exceptions.OwnerAlreadyExists;
import com.nss.pibblest.modules.owners.internal.core.exceptions.OwnerAlreadyVerified;
import com.nss.pibblest.modules.owners.internal.core.exceptions.OwnerNotExists;
import com.nss.pibblest.modules.owners.internal.infrastructure.data.OwnerEntity;
import com.nss.pibblest.modules.owners.internal.infrastructure.data.OwnerRepository;
import com.nss.pibblest.modules.owners.internal.mappers.OwnerMapper;
import com.nss.pibblest.modules.owners.internal.web.request.createOwner.CreateOwnerRequest;
import com.nss.pibblest.modules.owners.internal.web.request.createOwner.CreateOwnerResponse;
import com.nss.pibblest.modules.owners.internal.web.request.profile.OwnerProfileResponse;
import com.nss.pibblest.modules.owners.internal.web.request.profile.UpdateProfileRequest;
import com.nss.pibblest.modules.owners.internal.web.request.resendToken.ResendTokenRequest;
import com.nss.pibblest.modules.owners.internal.web.request.verifyOwner.VerifyOwnerRequest;
import com.nss.pibblest.modules.security.internal.core.JwtService;
import com.nss.pibblest.modules.security.internal.core.exceptions.OneTimeTokenExpired;
import com.nss.pibblest.modules.security.internal.infrastructure.data.OneTimeTokenOwnerEntity;
import com.nss.pibblest.modules.security.internal.infrastructure.data.OneTimeTokenOwnerRepository;

@ExtendWith(MockitoExtension.class)
class OwnerServiceTest {

    @Mock private OwnerRepository ownerRepository;
    @Mock private PasswordEncoder encoder;
    @Mock private OwnerMapper ownerMapper;
    @Mock private ApplicationEventPublisher events;
    @Mock private OneTimeTokenOwnerRepository oneTimeTokenOwnerRepository;
    @MockitoBean private JwtService jwtService;
    @Mock private MessageSource messageSource;

    @InjectMocks
    private OwnerService ownerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(ownerService, "timeToWaitForNextResend", 5L);
    }

    @Nested
    @DisplayName("Tests para el método registerOwner")
    class RegisterOwnerTests {

        @Test
        @DisplayName("Debe lanzar OwnerAlreadyExists si el email ya existe")
        void shouldThrowException_WhenEmailExists() {
            CreateOwnerRequest request = new CreateOwnerRequest();
            request.setEmail("existe@test.com");

            when(ownerRepository.existsByEmail("existe@test.com")).thenReturn(true);

            assertThrows(OwnerAlreadyExists.class, () -> ownerService.registerOwner(request));
            verify(ownerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe registrar exitosamente, publicar evento y retornar JWT")
        void shouldRegisterSuccessfully() {
            CreateOwnerRequest request = new CreateOwnerRequest();
            request.setCompany("Test Inc");
            request.setEmail("test@test.com");
            request.setPassword("plain-text");

            OwnerEntity mappedEntity = new OwnerEntity();
            OwnerEntity savedEntity = new OwnerEntity();
            savedEntity.setId(UUID.randomUUID());
            savedEntity.setCompany("Test Inc");
            savedEntity.setEmail("test@test.com");
            savedEntity.setSchemaName("test_schema");

            when(ownerRepository.existsByEmail(anyString())).thenReturn(false);
            when(ownerRepository.existsByCompany(anyString())).thenReturn(false);
            when(ownerMapper.toEntity(request)).thenReturn(mappedEntity);
            when(encoder.encode(anyString())).thenReturn("hashed-pwd");
            when(ownerRepository.save(mappedEntity)).thenReturn(savedEntity);
            when(jwtService.generateToken(any(), any(), any(), eq(false))).thenReturn("jwt-token");
            when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Success");

            ResponseEntity<CreateOwnerResponse> response = ownerService.registerOwner(request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals("jwt-token", response.getBody().token());
            
            verify(events).publishEvent(any(OwnerRegisteredEvent.class));
            verify(ownerRepository).save(mappedEntity);
        }
    }

    @Nested
    @DisplayName("Tests para el método verifyOwner")
    class VerifyOwnerTests {

        @Test
        @DisplayName("Debe lanzar OneTimeTokenExpired si el token ya venció")
        void shouldThrowException_WhenTokenExpired() {
            VerifyOwnerRequest request = new VerifyOwnerRequest();
            request.setToken("expired-token");

            OneTimeTokenOwnerEntity tokenEntity = new OneTimeTokenOwnerEntity();
            tokenEntity.setExpiresAt(ZonedDateTime.now().minusMinutes(10)); // Expirado
            
            when(oneTimeTokenOwnerRepository.findByTokenValue("expired-token")).thenReturn(Optional.of(tokenEntity));

            assertThrows(OneTimeTokenExpired.class, () -> ownerService.verifyOwner(request));
        }

        @Test
        @DisplayName("Debe lanzar OwnerAlreadyVerified si el token ya fue usado")
        void shouldThrowException_WhenTokenAlreadyUsed() {
            VerifyOwnerRequest request = new VerifyOwnerRequest();
            request.setToken("used-token");

            OneTimeTokenOwnerEntity tokenEntity = new OneTimeTokenOwnerEntity();
            tokenEntity.setExpiresAt(ZonedDateTime.now().plusMinutes(10)); 
            tokenEntity.setUsed(true); // Ya usado
            
            when(oneTimeTokenOwnerRepository.findByTokenValue("used-token")).thenReturn(Optional.of(tokenEntity));

            assertThrows(OwnerAlreadyVerified.class, () -> ownerService.verifyOwner(request));
        }
    }

    @Nested
    @DisplayName("Tests para el método resendToken")
    class ResendTokenTests {

        @Test
        @DisplayName("Debe retornar 200 genérico pero no hacer nada si el owner no existe (Previene enumeración)")
        void shouldDoNothing_WhenOwnerNotFound() {
            ResendTokenRequest request = new ResendTokenRequest("notfound@test.com");
            when(ownerRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());
            when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Generic msg");

            ResponseEntity<?> response = ownerService.resendToken(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(events, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Debe invalidar tokens viejos y publicar evento para generar uno nuevo")
        void shouldPublishEvent_WhenValidResendRequest() {
            ResendTokenRequest request = new ResendTokenRequest("valid@test.com");
            OwnerEntity owner = new OwnerEntity();
            owner.setId(UUID.randomUUID());
            owner.setEmail("valid@test.com");
            owner.setVerifiedAt(null); // No verificado aún

            when(ownerRepository.findByEmail("valid@test.com")).thenReturn(Optional.of(owner));
            when(oneTimeTokenOwnerRepository.findTopByOwnerIdOrderByCreatedAtDesc(owner.getId())).thenReturn(Optional.empty());
            when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Generic msg");

            ResponseEntity<?> response = ownerService.resendToken(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(oneTimeTokenOwnerRepository).expireAllActiveTokensByOwnerId(owner.getId());
            verify(events).publishEvent(any(OwnerGenerateVerifyToken.class));
        }
    }

    @Nested
    @DisplayName("Tests para el método getProfile")
    class GetProfileTests {

        @Test
        @DisplayName("Debe lanzar OwnerNotExists si el email del token no se encuentra en BD")
        void shouldThrowException_WhenOwnerNotFound() {
            String fakeEmail = "ghost@test.com";
            when(ownerRepository.findByEmail(fakeEmail)).thenReturn(Optional.empty());

            assertThrows(OwnerNotExists.class, () -> ownerService.getProfile(fakeEmail));
        }

        @Test
        @DisplayName("Debe retornar el OwnerProfileResponse correctamente mapeado")
        void shouldReturnProfile_WhenOwnerExists() {
            String email = "real@test.com";
            OwnerEntity owner = new OwnerEntity();
            owner.setEmail(email);
            
            OwnerProfileResponse mappedResponse = new OwnerProfileResponse(
                UUID.randomUUID(), "C", "N", "L", email, "O", true, ZonedDateTime.now()
            );

            when(ownerRepository.findByEmail(email)).thenReturn(Optional.of(owner));
            when(ownerMapper.toProfileResponse(owner)).thenReturn(mappedResponse);

            ResponseEntity<OwnerProfileResponse> response = ownerService.getProfile(email);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(email, response.getBody().email());
        }
    }

    @Nested
    @DisplayName("Tests para el método updateProfile")
    class UpdateProfileTests {

        @Test
        @DisplayName("Debe lanzar OwnerNotExists si el owner a actualizar no existe")
        void shouldThrowException_WhenOwnerNotFound() {
            UpdateProfileRequest request = new UpdateProfileRequest("A", "B");
            when(ownerRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            assertThrows(OwnerNotExists.class, () -> ownerService.updateProfile("ghost@test.com", request));
            verify(ownerRepository, never()).save(any());
        }

        @Test
        @DisplayName("Debe actualizar, guardar en BD y retornar el perfil modificado")
        void shouldUpdateAndSaveProfile_WhenOwnerExists() {
            String email = "real@test.com";
            UpdateProfileRequest request = new UpdateProfileRequest("NewName", "NewLast");
            
            OwnerEntity existingOwner = new OwnerEntity();
            existingOwner.setEmail(email);

            OwnerProfileResponse mappedResponse = new OwnerProfileResponse(
                UUID.randomUUID(), "C", "NewName", "NewLast", email, "O", true, ZonedDateTime.now()
            );

            when(ownerRepository.findByEmail(email)).thenReturn(Optional.of(existingOwner));
            // Simulamos que el mapper actualiza la entidad (el método void usa @MappingTarget)
            doNothing().when(ownerMapper).updateEntityFromRequest(request, existingOwner);
            when(ownerRepository.save(existingOwner)).thenReturn(existingOwner);
            when(ownerMapper.toProfileResponse(existingOwner)).thenReturn(mappedResponse);

            ResponseEntity<OwnerProfileResponse> response = ownerService.updateProfile(email, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("NewName", response.getBody().name());
            
            // Verificamos que se ejecutó el update con @MappingTarget y el guardado
            verify(ownerMapper).updateEntityFromRequest(request, existingOwner);
            verify(ownerRepository).save(existingOwner);
        }
    }
}
