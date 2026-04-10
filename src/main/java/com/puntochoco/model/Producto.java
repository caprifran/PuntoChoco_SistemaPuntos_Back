package com.puntochoco.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Producto canjeable por puntos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del producto", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Descripción del producto", example = "Caja de bombones")
    private String descripcion;

    @Column(nullable = false)
    @Schema(description = "Precio en puntos", example = "500")
    private Integer precio;

    @Column(name = "fechaBaja")
    @Schema(description = "Fecha de baja lógica", example = "2026-04-09T15:30:00")
    private LocalDateTime fechaBaja;

    @Column(name = "imagenURL")
    @Schema(description = "URL de la imagen del producto", example = "https://ejemplo.com/img/bombones.jpg")
    private String imagenURL;

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
