package com.nss.pibblest.modules.notifications.internal.core.owner;

import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.nss.pibblest.modules.owners.api.events.OwnerGenerateVerifyToken;
import com.nss.pibblest.modules.owners.api.events.OwnerRegisteredEvent;
import com.nss.pibblest.modules.security.internal.core.PersistentOneTimeTokeOwnerService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
public class EmailListener {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final PersistentOneTimeTokeOwnerService persistentOneTimeTokeOwnerService;
    private final MessageSource messageSource;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${app.email}")
    private String fromEmail;

    public EmailListener(JavaMailSender mailSender, TemplateEngine templateEngine,
            PersistentOneTimeTokeOwnerService persistentOneTimeTokeOwnerService, MessageSource messageSource) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.persistentOneTimeTokeOwnerService = persistentOneTimeTokeOwnerService;
        this.messageSource = messageSource;
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

    public void onGenerateVerifyToken(OwnerGenerateVerifyToken event){
         try {
             sendResendTokenToVerifyOwner(event.email(), event.id());
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
        context.setVariable("userToken", token);
        context.setVariable("verificationLink", frontendVerificationLink);

        String htmlContent = templateEngine.process("welcome-email", context);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    private void sendResendTokenToVerifyOwner(String to, UUID uuid) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Locale locale = LocaleContextHolder.getLocale();

        String subjectText = messageSource.getMessage("resend.token.subject", null,locale);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subjectText);

        GenerateOneTimeTokenRequest request = new GenerateOneTimeTokenRequest(uuid.toString());
        String token = persistentOneTimeTokeOwnerService.generate(request).getTokenValue();
        String frontendVerificationLink = frontendUrl + "/verify-account?token=" + token;

        Context context = new Context();
        context.setVariable("userEmail", to);
        context.setVariable("userToken", token);
        context.setVariable("verificationLink", frontendVerificationLink);

        String htmlContent = templateEngine.process("resend-token-email", context);
        helper.setText(htmlContent, true);
        mailSender.send(message);

    }
}
