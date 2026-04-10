package com.puntochoco.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos para iniciar sesión")
public class LoginRequest {

    @Schema(description = "Nombre de usuario", example = "admin")
    private String username;

    @Schema(description = "Contraseña del usuario", example = "admin123")
    private String password;
}
