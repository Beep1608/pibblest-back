package com.nss.pibblest.modules.notifications.internal.core.store;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.nss.pibblest.modules.stores.api.dtos.StorePreviewDto;
import com.nss.pibblest.modules.stores.internal.core.StoreSseService;
import com.nss.pibblest.modules.stores.internal.infrastructure.data.StoreRepository;
import com.nss.pibblest.modules.tenant.SessionTrackerService;

@Service
public class StoreNotificationHelper {

    private final StoreRepository storeRepository;
    private final StoreSseService storeSseService;
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final SessionTrackerService sessionTrackerService;
    private final MessageSource messageSource;

    public StoreNotificationHelper(StoreRepository storeRepository, StoreSseService storeSseService,
            KafkaTemplate<Object, Object> kafkaTemplate, SessionTrackerService sessionTrackerService,
            MessageSource messageSource) {
        this.storeRepository = storeRepository;
        this.storeSseService = storeSseService;
        this.kafkaTemplate = kafkaTemplate;
        this.sessionTrackerService = sessionTrackerService;
        this.messageSource = messageSource;
    }

    public void notifyStoreChange(Long storeId, String userId) {
        if (!sessionTrackerService.isUserOnline(userId)) {
            return;
        }
        ZonedDateTime startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);

        storeRepository.findStorePreviewById(storeId, startOfDay)
                .ifPresent(freshDto -> {
                    String localizedTime = getLocalizedOperatingTime(freshDto.getCreatedAt(),
                            LocaleContextHolder.getLocale());
                    freshDto.setOperatinTime(localizedTime);

                    ProducerRecord<Object, Object> record = new ProducerRecord<>(
                            "store-realtime-updates", // Tópico
                            String.valueOf(freshDto.getId()), // Key (ID de la tienda)
                            freshDto);
                    record.headers().add("userId", userId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    kafkaTemplate.send(record);
                    //storeSseService.broadcastStoreUpdate(token, freshDto);

                });
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
