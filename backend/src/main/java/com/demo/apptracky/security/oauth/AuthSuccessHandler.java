package com.demo.apptracky.security.oauth;

import com.demo.apptracky.dto.RegisterDto;
import com.demo.apptracky.dto.UserDto;
import com.demo.apptracky.entities.enums.AuthProvider;
import com.demo.apptracky.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {
    private final UserService userService;

    private final ObjectMapper objectMapper;

    public AuthSuccessHandler(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Authentication authentication
    ) throws IOException {
        System.out.println(authentication.getClass().getSimpleName());
        authentication.getAuthorities().forEach(a -> System.out.println(a.getAuthority()));
        final String existingEmail = request.getParameter("associate");
        final UserDto userDto = userService.loginOrRegisterOAuthUser(authentication, existingEmail);
        response.getWriter().write(objectMapper.writeValueAsString(userDto));
    }
}
