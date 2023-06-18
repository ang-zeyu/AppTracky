package com.demo.apptracky.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.log.LogMessage;
import org.springframework.security.web.DefaultRedirectStrategy;

import java.io.IOException;

public class OAuth2ClientRedirectStrategy extends DefaultRedirectStrategy {
    @Override
    public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url) throws IOException {
        String redirectUrl = calculateRedirectUrl(request.getContextPath(), url);
        redirectUrl = response.encodeRedirectURL(redirectUrl);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(LogMessage.format("Redirecting to %s", redirectUrl));
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("X-Oauth2-Redirect", response.encodeRedirectURL(url));
    }
}
