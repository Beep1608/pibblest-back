package com.nss.pibblest.modules.owners.internal.core;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nss.pibblest.modules.owners.internal.core.exceptions.OwnerAlreadyExists;
import com.nss.pibblest.modules.owners.internal.infrastructure.data.OwnerEntity;
import com.nss.pibblest.modules.owners.internal.infrastructure.data.OwnerRepository;
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
    // 
    // 2. Publicacion de eventos ?
    @Transactional
    public ResponseEntity<CreateOwnerResponse> registerOwner(CreateOwnerRequest request)
    {
        
        if(ownerRepository.existsByEmail(request.getEmail()))
        {
            throw new OwnerAlreadyExists("error.owner.email.exists", request.getEmail());
        }

        if(ownerRepository.existsByCompany(request.getCompany()))
        {
            throw new OwnerAlreadyExists("error.owner.company.exists", request.getCompany());
        }
    
        String organizationCode = IdentifierGenerator.generateOrganizationCode(request.getCompany());
        String schemaName = IdentifierGenerator.generateSchemaName(request.getCompany());

        request.setOrganizationCode(organizationCode);
        request.setSchemaName(schemaName);

        OwnerEntity ownerEntity = ownerMapper.toEntity(request);
        ownerEntity.setPassword(encoder.encode( ownerEntity.getPassword()));

        OwnerEntity ownerCreated =  ownerRepository.save(ownerEntity);

        CreateOwnerResponse responseBody = new CreateOwnerResponse(ownerCreated.getId(), "Owner registrado exitosamente");
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    
}
