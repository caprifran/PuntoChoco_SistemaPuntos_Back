package com.puntochoco.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String username;
    private String nombre;
    private String apellido;
    private String rol;
}
