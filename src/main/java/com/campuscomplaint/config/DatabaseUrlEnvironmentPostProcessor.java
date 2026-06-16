package com.campuscomplaint.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts Render-style database URLs into Spring datasource properties.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "renderDatabaseUrl";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = firstNonBlankEnvironmentVariable(
                environment,
                "SPRING_DATASOURCE_URL",
                "DATABASE_URL"
        );
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return;
        }

        Map<String, Object> datasourceProperties = toDatasourceProperties(databaseUrl.trim(), environment);
        if (!datasourceProperties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, datasourceProperties));
        }
    }

    private Map<String, Object> toDatasourceProperties(String databaseUrl, ConfigurableEnvironment environment) {
        Map<String, Object> properties = new HashMap<>();

        if (databaseUrl.startsWith("jdbc:")) {
            properties.put("spring.datasource.url", databaseUrl);
            return properties;
        }

        URI uri = URI.create(databaseUrl);
        String scheme = uri.getScheme();
        if (scheme == null) {
            return properties;
        }

        String jdbcScheme = switch (scheme.toLowerCase()) {
            case "postgres", "postgresql" -> "jdbc:postgresql";
            case "mysql" -> "jdbc:mysql";
            default -> null;
        };
        if (jdbcScheme == null) {
            return properties;
        }

        StringBuilder jdbcUrl = new StringBuilder(jdbcScheme)
                .append("://")
                .append(uri.getHost());

        if (uri.getPort() != -1) {
            jdbcUrl.append(':').append(uri.getPort());
        }
        if (uri.getRawPath() != null) {
            jdbcUrl.append(uri.getRawPath());
        }
        if (uri.getRawQuery() != null) {
            jdbcUrl.append('?').append(uri.getRawQuery());
        }

        properties.put("spring.datasource.url", jdbcUrl.toString());
        addCredentials(uri, environment, properties);
        return properties;
    }

    private void addCredentials(URI uri, ConfigurableEnvironment environment, Map<String, Object> properties) {
        String userInfo = uri.getUserInfo();
        if (userInfo == null || userInfo.isBlank()) {
            return;
        }

        String[] credentials = userInfo.split(":", 2);
        if (!hasNonBlankEnvironmentVariable(environment, "SPRING_DATASOURCE_USERNAME")) {
            properties.put("spring.datasource.username", credentials[0]);
        }
        if (credentials.length > 1 && !hasNonBlankEnvironmentVariable(environment, "SPRING_DATASOURCE_PASSWORD")) {
            properties.put("spring.datasource.password", credentials[1]);
        }
    }

    private boolean hasNonBlankEnvironmentVariable(ConfigurableEnvironment environment, String name) {
        Object value = environment.getSystemEnvironment().get(name);
        return value != null && !value.toString().isBlank();
    }

    private String firstNonBlankEnvironmentVariable(ConfigurableEnvironment environment, String... names) {
        for (String name : names) {
            Object value = environment.getSystemEnvironment().get(name);
            if (value != null && !value.toString().isBlank()) {
                return value.toString();
            }
        }
        return null;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
