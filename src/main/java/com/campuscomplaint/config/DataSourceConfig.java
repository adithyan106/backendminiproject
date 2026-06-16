package com.campuscomplaint.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);
    private static final String LOCAL_MYSQL_URL = "jdbc:mysql://localhost:3306/campusinfrastructure";

    @Bean
    @Primary
    public DataSource dataSource(ConfigurableEnvironment environment) {
        String rawUrl = firstNonBlankEnvironmentVariable(
                environment,
                "SPRING_DATASOURCE_URL",
                "DATABASE_URL"
        );

        if (!hasText(rawUrl)) {
            if (isRender(environment)) {
                throw new IllegalStateException(
                        "No database URL configured for Render. Set DATABASE_URL to your Render Postgres "
                                + "internal database URL, or set SPRING_DATASOURCE_URL to a JDBC PostgreSQL URL."
                );
            }
            rawUrl = firstNonBlank(environment.getProperty("spring.datasource.url"), LOCAL_MYSQL_URL);
        }

        DatabaseUrl databaseUrl = normalizeDatabaseUrl(rawUrl.trim());
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(databaseUrl.jdbcUrl());

        String driverClassName = driverClassName(databaseUrl.jdbcUrl());
        if (driverClassName != null) {
            dataSource.setDriverClassName(driverClassName);
        }

        String username = firstNonBlank(
                firstNonBlankEnvironmentVariable(environment, "SPRING_DATASOURCE_USERNAME"),
                databaseUrl.username(),
                environment.getProperty("spring.datasource.username")
        );
        if (hasText(username)) {
            dataSource.setUsername(username);
        }

        String password = firstNonBlank(
                firstNonBlankEnvironmentVariable(environment, "SPRING_DATASOURCE_PASSWORD"),
                databaseUrl.password(),
                environment.getProperty("spring.datasource.password")
        );
        if (hasText(password)) {
            dataSource.setPassword(password);
        }

        log.info("Configured datasource with {}", databaseKind(databaseUrl.jdbcUrl()));
        return dataSource;
    }

    private DatabaseUrl normalizeDatabaseUrl(String rawUrl) {
        if (rawUrl.startsWith("jdbc:")) {
            return new DatabaseUrl(rawUrl, null, null);
        }

        URI uri = URI.create(rawUrl);
        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new IllegalArgumentException("Database URL is missing a scheme.");
        }

        String jdbcScheme = switch (scheme.toLowerCase()) {
            case "postgres", "postgresql" -> "jdbc:postgresql";
            case "mysql" -> "jdbc:mysql";
            default -> throw new IllegalArgumentException("Unsupported database URL scheme: " + scheme);
        };

        if (!hasText(uri.getHost())) {
            throw new IllegalArgumentException("Database URL is missing a host.");
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

        String username = null;
        String password = null;
        String userInfo = uri.getUserInfo();
        if (hasText(userInfo)) {
            String[] credentials = userInfo.split(":", 2);
            username = credentials[0];
            if (credentials.length > 1) {
                password = credentials[1];
            }
        }

        return new DatabaseUrl(jdbcUrl.toString(), username, password);
    }

    private String driverClassName(String jdbcUrl) {
        if (jdbcUrl.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        }
        if (jdbcUrl.startsWith("jdbc:mysql:")) {
            return "com.mysql.cj.jdbc.Driver";
        }
        return null;
    }

    private String databaseKind(String jdbcUrl) {
        if (jdbcUrl.startsWith("jdbc:postgresql:")) {
            return "PostgreSQL";
        }
        if (jdbcUrl.startsWith("jdbc:mysql:")) {
            return "MySQL";
        }
        return "configured JDBC";
    }

    private boolean isRender(ConfigurableEnvironment environment) {
        return hasText(firstNonBlankEnvironmentVariable(environment, "RENDER"));
    }

    private String firstNonBlankEnvironmentVariable(ConfigurableEnvironment environment, String... names) {
        for (String name : names) {
            Object value = environment.getSystemEnvironment().get(name);
            if (value != null && hasText(value.toString())) {
                return value.toString();
            }
        }
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record DatabaseUrl(String jdbcUrl, String username, String password) {
    }
}
