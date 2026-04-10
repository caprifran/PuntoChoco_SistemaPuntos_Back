package com.puntochoco.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos para registrar un nuevo usuario")
public class RegisterRequest {

    @Schema(description = "Nombre de usuario", example = "jperez")
    private String username;

    @Schema(description = "Contraseña", example = "miPassword123")
    private String password;

    @Schema(description = "Nombre del usuario", example = "Juan")
    private String nombre;

    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String apellido;
}
