package com.nss.pibblest.modules.owners.unit;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nss.pibblest.modules.owners.internal.core.OwnerService;
import com.nss.pibblest.modules.owners.internal.web.OwnerController;
import com.nss.pibblest.modules.owners.internal.web.request.verifyOwner.VerifyOwnerRequest;
import com.nss.pibblest.modules.owners.internal.web.request.verifyOwner.VerifyOwnerResponse;
import com.nss.pibblest.modules.security.internal.core.exceptions.OneTimeTokenExpired;
import com.nss.pibblest.modules.security.internal.core.exceptions.OneTimeTokenInvalid;

@WebMvcTest(OwnerController.class)
@AutoConfigureMockMvc(addFilters=false)
public class OwnerControllerTest {
    
    @Autowired 
    private MockMvc mockMvc;


    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OwnerService ownerService;

    private VerifyOwnerRequest request;

    private final String VALID_TOKEN="papoiii";

    @BeforeEach
    void setUp(){
        request = new VerifyOwnerRequest();
        request.setToken(VALID_TOKEN);
    }

    @Test
    @DisplayName("Debe retornar 200 Ok cuando el servicio verifica con éxito")
    void verifyOwner_Returns200_WhenSuccess() throws Exception {
        VerifyOwnerResponse response  =new VerifyOwnerResponse("response.verify.owner", "Miau");
        response.setMessage(UUID.randomUUID().toString());

        ResponseEntity<VerifyOwnerResponse> responseEntity  = ResponseEntity.status(HttpStatus.OK).body(response);
        when(ownerService.verifyOwner(any(VerifyOwnerRequest.class))).thenReturn(responseEntity);

        mockMvc.perform(post("/api/owners/verify") 
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Debe retornar 409 Conflict cuando el servicio lanza OneTimeTokenExpired")
    void verifyOwner_Returns409_WhenTokenExpired() throws Exception {
      
        when(ownerService.verifyOwner(any(VerifyOwnerRequest.class)))
                .thenThrow(new OneTimeTokenExpired("error.token.expired", null));

      
        mockMvc.perform(post("/api/owners/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists()); 
    }

    @Test
    @DisplayName("Debe retornar 400 Bad Request cuando el servicio lanza OneTimeTokenInvalid")
    void verifyOwner_Returns400_WhenTokenInvalid() throws Exception {
  
        when(ownerService.verifyOwner(any(VerifyOwnerRequest.class)))
                .thenThrow(new OneTimeTokenInvalid("error.token.invalid", null));


        mockMvc.perform(post("/api/owners/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

}
