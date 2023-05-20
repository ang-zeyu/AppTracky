package com.demo.apptracky.services;

import com.demo.apptracky.dao.UserDao;
import com.demo.apptracky.dto.RegisterDto;
import com.demo.apptracky.dto.UserDto;
import com.demo.apptracky.entities.Authority;
import com.demo.apptracky.entities.User;
import com.demo.apptracky.entities.enums.AuthProvider;
import com.demo.apptracky.exceptions.InvalidJwtException;
import com.demo.apptracky.utils.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static Logger log = LoggerFactory.getLogger(UserService.class);

    private final EmailService emailService;

    private final JwtEncoder jwtEncoder;

    private final PropMapper propMapper;

    private final UserDao userDao;

    private final PasswordEncoder passwordEncoder;

    public UserService(
            final EmailService emailService,
            final JwtEncoder jwtEncoder,
            final PropMapper propMapper,
            final UserDao userDao
    ) {
        this.emailService = emailService;
        this.jwtEncoder = jwtEncoder;
        this.propMapper = propMapper;
        this.userDao = userDao;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    public static long getUserIdFromJwt(final JwtAuthenticationToken authenticationToken) {
        final Object jwtUserIdObj = authenticationToken.getToken().getClaim("userId");
        if (!(jwtUserIdObj instanceof Long)) {
            throw new InvalidJwtException("userId missing in JWT");
        }

        return (Long) jwtUserIdObj;
    }

    public UserDto loginUser(final Authentication authentication) {
        final User user = userDao.findByUsername(authentication.getName());
        final String jwtToken = getJwtToken(user);
        return getUserDto(user, jwtToken);
    }

    public UserDto loginOrRegisterOAuthUser(final Authentication authentication, final String existingEmail) {
        DefaultOidcUser defaultOidcUser = (DefaultOidcUser)authentication.getPrincipal();
        if (defaultOidcUser.getEmail() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email permissions are needed"
            );
        } else if (existingEmail != null && !existingEmail.equals(defaultOidcUser.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Email not equals"
            );
        }

        User user = userDao.findByEmail(defaultOidcUser.getEmail());
        final UserDto userDto;
        if (user == null) {
            final RegisterDto registerDto = new RegisterDto(
                    defaultOidcUser.getEmail(),
                    null,
                    defaultOidcUser.getEmail()
            );

            userDto = registerUser(registerDto, AuthProvider.GOOGLE);
        } else {
            if (!user.getIsGoogle()) {
                // Allow federated identity association if email ownership is validated
                if (user.getValidated()) {
                    user.setIsGoogle(true);
                    userDao.save(user);
                } else {
                    throw new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED, "User is not validated"
                    );
                }
            }

            userDto = getUserDto(user, getJwtToken(user));
        }

        return userDto;
    }

    public UserDto registerUser(final RegisterDto regDto, final AuthProvider authProvider) {
        User user = propMapper.map(regDto, User.class);
        if (authProvider == AuthProvider.GOOGLE) {
            user.setValidated(true);
            user.setIsGoogle(true);
        } else if (authProvider == AuthProvider.SELF) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setValidated(false);
            user.setIsGoogle(false);

            // TODO temporarily disabled. Avoid racking up bills.
            emailService.sendValidationEmail(user);
        } else {
            throw new UnsupportedOperationException("Unhandled auth provider registration");
        }

        final Authority authority = new Authority(Roles.ROLE_JOBSEEKER);
        authority.setUser(user);
        user.setAuthorities(List.of(authority));

        try {
            user = userDao.save(user);
        } catch (final DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists");
        }

        return getUserDto(user, getJwtToken(user));
    }

    /**
     * Also associates a basic auth user to an OAuth-only user, if applicable.
     */
    public void changePassword(
            final String password,
            final JwtAuthenticationToken authentication
    ) {
        final long userId = getUserIdFromJwt(authentication);
        final User user = userDao.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        user.setPassword(passwordEncoder.encode(password));
        userDao.save(user);
    }

    public void forgotPassword(final String userEmail) {
        final User user = userDao.findByEmail(userEmail);
        if (user == null) {
            // Obscure if the account actually exists
            return;
        } else if (user.getPassword() == null) {
            // OAuth only account, again, obscure the result
            return;
        }

        emailService.sendForgotPasswordEmail(user);
        userDao.save(user);
    }

    public void resetPassword(final String password, final String uuid) {
        final User user = userDao.findByForgotPwUuid(uuid);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Link expired");
        }

        boolean didExpire = user.getForgotPwExpiry() == null || System.currentTimeMillis() > user.getForgotPwExpiry();
        if (didExpire) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link expired");
        }

        user.setPassword(passwordEncoder.encode(password));
        userDao.save(user);
    }

    public void validateUser(final String uuid) {
        final User user = userDao.findByEmailValidationUuid(uuid);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid link");
        }

        user.setValidated(true);
        user.setEmailValidationUuid(null);
        userDao.save(user);
    }

    private String getJwtToken(final User user) {
        final String subject = user.getUsername();
        final String claims = user.getAuthorities()
                .stream()
                .map(Authority::getName)
                .collect(Collectors.joining(" "));

        System.out.println(subject + " | " + claims);

        var claimSet = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60 * 120))
                .subject(subject)
                .claim("scope", claims)
                .claim("userId", user.getId())
                .build();
        JwtEncoderParameters parameters = JwtEncoderParameters.from(claimSet);

        final Jwt token = jwtEncoder.encode(parameters);
        return token.getTokenValue();
    }

    private UserDto getUserDto(User user, String jwtToken) {
        final UserDto userDto = propMapper.map(user, UserDto.class);
        userDto.setJwtToken(jwtToken);
        userDto.setRoles(user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toList()));
        return userDto;
    }
}
