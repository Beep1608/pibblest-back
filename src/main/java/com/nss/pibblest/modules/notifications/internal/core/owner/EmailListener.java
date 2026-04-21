package com.nss.pibblest.modules.notifications.internal.core.owner;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.nss.pibblest.modules.owners.api.events.OwnerRegisteredEvent;
import com.nss.pibblest.modules.security.internal.core.PersistentOneTimeTokeOwnerService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
public class EmailListener {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final PersistentOneTimeTokeOwnerService persistentOneTimeTokeOwnerService;

    @Value("${frontend.url}")
    private String frontendUrl;

    public EmailListener(JavaMailSender mailSender, TemplateEngine templateEngine, PersistentOneTimeTokeOwnerService persistentOneTimeTokeOwnerService ) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.persistentOneTimeTokeOwnerService = persistentOneTimeTokeOwnerService;
    }

    @KafkaListener(topics = "owners-registered-topic", groupId = "notifications-group")
    public void onOwnerRegistered(OwnerRegisteredEvent event) {
        System.out.println("¡Evento recibido desde Kafka!");
        try {

            sendWelcomeMail(event.email(), event.id());
        } catch (Exception e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
        }

    }

    private void sendWelcomeMail(String to, UUID uuid) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("no-reply@tuapp.com");
        helper.setTo(to);
        helper.setSubject("Bienvenido nihao");

        GenerateOneTimeTokenRequest request = new GenerateOneTimeTokenRequest(uuid.toString());
        String token = persistentOneTimeTokeOwnerService.generate(request).getTokenValue();
        String frontendVerificationLink = frontendUrl + "/verify-account?token=" + token;

        Context context = new Context();
        context.setVariable("userEmail", to);
        context.setVariable("userToken",token);
        context.setVariable("verificationLink", frontendVerificationLink);

        String htmlContent = templateEngine.process("welcome-email", context);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
