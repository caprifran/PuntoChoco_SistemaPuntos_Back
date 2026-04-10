package com.puntochoco.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Usuario del sistema")
public class Usuario {

    @Id
    @Schema(description = "ID del usuario (Keycloak sub)", example = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6")
    private String id;

    @Column(nullable = false, unique = true)
    @Schema(description = "Nombre de usuario", example = "admin")
    private String username;

    @Column(nullable = false, unique = true)
    @Schema(description = "Correo electrónico", example = "juan.perez@example.com")
    private String email;

    @Column(nullable = false)
    @Schema(description = "Nombre del usuario", example = "Juan")
    private String nombre;

    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String apellido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Rol del usuario", example = "SELLER")
    private Rol rol;



    @Column(nullable = false)
    @Schema(description = "Indica si el usuario está activo", example = "true")
    private Boolean activo = true;

    @Column(name = "createdAt", updatable = false)
    @Schema(description = "Fecha de creación", example = "2026-01-15T10:00:00")
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    @Schema(description = "Fecha de última actualización", example = "2026-03-20T12:00:00")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
