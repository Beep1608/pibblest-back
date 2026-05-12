package com.nss.pibblest.modules.stores.internal.core;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nss.pibblest.modules.stores.api.dtos.StorePreviewDto;
import com.nss.pibblest.modules.stores.internal.core.exceptions.StoreNotFound;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreEntity;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreRepository;
import com.nss.pibblest.modules.stores.internal.mappers.StoreMapper;
import com.nss.pibblest.modules.stores.internal.web.requests.stores.createStore.CreateStoreRequest;
import com.nss.pibblest.modules.stores.internal.web.requests.stores.createStore.CreateStoreResponse;
import com.nss.pibblest.modules.stores.internal.web.requests.stores.getAllStores.GetAllStoresResponse;
import com.nss.pibblest.modules.stores.internal.web.requests.updateStore.UpdateStoreRequest;
import com.nss.pibblest.modules.stores.internal.web.requests.updateStore.UpdateStoreResponse;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;
    private final MessageSource messageSource;

    public StoreService(StoreRepository storeRepository, StoreMapper storeMapper, MessageSource messageSource) {
        this.storeRepository = storeRepository;
        this.storeMapper = storeMapper;
        this.messageSource = messageSource;
    }

    public ResponseEntity<GetAllStoresResponse> getAllStores(Pageable pageable) {

        ZonedDateTime startOfToday = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);
        Page<StorePreviewDto> storesPage = storeRepository.findStorePreviewInfo(pageable, startOfToday);
        Locale locale = LocaleContextHolder.getLocale();

        storesPage.forEach(dto -> {
            String localizedTime = getLocalizedOperatingTime(dto.getCreatedAt(), locale);
            dto.setOperatinTime(localizedTime);
        });
        GetAllStoresResponse response = new GetAllStoresResponse(storesPage);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Transactional
    public ResponseEntity<CreateStoreResponse> createStore(CreateStoreRequest request) {

        StoreEntity storeEntity = storeMapper.toEntity(request);

        StoreEntity newStoreEntity = storeRepository.save(storeEntity);

        String message = messageSource.getMessage("store.created.response", new Object[] { newStoreEntity.getName() },
                LocaleContextHolder.getLocale());
        CreateStoreResponse response = new CreateStoreResponse(message);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ResponseEntity<UpdateStoreResponse> updateStore(Long id, UpdateStoreRequest request) {

        Locale locale = LocaleContextHolder.getLocale();
        String notFoundMessage = messageSource.getMessage("error.store.not.found", new Object[] { id }, locale);

        StoreEntity storeEntity = storeRepository.findById(id).orElseThrow(
                () -> new StoreNotFound(notFoundMessage));

        storeMapper.updateEntityFromRequest(request, storeEntity);

        storeRepository.save(storeEntity);

        String successMessage = messageSource.getMessage("store.updated.success",
                new Object[] { storeEntity.getName() }, locale);

        UpdateStoreResponse response = new UpdateStoreResponse(successMessage);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    private String getLocalizedOperatingTime(ZonedDateTime createdAt, Locale locale) {
        if (createdAt == null) {
            return messageSource.getMessage("time.unknown", null, locale);
        }

        ZonedDateTime now = ZonedDateTime.now();
        Period period = Period.between(createdAt.toLocalDate(), now.toLocalDate());

        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        // Evaluamos Años
        if (years > 0) {
            String key = years == 1 ? "time.years.one" : "time.years.many";
            return messageSource.getMessage(key, new Object[] { years }, locale);
        }
        // Evaluamos Meses
        if (months > 0) {
            String key = months == 1 ? "time.months.one" : "time.months.many";
            return messageSource.getMessage(key, new Object[] { months }, locale);
        }
        // Evaluamos Días
        if (days > 0) {
            String key = days == 1 ? "time.days.one" : "time.days.many";
            return messageSource.getMessage(key, new Object[] { days }, locale);
        }

        // Evaluamos Horas si se creó hoy
        long hours = Duration.between(createdAt, now).toHours();
        if (hours > 0) {
            String key = hours == 1 ? "time.hours.one" : "time.hours.many";
            return messageSource.getMessage(key, new Object[] { hours }, locale);
        }

        // Menos de una hora
        return messageSource.getMessage("time.less.than.hour", null, locale);
    }
}
