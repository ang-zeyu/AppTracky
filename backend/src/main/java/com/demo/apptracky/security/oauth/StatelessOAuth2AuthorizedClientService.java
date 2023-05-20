package com.demo.apptracky.security.oauth;

import com.demo.apptracky.dto.RegisterDto;
import com.demo.apptracky.entities.enums.AuthProvider;
import com.demo.apptracky.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * By default, it is {@link org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService}.
 * There is also a JDBC version.
 *
 * For this app, Oauth/OIDC is used only for authentication, persistence is not required.
 * Everything is a no-op here.
 */
@Component
public class StatelessOAuth2AuthorizedClientService implements OAuth2AuthorizedClientService {
    private final UserService userService;

    public StatelessOAuth2AuthorizedClientService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(
            final String clientRegistrationId, final String principalName
    ) {
        throw new UnsupportedOperationException("loadAuthorizedClient should not be used");
    }

    @Override
    public void saveAuthorizedClient(
            final OAuth2AuthorizedClient authorizedClient, final Authentication authentication
    ) {}

    @Override
    public void removeAuthorizedClient(final String clientRegistrationId, final String principalName) {
        throw new UnsupportedOperationException("removeAuthorizedClient should not be used");
    }
}
