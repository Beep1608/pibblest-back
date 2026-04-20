package com.nss.pibblest.modules.notifications.internal.core.owner;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.nss.pibblest.modules.owners.api.events.OwnerRegisteredEvent;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Component
public class EmailListener {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailListener(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @KafkaListener(topics = "owners-registered-topic", groupId = "notifications-group")
    public void onOwnerRegistered(OwnerRegisteredEvent event) {
        System.out.println("¡Evento recibido desde Kafka!");
        try {

            sendWelcomeMail(event.email());
        } catch (Exception e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
        }

    }

    private void sendWelcomeMail(String to) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("no-reply@tuapp.com");
        helper.setTo(to);
        helper.setSubject("Bienvenido nihao");

        Context context = new Context();
        context.setVariable("userEmail", to);

        String htmlContent = templateEngine.process("welcome-email", context);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
