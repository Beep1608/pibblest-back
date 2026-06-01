package com.nss.pibblest.modules.owners.unit.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

// IMPORTS DE TESTING SPRING BOOT 4.X
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nss.pibblest.modules.owners.internal.core.OwnerService;
import com.nss.pibblest.modules.owners.internal.web.OwnerController;
import com.nss.pibblest.modules.owners.internal.web.request.createOwner.CreateOwnerRequest;
import com.nss.pibblest.modules.owners.internal.web.request.createOwner.CreateOwnerResponse;
import com.nss.pibblest.modules.owners.internal.web.request.profile.OwnerProfileResponse;
import com.nss.pibblest.modules.owners.internal.web.request.profile.UpdateProfileRequest;
import com.nss.pibblest.modules.owners.internal.web.request.resendToken.ResendTokenRequest;
import com.nss.pibblest.modules.owners.internal.web.request.resendToken.ResendTokenResponse;
import com.nss.pibblest.modules.owners.internal.web.request.verifyOwner.VerifyOwnerRequest;
import com.nss.pibblest.modules.owners.internal.web.request.verifyOwner.VerifyOwnerResponse;

// AQUÍ ESTÁ LA MAGIA: Le decimos a Spring que IGNORE la seguridad al cargar el contexto
@WebMvcTest(
    controllers = OwnerController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration.class,
        org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration.class,
        org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
class OwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OwnerService ownerService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Debe retornar 201 Created al registrar un owner exitosamente")
    void registerOwner_Returns201_WhenValidRequest() throws Exception {
        CreateOwnerRequest request = new CreateOwnerRequest();
        request.setCompany("Minion Inc.");
        request.setName("Papoi");
        request.setLastName("Morales");
        request.setEmail("hola@example.com");
        request.setPassword("Secreta123!");

        CreateOwnerResponse response = new CreateOwnerResponse("Owner creado", "fake-jwt-token");
        when(ownerService.registerOwner(any(CreateOwnerRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(response));

        mockMvc.perform(post("/api/owners/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Owner creado"));
    }

    @Test
    @DisplayName("Debe retornar 200 OK al verificar un owner")
    void verifyOwner_Returns200_WhenValidRequest() throws Exception {
        VerifyOwnerRequest request = new VerifyOwnerRequest();
        request.setToken("valid-token");

        VerifyOwnerResponse response = new VerifyOwnerResponse("Cuenta verificada");
        when(ownerService.verifyOwner(any(VerifyOwnerRequest.class)))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(post("/api/owners/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe retornar 200 OK al reenviar token")
    void resendToken_Returns200_WhenValidRequest() throws Exception {
        ResendTokenRequest request = new ResendTokenRequest("hola@example.com");
        ResendTokenResponse response = new ResendTokenResponse("Mensaje genérico enviado");
        
        when(ownerService.resendToken(any(ResendTokenRequest.class)))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(post("/api/owners/resend-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe retornar 200 OK y el perfil del Owner extraído del JWT")
    void getMyProfile_Returns200_WithProfileData() throws Exception {
        String mockEmail = "gru@minions.com";
        mockSecurityContext(mockEmail);

        OwnerProfileResponse mockResponse = new OwnerProfileResponse(
            UUID.randomUUID(), "Minion Inc", "Gru", "Felonius", mockEmail, "MINI-1234", true, ZonedDateTime.now()
        );

        when(ownerService.getProfile(mockEmail)).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(get("/api/owners/profile")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(mockEmail))
                .andExpect(jsonPath("$.name").value("Gru"));
    }

    @Test
    @DisplayName("Debe retornar 200 OK al actualizar el perfil correctamente")
    void updateMyProfile_Returns200_WhenValidRequest() throws Exception {
        String mockEmail = "gru@minions.com";
        mockSecurityContext(mockEmail);

        UpdateProfileRequest request = new UpdateProfileRequest("NuevoGru", "NuevoApellido");
        OwnerProfileResponse mockResponse = new OwnerProfileResponse(
            UUID.randomUUID(), "Minion Inc", "NuevoGru", "NuevoApellido", mockEmail, "MINI-1234", true, ZonedDateTime.now()
        );

        when(ownerService.updateProfile(eq(mockEmail), any(UpdateProfileRequest.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(put("/api/owners/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NuevoGru"))
                .andExpect(jsonPath("$.lastName").value("NuevoApellido"));
    }

    @Test
    @DisplayName("Debe retornar 400 Bad Request si la actualización envía un nombre en blanco")
    void updateMyProfile_Returns400_WhenNameIsBlank() throws Exception {
        mockSecurityContext("gru@minions.com");
        
        UpdateProfileRequest request = new UpdateProfileRequest("", "Apellido");

        mockMvc.perform(put("/api/owners/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
