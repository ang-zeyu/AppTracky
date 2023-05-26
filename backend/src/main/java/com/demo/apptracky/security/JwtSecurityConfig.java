package com.demo.apptracky.security;

import com.demo.apptracky.security.oauth.AuthFailureHandler;
import com.demo.apptracky.security.oauth.AuthSuccessHandler;
import com.demo.apptracky.security.oauth.OAuth2ClientRedirectStrategy;
import com.demo.apptracky.security.oauth.StatelessOAuth2AuthorizationRequestRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.util.ResourceUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Configuration
@EnableMethodSecurity
public class JwtSecurityConfig {
    @Value("${apptracky.frontend-base-url}")
    private String baseUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(
            final HttpSecurity http,
            final StatelessOAuth2AuthorizationRequestRepository requestRepository,
            final AuthSuccessHandler oauthSuccessHandler
    ) throws Exception {
        http
                .authorizeHttpRequests(config -> config
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/api/auth/login").authenticated()
                        .requestMatchers("/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/validate-email").permitAll()
                        .requestMatchers("/api/auth/reset-password").permitAll()
                        .requestMatchers("/api/auth/forgot-password").permitAll()
                        .requestMatchers("/api/**").hasRole("JOBSEEKER")
                        .anyRequest().denyAll()
                )
                .formLogin().disable()
                .httpBasic().authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .and()
                .exceptionHandling().authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.FORBIDDEN))
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource()).and()
                .oauth2Login(oauth2 -> {
                    // oauth2.redirectionEndpoint().baseUri(baseUrl).and().successHandler();
                    /*oauth2.successHandler((request, response, authentication) -> {

                    });*/
                    oauth2.authorizationEndpoint(conf -> conf
                            .authorizationRedirectStrategy(new OAuth2ClientRedirectStrategy())
                            .authorizationRequestRepository(requestRepository));
                    oauth2.successHandler(oauthSuccessHandler);
                    oauth2.failureHandler(new AuthFailureHandler());
                })
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        var converter = new JwtAuthenticationConverter();
        var granted = new JwtGrantedAuthoritiesConverter();
        granted.setAuthorityPrefix("");
        converter.setJwtGrantedAuthoritiesConverter(granted);
        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(j -> j.jwtAuthenticationConverter(converter)));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowedOrigins(Collections.singletonList(baseUrl));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setExposedHeaders(Collections.singletonList("X-Oauth2-Redirect"));
        //configuration.set
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /*
    // One-time
    private void generateDevKeyPairs(final File publicKeyResource, final File privateKeyResource)
            throws NoSuchAlgorithmException {
        var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        final KeyPair keyPair = keyPairGenerator.generateKeyPair();

        try (FileOutputStream fos = new FileOutputStream(publicKeyResource)) {
            fos.write(keyPair.getPublic().getEncoded());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileOutputStream fos = new FileOutputStream(privateKeyResource)) {
            fos.write(keyPair.getPrivate().getEncoded());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

    @Bean
    @Profile("local")
    public RSAKey rsaKeyLocal() throws NoSuchAlgorithmException, FileNotFoundException {
        final File publicKeyResource = ResourceUtils.getFile("classpath:public.key");
        final File privateKeyResource = ResourceUtils.getFile("classpath:private.key");
        /*
        One-time
        if (!publicKeyResource.exists() || !privateKeyResource.exists()) {
            generateDevKeyPairs(publicKeyResource, privateKeyResource);
        }
        */

        RSAPublicKey rsaPublicKey;
        PrivateKey privateKey;
        try {
            final byte[] publicKeyRaw = Files.readAllBytes(publicKeyResource.toPath());
            final byte[] privateKeyRaw = Files.readAllBytes(privateKeyResource.toPath());

            final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            final EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyRaw);
            final EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyRaw);

            rsaPublicKey = (RSAPublicKey)keyFactory.generatePublic(publicKeySpec);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
        } catch (final IOException | InvalidKeySpecException ex) {
            throw new RuntimeException(ex);
        }

        return new RSAKey.Builder(rsaPublicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    @Bean
    @Profile("!local")
    public RSAKey rsaKeyProd() {
        throw new UnsupportedOperationException("RSA Key retrieval for PROD unimplemented");
    }

    @Bean
    public JWKSource jwkSource(final RSAKey rsaKey) {
        var jwkSet = new JWKSet(rsaKey);
        return new JWKSource() {
            @Override
            public List<JWK> get(JWKSelector jwkSelector, SecurityContext securityContext) throws KeySourceException {
                return jwkSelector.select(jwkSet);
            }
        };
    }

    @Bean
    public JwtDecoder jwtDecoder(final RSAKey rsaKey) throws JOSEException {
        return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
    }

    @Bean
    public JwtEncoder jwtEncoder(final JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }
}
