package com.demo.apptracky.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;

import java.util.Arrays;
import java.util.Properties;

public class SsmParameterStoreConfig implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private static final String CLIENT_ID_KEY = "spring.security.oauth2.client.registration.google.client-id";

    private static final String CLIENT_ID_SECRET = "spring.security.oauth2.client.registration.google.client-secret";

    private static final String JDBC_URL = "spring.datasource.url";

    private static final String JDBC_USER = "spring.datasource.username";

    private static final String JDBC_PW = "spring.datasource.password";

    @Override
    public void onApplicationEvent(final ApplicationEnvironmentPreparedEvent event) {
        final GetParametersByPathResponse oauthParameters;
        try (final SsmClient ssmClient = SsmClient.builder().build()) {
            oauthParameters = ssmClient.getParametersByPath(
                    GetParametersByPathRequest.builder()
                            .path("/apptracky/oauth/google/")
                            .withDecryption(true)
                            .build()
            );
        }

        final String clientId = oauthParameters.parameters().stream()
                .filter(p -> p.name().endsWith("client-id"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Missing Google oauth client id"))
                .value();
        final String clientSecret = oauthParameters.parameters().stream()
                .filter(p -> p.name().endsWith("client-secret"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Missing Google oauth client secret"))
                .value();

        ConfigurableEnvironment environment = event.getEnvironment();
        Properties props = new Properties();

        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            final GetParametersByPathResponse rdsParameters;
            try (final SsmClient ssmClient = SsmClient.builder().build()) {
                rdsParameters = ssmClient.getParametersByPath(
                        GetParametersByPathRequest.builder()
                                .path("/apptracky/rds/")
                                .withDecryption(true)
                                .build()
                );
            }
            final String rdsEndpoint = rdsParameters.parameters().stream()
                    .filter(p -> p.name().endsWith("url"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Missing RDS Endpoint"))
                    .value();
            final String rdsUser = rdsParameters.parameters().stream()
                    .filter(p -> p.name().endsWith("user"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Missing RDS User"))
                    .value();
            final String rdsPw = rdsParameters.parameters().stream()
                    .filter(p -> p.name().endsWith("pw"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Missing RDS Pw"))
                    .value();
            props.put(JDBC_URL, rdsEndpoint);
            props.put(JDBC_USER, rdsUser);
            props.put(JDBC_PW, rdsPw);
        }

        props.put(CLIENT_ID_KEY, clientId);
        props.put(CLIENT_ID_SECRET, clientSecret);
        environment.getPropertySources().addFirst(new PropertiesPropertySource("ssm", props));
    }
}
