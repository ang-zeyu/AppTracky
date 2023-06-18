package com.demo.apptracky.security.oauth;

import com.demo.apptracky.services.EncryptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * By default, Spring stores and maps the OAuth2AuthorizationRequest using the JSESSION_ID.
 *
 * This is "stateless" instead, or rather, the state is stored on the client-side.
 */
@Component
public class StatelessOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    private static Logger log = LoggerFactory.getLogger(StatelessOAuth2AuthorizationRequestRepository.class);

    private final EncryptionService encryptionService;

    public StatelessOAuth2AuthorizationRequestRepository(final EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(final HttpServletRequest request) {
        log.debug("loadAuthorizationRequest");

        try (ServletInputStream inputStream = request.getInputStream()) {
            final byte[] decryptedReqBody = encryptionService.decrypt(inputStream.readAllBytes());
            return (OAuth2AuthorizationRequest)SerializationUtils.deserialize(decryptedReqBody);
        } catch (final Exception e) {
            log.error("Unable to read loadAuthorizationRequest request body", e);
            // ignored, unable to resolve
        }
        return null;
    }

    @Override
    public void saveAuthorizationRequest(
            final OAuth2AuthorizationRequest authorizationRequest,
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        log.debug("saveAuthorizationRequest");
        try {
            final byte[] serialized = SerializationUtils.serialize(authorizationRequest);
            final byte[] encrypted = encryptionService.encrypt(serialized);
            response.getOutputStream().write(encrypted);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        } catch (final Exception e) {
            log.error("Unable to read loadAuthorizationRequest request body", e);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) {
        log.debug("removeAuthorizationRequest");
        // Handled by client
        return loadAuthorizationRequest(request);
    }
}
