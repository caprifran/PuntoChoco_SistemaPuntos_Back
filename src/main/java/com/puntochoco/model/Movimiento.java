package com.puntochoco.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Movimiento de puntos (alta o consumo)")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del movimiento", example = "1")
    private Long id;

    @Column(nullable = false, length = 10)
    @Schema(description = "Tipo de movimiento", example = "alta")
    private String tipo;

    @Column(nullable = false)
    @Schema(description = "Cantidad de puntos (positivo para alta, negativo para consumo)", example = "200")
    private Integer puntos;

    @Column(nullable = false)
    @Schema(description = "Fecha del movimiento", example = "2026-04-09T14:30:00")
    private LocalDateTime fecha;

    @Schema(description = "Fecha de vencimiento de los puntos", example = "2026-10-09T14:30:00")
    private LocalDateTime vencimiento;

    @Column(length = 1000)
    @Schema(description = "Detalle del movimiento (producto canjeado)", example = "Caja de bombones X2")
    private String detalle;

    @Schema(description = "Fecha de baja del movimiento", example = "2026-05-01T10:00:00")
    private LocalDateTime fechaBaja;

    @Column(unique = true)
    @Schema(description = "Número de factura asociada", example = "FAC-00123")
    private String nroFactura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clienteId", nullable = false)
    @JsonIgnore
    private Cliente cliente;

    @Column(name = "clienteId", insertable = false, updatable = false)
    @Schema(description = "ID del cliente asociado", example = "1")
    private Long clienteId;
}
