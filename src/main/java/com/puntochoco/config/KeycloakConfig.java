package com.puntochoco.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Value("${keycloak.server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    @Value("${keycloak.admin.user}")
    private String adminUser;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Bean
    public Keycloak keycloak() {
        System.out.println("Configurando Keycloak Admin Client:");
        System.out.println(" - Server URL: " + serverUrl);
        System.out.println(" - Target Realm: " + realm);
        System.out.println(" - Client ID: " + clientId);

        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .clientId(clientId);

        if (clientSecret != null && !clientSecret.isBlank()) {
            System.out.println(" - Auth Mode: Client Credentials (Autenticando contra realm: " + realm + ")");
            builder.realm(realm)
                   .clientSecret(clientSecret)
                   .grantType("client_credentials");
        } else if (adminUser != null && !adminUser.isBlank()) {
            System.out.println(" - Auth Mode: Password Grant (Autenticando contra realm: master)");
            builder.realm("master")
                   .username(adminUser)
                   .password(adminPassword)
                   .grantType("password");
        }

        return builder.build();
    }
}
