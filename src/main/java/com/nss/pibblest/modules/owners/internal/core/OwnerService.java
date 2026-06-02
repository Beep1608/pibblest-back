package com.nss.pibblest.modules.owners.internal.core;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
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
import com.nss.pibblest.modules.owners.internal.web.request.profile.OwnerProfileResponse;
import com.nss.pibblest.modules.owners.internal.web.request.profile.UpdateProfileRequest;
import com.nss.pibblest.modules.owners.internal.web.request.resendToken.ResendTokenRequest;
import com.nss.pibblest.modules.owners.internal.web.request.resendToken.ResendTokenResponse;
import com.nss.pibblest.modules.owners.internal.web.request.verifyOwner.VerifyOwnerRequest;
import com.nss.pibblest.modules.owners.internal.web.request.verifyOwner.VerifyOwnerResponse;
import com.nss.pibblest.modules.security.internal.core.JwtService;
import com.nss.pibblest.modules.security.internal.core.exceptions.OneTimeTokenExpired;
import com.nss.pibblest.modules.security.internal.core.exceptions.OneTimeTokenInvalid;
import com.nss.pibblest.modules.security.internal.infrastructure.data.OneTimeTokenOwnerEntity;
import com.nss.pibblest.modules.security.internal.infrastructure.data.OneTimeTokenOwnerRepository;
import com.nss.pibblest.shared.exceptions.TranslatedRuntimeException;

@Service
public class OwnerService {
    private final OwnerRepository ownerRepository;
    private final PasswordEncoder encoder;
    private final OwnerMapper ownerMapper;
    private final ApplicationEventPublisher events;
    private final OneTimeTokenOwnerRepository oneTimeTokenOwnerRepository;
    private final JwtService jwtService;
    private final MessageSource messageSource;
    
    @Value("${time.to.wait.resend.token}")
    private long timeToWaitForNextResend;

    public OwnerService(OwnerRepository ownerRepository, PasswordEncoder encoder, OwnerMapper ownerMapper,
            ApplicationEventPublisher events, OneTimeTokenOwnerRepository oneTimeTokenOwnerRepository,
            JwtService jwtService, MessageSource messageSource) {
        this.ownerRepository = ownerRepository;
        this.encoder = encoder;
        this.ownerMapper = ownerMapper;
        this.events = events;
        this.oneTimeTokenOwnerRepository = oneTimeTokenOwnerRepository;
        this.jwtService = jwtService;
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

        OwnerEntity ownerEntity = ownerMapper.toEntity(request);
        
        boolean identifiersAssigned = false;
        for (int i = 0; i < 3; i++) {
            String orgCode = IdentifierGenerator.generateOrganizationCode(request.getCompany());
            String schemaName = IdentifierGenerator.generateSchemaName(request.getCompany());
            
            if (!ownerRepository.existsByOrganizationCode(orgCode) && !ownerRepository.existsBySchemaName(schemaName)) {
                ownerEntity.setOrganizationCode(orgCode);
                ownerEntity.setSchemaName(schemaName);
                identifiersAssigned = true;
                break;
            }
        }
        
        if (!identifiersAssigned) {
            throw new TranslatedRuntimeException("error.owner.registration.collision", request.getCompany());
        }

        ownerEntity.setPassword(encoder.encode(request.getPassword()));
        ownerEntity.setIsActive(false);

        OwnerEntity ownerCreated = ownerRepository.save(ownerEntity);

        events.publishEvent(new OwnerRegisteredEvent(
                ownerCreated.getId(),
                ownerCreated.getCompany(),
                ownerCreated.getEmail(),
                ownerCreated.getSchemaName(),
                ownerCreated.getPassword()));

        String token = jwtService.generateToken(ownerCreated.getId(), ownerCreated.getName(), ownerCreated.getSchemaName(), false);
        String message = messageSource.getMessage("response.created.owner", null, LocaleContextHolder.getLocale());
        
        CreateOwnerResponse responseBody = new CreateOwnerResponse(message, token);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @Transactional
    public ResponseEntity<VerifyOwnerResponse> verifyOwner(VerifyOwnerRequest request) {
        String token = request.getToken(); 
        
        OneTimeTokenOwnerEntity tokenOwnerEntity = oneTimeTokenOwnerRepository.findByTokenValue(token)
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
        ownerEntity.setIsActive(true); 
        ownerRepository.save(ownerEntity);

        tokenOwnerEntity.setUsed(true);
        oneTimeTokenOwnerRepository.save(tokenOwnerEntity);

        String message = messageSource.getMessage("response.verify.owner", new Object[] { ownerEntity.getName() },
                LocaleContextHolder.getLocale());
        VerifyOwnerResponse response = new VerifyOwnerResponse(message);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Transactional
    public ResponseEntity<ResendTokenResponse> resendToken(ResendTokenRequest request) {
        Optional<OwnerEntity> ownerEntity = ownerRepository.findByEmail(request.email());

        ownerEntity.ifPresent(owner -> {
            boolean isAlreadyVerified = owner.getVerifiedAt() != null;

            boolean canRequestNewToken = oneTimeTokenOwnerRepository
                    .findTopByOwnerIdOrderByCreatedAtDesc(owner.getId())
                    .map(latestToken -> {
                        ZonedDateTime cooldownEnd = latestToken.getCreatedAt().plusMinutes(timeToWaitForNextResend);
                        return ZonedDateTime.now().isAfter(cooldownEnd);
                    })
                    .orElse(true);

            if (canRequestNewToken && !isAlreadyVerified) {
                oneTimeTokenOwnerRepository.expireAllActiveTokensByOwnerId(owner.getId());
                events.publishEvent(new OwnerGenerateVerifyToken(owner.getEmail(), owner.getId()));
            }
        });

        String message = messageSource.getMessage("response.resend.token.owner", null, LocaleContextHolder.getLocale());
        ResendTokenResponse response = new ResendTokenResponse(message);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<OwnerProfileResponse> getProfile(String email) {
        OwnerEntity owner = ownerRepository.findByEmail(email)
                .orElseThrow(() -> new OwnerNotExists("error.owner.not.exists", email));
        
        return ResponseEntity.ok(ownerMapper.toProfileResponse(owner));
    }

    @Transactional
    public ResponseEntity<OwnerProfileResponse> updateProfile(String email, UpdateProfileRequest request) {
        OwnerEntity owner = ownerRepository.findByEmail(email)
                .orElseThrow(() -> new OwnerNotExists("error.owner.not.exists", email));
        
        ownerMapper.updateEntityFromRequest(request, owner);
        ownerRepository.save(owner);
        
        return ResponseEntity.ok(ownerMapper.toProfileResponse(owner));
    }
}
