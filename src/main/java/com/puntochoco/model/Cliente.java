package com.puntochoco.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Cliente del sistema de puntos")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del cliente", example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Nombre del cliente", example = "María")
    private String nombre;

    @Column(nullable = false)
    @Schema(description = "Apellido del cliente", example = "González")
    private String apellido;

    @Column(nullable = false)
    @Schema(description = "DNI del cliente", example = "32456789")
    private String dni;

    @Column(name = "fechaBaja")
    @Schema(description = "Fecha de baja lógica", example = "2026-04-09T15:30:00")
    private LocalDateTime fechaBaja;

    @Column(name = "createdAt", updatable = false)
    @Schema(description = "Fecha de creación", example = "2026-01-15T10:00:00")
    private LocalDateTime createdAt;

    @Column(name = "updatedAt")
    @Schema(description = "Fecha de última actualización", example = "2026-03-20T12:00:00")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Movimiento> movimientos;

    @Transient
    @Schema(description = "Total de puntos vigentes", example = "1500")
    private Long totalPuntos;

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
