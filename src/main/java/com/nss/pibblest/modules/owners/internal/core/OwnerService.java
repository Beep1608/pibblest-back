package com.nss.pibblest.modules.owners.internal.core;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nss.pibblest.modules.owners.internal.data.OwnerEntity;
import com.nss.pibblest.modules.owners.internal.data.OwnerRepository;
import com.nss.pibblest.modules.owners.internal.mappers.OwnerMapper;
import com.nss.pibblest.modules.owners.internal.utils.IdentifierGenerator;
import com.nss.pibblest.modules.owners.internal.web.request.createOwner.CreateOwnerRequest;
import com.nss.pibblest.modules.owners.internal.web.request.createOwner.CreateOwnerResponse;

@Service
public class OwnerService {
    private final OwnerRepository ownerRepository;
    private final PasswordEncoder encoder;
    private final OwnerMapper ownerMapper;

    public OwnerService(OwnerRepository ownerRepository, PasswordEncoder encoder, OwnerMapper ownerMapper)
    {
        this.ownerRepository = ownerRepository;
        this.encoder = encoder;
        this.ownerMapper = ownerMapper;
    }


    //TODO: 
    // 1. Implementar excepcion personalizada
    // 2. Publicacion de eventos ?
    @Transactional
    public ResponseEntity<CreateOwnerResponse> registerOwner(CreateOwnerRequest request)
    {
        try {
            String organizationCode = IdentifierGenerator.generateOrganizationCode(request.getCompany());
            String schemaName = IdentifierGenerator.generateSchemaName(request.getCompany());

            request.setOrganizationCode(organizationCode);
            request.setSchemaName(schemaName);

            OwnerEntity ownerEntity = ownerMapper.toEntity(request);

           OwnerEntity ownerCreated =  ownerRepository.save(ownerEntity);

            CreateOwnerResponse responseBody = new CreateOwnerResponse(ownerCreated.getId(), "Owner registrado exitosamente");
            return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
        } catch (DataIntegrityViolationException e) {
            // 5. Retornar ERROR CONOCIDO (HTTP 409 Conflict si ya existe el correo/empresa)
            // Nota: Aquí lo ideal es que tu CreateOwnerResponse soporte mensajes de error
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); 
            
        } catch (Exception e) {
            // 6. Retornar ERROR GENERAL (HTTP 500 Internal Server Error)
            e.printStackTrace(); // Solo para ver en consola qué reventó
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    
}
