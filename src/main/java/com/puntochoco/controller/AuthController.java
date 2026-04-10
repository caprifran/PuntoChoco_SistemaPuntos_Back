package com.puntochoco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * El login lo maneja Keycloak externamente.
 * Este controlador expone información del usuario autenticado
 * a partir del JWT emitido por el proveedor OAuth2.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Información del usuario autenticado (login gestionado por Keycloak)")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    @Operation(summary = "Obtener información del usuario autenticado desde el JWT de Keycloak")
    @ApiResponse(responseCode = "200", description = "Datos del usuario extraídos del token")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getClaimAsString("preferred_username");
        String email = jwt.getClaimAsString("email");
        String nombre = jwt.getClaimAsString("given_name");
        String apellido = jwt.getClaimAsString("family_name");

        @SuppressWarnings("unchecked")
        List<String> roles = jwt.hasClaim("realm_access")
                ? (List<String>) ((Map<String, Object>) jwt.getClaim("realm_access")).get("roles")
                : List.of();

        return ResponseEntity.ok(Map.of(
                "username", username != null ? username : "",
                "email", email != null ? email : "",
                "nombre", nombre != null ? nombre : "",
                "apellido", apellido != null ? apellido : "",
                "roles", roles
        ));
    }
}
