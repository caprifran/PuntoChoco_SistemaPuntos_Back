package com.puntochoco.model;

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
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String tipo;

    @Column(nullable = false)
    private Integer puntos;

    @Column(nullable = false)
    private LocalDateTime fecha;

    private LocalDateTime vencimiento;

    @Column(length = 1000)
    private String detalle;

    private LocalDateTime fechaBaja;

    @Column(unique = true)
    private String nroFactura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clienteId", nullable = false)
    @JsonIgnore
    private Cliente cliente;

    @Column(name = "clienteId", insertable = false, updatable = false)
    private Long clienteId;
}
