package com.demo.apptracky.rest;

import com.demo.apptracky.dto.RegisterDto;
import com.demo.apptracky.dto.ResetPasswordDto;
import com.demo.apptracky.dto.UserDto;
import com.demo.apptracky.entities.enums.AuthProvider;
import com.demo.apptracky.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public UserDto authenticate(final Authentication authentication) {
        return userService.loginUser(authentication);
    }

    @PostMapping("/register")
    public UserDto register(@RequestBody @Valid final RegisterDto regDto) {
        return userService.registerUser(regDto, AuthProvider.SELF);
    }

    @PostMapping("/change-password")
    public void resetPw(
            @RequestBody final String password,
            final JwtAuthenticationToken authenticationToken
    ) {
        if (password.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password should be 8 characters long");
        }

        userService.changePassword(password, authenticationToken);
    }

    @PostMapping("/forgot-password")
    public void forgotPw(@RequestBody final String email) {
        userService.forgotPassword(email);
    }

    @PostMapping("/reset-password")
    public void resetPw(@RequestBody @Valid final ResetPasswordDto resetPasswordDto) {
        userService.resetPassword(resetPasswordDto.password(), resetPasswordDto.uuid());
    }

    @PostMapping("/validate-email")
    public void validateEmail(@RequestBody final String emailValidationUuid) {
        userService.validateUser(emailValidationUuid);
    }
}
