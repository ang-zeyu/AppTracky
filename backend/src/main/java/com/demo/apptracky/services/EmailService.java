package com.demo.apptracky.services;

import com.demo.apptracky.entities.Application;
import com.demo.apptracky.entities.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesAsyncClient;
import software.amazon.awssdk.services.ses.model.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private static final SimpleDateFormat applicationDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Value("${apptracky.frontend-base-url}")
    private String baseUrl;

    private final SesAsyncClient sesClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmailService() {
        sesClient = SesAsyncClient.builder().build();
    }

    private String serializeApplication(final Application application) {
        try {
            final String company = objectMapper.writeValueAsString(application.getCompany());
            final String title = objectMapper.writeValueAsString(application.getTitle());
            return "<br>("
                    + applicationDateFormat.format(application.getDate())
                    + ") "
                    + company.substring(1, company.length() - 1)
                    + " -- "
                    + title.substring(1, title.length() - 1);
        } catch (final JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String userToTemplateData(final User user, final long staleThreshold) {
        final long currDate = new Date().getTime();
        final StringBuilder staleSb = new StringBuilder();
        final StringBuilder activeSb = new StringBuilder();
        for (final Application application : user.getApplications()) {
            if ((application.getDate().getTime() + staleThreshold) < currDate) {
                staleSb.append(serializeApplication(application));
            } else {
                activeSb.append(serializeApplication(application));
            }
        }

        String userName = "";
        try {
            userName = objectMapper.writeValueAsString(user.getUsername());
        } catch (final JsonProcessingException ex) {
            // Use an empty string
        }

        return "{"
                + " \"name\": "
                + userName
                + ", \"stale\": \""
                + staleSb
                + "\", \"active\": \""
                + activeSb
                + "\" }";
    }

    public void sendApplicationsReports(final List<User> users, final long staleThreshold) {
        /* In test mode, AWS SES does not allow bulk APIs =(

        final List<BulkEmailDestination> bulkEmailDestinations = users.stream()
                .map(user -> BulkEmailDestination.builder()
                        .destination(Destination.builder().toAddresses(user.getEmail()).build())
                        .replacementTemplateData(userToTemplateData(user, staleThreshold))
                        .build())
                .collect(Collectors.toList());

        final SendBulkTemplatedEmailRequest sendEmailRequest = SendBulkTemplatedEmailRequest.builder()
                .template("ApptrackyDailyApplicationsReport")
                .source("angzeyu@gmail.com")
                .destinations(bulkEmailDestinations)
                .build();

        sesClient.sendBulkTemplatedEmail(sendEmailRequest);
        */

        for (final User user : users) {
            final SendTemplatedEmailRequest sendEmailRequest = SendTemplatedEmailRequest.builder()
                    .template("ApptrackyDailyApplicationsReport")
                    .source("angzeyu@gmail.com")
                    .templateData(userToTemplateData(user, staleThreshold))
                    .destination(Destination.builder().toAddresses(user.getEmail()).build())
                    .build();

            sesClient.sendTemplatedEmail(sendEmailRequest);
        }

        log.info("Sent {} daily application reports", users.size());
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
