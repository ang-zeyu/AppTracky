package com.demo.apptracky.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;

import java.util.Properties;

public class SsmParameterStoreConfig implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private static final String CLIENT_ID_KEY = "spring.security.oauth2.client.registration.google.client-id";

    private static final String CLIENT_ID_SECRET = "spring.security.oauth2.client.registration.google.client-secret";

    @Override
    public void onApplicationEvent(final ApplicationEnvironmentPreparedEvent event) {
        final GetParametersByPathResponse response;
        try (final SsmClient ssmClient = SsmClient.builder().build()) {
            response = ssmClient.getParametersByPath(
                    GetParametersByPathRequest.builder()
                            .path("/apptracky/oauth/google/")
                            .withDecryption(true)
                            .build()
            );
        }

        final String clientId = response.parameters().stream()
                .filter(p -> p.name().endsWith("client-id"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Missing Google oauth client id"))
                .value();
        final String clientSecret = response.parameters().stream()
                .filter(p -> p.name().endsWith("client-secret"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Missing Google oauth client secret"))
                .value();

        ConfigurableEnvironment environment = event.getEnvironment();
        Properties props = new Properties();
        props.put(CLIENT_ID_KEY, clientId);
        props.put(CLIENT_ID_SECRET, clientSecret);
        environment.getPropertySources().addFirst(new PropertiesPropertySource("oauth", props));
    }
}
