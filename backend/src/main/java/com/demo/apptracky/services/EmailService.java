package com.demo.apptracky.services;

import com.demo.apptracky.dao.UserDao;
import com.demo.apptracky.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesAsyncClient;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.UUID;

@Service
public class EmailService {
    @Value("${apptracky.frontend-base-url}")
    private String baseUrl;

    private final SesAsyncClient sesClient;

    public EmailService() {
        sesClient = SesAsyncClient.builder().build();
    }

    public void sendValidationEmail(final User user) {
        final UUID linkUUID = UUID.randomUUID();
        final String link = baseUrl + "/#/validate-email?uuid=" + linkUUID;

        user.setEmailValidationUuid(linkUUID.toString());

        final String bodyStr = "Please validate your account with the following link.\n\n" + link;

        final Body body = Body.builder()
                .text(Content.builder().data(bodyStr).build())
                .build();

        final Message message = Message.builder()
                .body(body)
                .subject(Content.builder().data("AppTracky - validate your email").build())
                .build();

        final SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .message(message)
                .source("angzeyu@gmail.com")
                .destination(Destination.builder().toAddresses(user.getEmail()).build())
                .build();

        sesClient.sendEmail(sendEmailRequest);
    }

    public void sendForgotPasswordEmail(final User user) {
        final UUID linkUUID = UUID.randomUUID();
        final long expiry = System.currentTimeMillis() + 1000 * 60 * 5; // 5 minutes
        final String link = baseUrl + "/#/forgot-password/reset?uuid=" + linkUUID;

        user.setForgotPwUuid(linkUUID.toString());
        user.setForgotPwExpiry(expiry);

        final String bodyStr = "You requested a password reset for the account associated with this email.\n\n"
                + "Please use the following link to perform the reset.\n\n"
                + link;

        final Body body = Body.builder()
                .text(Content.builder().data(bodyStr).build())
                .build();

        final Message message = Message.builder()
                .body(body)
                .subject(Content.builder().data("AppTracky - reset your password").build())
                .build();

        final SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .message(message)
                .source("angzeyu@gmail.com")
                .destination(Destination.builder().toAddresses(user.getEmail()).build())
                .build();

        sesClient.sendEmail(sendEmailRequest);
    }
}
