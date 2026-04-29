package com.nss.pibblest.modules.owners.internal.core;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nss.pibblest.modules.owners.api.events.OwnerGenerateVerifyToken;
import com.nss.pibblest.modules.owners.api.events.OwnerRegisteredEvent;
import com.nss.pibblest.modules.owners.internal.core.exceptions.OwnerAlreadyExists;
import com.nss.pibblest.modules.owners.internal.core.exceptions.OwnerAlreadyVerified;
import com.nss.pibblest.modules.owners.internal.core.exceptions.OwnerNotExists;
import com.nss.pibblest.modules.owners.internal.infrastructure.data.OwnerEntity;
import com.nss.pibblest.modules.owners.internal.infrastructure.data.OwnerRepository;
import com.nss.pibblest.modules.owners.internal.mappers.OwnerMapper;
import com.nss.pibblest.modules.owners.internal.utils.IdentifierGenerator;
import com.nss.pibblest.modules.owners.internal.web.request.createOwner.CreateOwnerRequest;
import com.nss.pibblest.modules.owners.internal.web.request.createOwner.CreateOwnerResponse;
import com.nss.pibblest.modules.owners.internal.web.request.resendToken.ResendTokenRequest;
import com.nss.pibblest.modules.owners.internal.web.request.resendToken.ResendTokenResponse;
import com.nss.pibblest.modules.owners.internal.web.request.verifyOwner.VerifyOwnerRequest;
import com.nss.pibblest.modules.owners.internal.web.request.verifyOwner.VerifyOwnerResponse;
import com.nss.pibblest.modules.security.internal.core.exceptions.OneTimeTokenExpired;
import com.nss.pibblest.modules.security.internal.core.exceptions.OneTimeTokenInvalid;
import com.nss.pibblest.modules.security.internal.infrastructure.data.OneTimeTokenOwnerEntity;
import com.nss.pibblest.modules.security.internal.infrastructure.data.OneTimeTokenOwnerRepository;

@Service
public class OwnerService {
    private final OwnerRepository ownerRepository;
    private final PasswordEncoder encoder;
    private final OwnerMapper ownerMapper;
    private final ApplicationEventPublisher events;
    private final OneTimeTokenOwnerRepository oneTimeTokenOwnerRepository;
    private final MessageSource messageSource;

    public OwnerService(OwnerRepository ownerRepository, PasswordEncoder encoder, OwnerMapper ownerMapper,
            ApplicationEventPublisher events, OneTimeTokenOwnerRepository oneTimeTokenOwnerRepository,
            MessageSource messageSource) {
        this.ownerRepository = ownerRepository;
        this.encoder = encoder;
        this.ownerMapper = ownerMapper;
        this.events = events;
        this.oneTimeTokenOwnerRepository = oneTimeTokenOwnerRepository;
        this.messageSource = messageSource;
    }

    @Transactional
    public ResponseEntity<CreateOwnerResponse> registerOwner(CreateOwnerRequest request) {

        if (ownerRepository.existsByEmail(request.getEmail())) {
            throw new OwnerAlreadyExists("error.owner.email.exists", request.getEmail());
        }

        if (ownerRepository.existsByCompany(request.getCompany())) {
            throw new OwnerAlreadyExists("error.owner.company.exists", request.getCompany());
        }

        String organizationCode = IdentifierGenerator.generateOrganizationCode(request.getCompany());
        String schemaName = IdentifierGenerator.generateSchemaName(request.getCompany());

        request.setOrganizationCode(organizationCode);
        request.setSchemaName(schemaName);

        OwnerEntity ownerEntity = ownerMapper.toEntity(request);
        ownerEntity.setPassword(encoder.encode(ownerEntity.getPassword()));

        OwnerEntity ownerCreated = ownerRepository.save(ownerEntity);

        events.publishEvent(new OwnerRegisteredEvent(
                ownerCreated.getId(),
                ownerCreated.getCompany(),
                ownerCreated.getEmail(),
                ownerCreated.getSchemaName()));

        CreateOwnerResponse responseBody = new CreateOwnerResponse(ownerCreated.getId(),
                "Owner registrado exitosamente");
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @Transactional
    public ResponseEntity<VerifyOwnerResponse> verifyOwner(VerifyOwnerRequest request) {

        String tokenValue = request.getToken();

        OneTimeTokenOwnerEntity tokenOwnerEntity = oneTimeTokenOwnerRepository.findByTokenValue(tokenValue)
                .orElseThrow(() -> new OneTimeTokenInvalid("error.token.invalid", null));

        if (tokenOwnerEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new OneTimeTokenExpired("error.token.expired", null);
        }

        if (tokenOwnerEntity.isUsed()) {
            throw new OwnerAlreadyVerified("error.owner.already.verified", null);
        }

        OwnerEntity ownerEntity = ownerRepository.findById(tokenOwnerEntity.getOwnerId())
                .orElseThrow(() -> new OwnerNotExists("error.owner.not.exists", tokenOwnerEntity.getOwnerId()));

        ownerEntity.setVerifiedAt(LocalDateTime.now().atZone(ZoneId.systemDefault()));
        ownerRepository.save(ownerEntity);

        tokenOwnerEntity.setUsed(true);
        oneTimeTokenOwnerRepository.save(tokenOwnerEntity);

        VerifyOwnerResponse response = new VerifyOwnerResponse("response.verify.owner", ownerEntity.getName());
        response.setMessage(tokenOwnerEntity.getOwnerId().toString());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // TODO: Implementar el servicio para volver a mandar el token
    // TODO: Limitar el envio de correos de confirmacion
    public ResponseEntity<ResendTokenResponse> resendToken(ResendTokenRequest request) {
        Optional<OwnerEntity> ownerEntity = ownerRepository.findByEmail(request.getEmail());

        ownerEntity.ifPresent(owner -> {
            boolean canRequestNewToken = oneTimeTokenOwnerRepository
                    .findTopByOwnerIdOrderByCreatedAtDesc(owner.getId())
                    .map(latestToken -> {
                        ZonedDateTime cooldownEnd = latestToken.getCreatedAt().plusMinutes(5);
                        return ZonedDateTime.now().isAfter(cooldownEnd);
                    })
                    .orElse(true);

            if (canRequestNewToken) {
                oneTimeTokenOwnerRepository.expireAllActiveTokensByOwnerId(owner.getId());

                events.publishEvent(new OwnerGenerateVerifyToken(owner.getEmail(), owner.getId()));
            }
        });

        String message = messageSource.getMessage("response.resend.token.owner", null, LocaleContextHolder.getLocale());

        ResendTokenResponse response = new ResendTokenResponse(message);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
