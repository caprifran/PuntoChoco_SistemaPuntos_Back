package com.puntochoco.repository;

import com.puntochoco.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    @Query("SELECT COALESCE(SUM(m.puntos), 0) FROM Movimiento m " +
           "WHERE m.cliente.id = :clienteId " +
           "AND (m.tipo = 'consumo' OR (m.tipo = 'alta' AND m.vencimiento > :ahora))")
    Long sumPuntosVigentes(@Param("clienteId") Long clienteId, @Param("ahora") LocalDateTime ahora);

    @Query("SELECT m FROM Movimiento m JOIN FETCH m.cliente c " +
           "WHERE c.fechaBaja IS NULL " +
           "AND (:clienteId IS NULL OR c.id = :clienteId) " +
           "ORDER BY m.fecha DESC")
    List<Movimiento> findHistoricoSinFechas(@Param("clienteId") Long clienteId);

    @Query("SELECT m FROM Movimiento m JOIN FETCH m.cliente c " +
           "WHERE c.fechaBaja IS NULL " +
           "AND (:clienteId IS NULL OR c.id = :clienteId) " +
           "AND m.fecha >= :fDesde " +
           "ORDER BY m.fecha DESC")
    List<Movimiento> findHistoricoDesde(
            @Param("clienteId") Long clienteId,
            @Param("fDesde") LocalDateTime fDesde);

    @Query("SELECT m FROM Movimiento m JOIN FETCH m.cliente c " +
           "WHERE c.fechaBaja IS NULL " +
           "AND (:clienteId IS NULL OR c.id = :clienteId) " +
           "AND m.fecha < :fHasta " +
           "ORDER BY m.fecha DESC")
    List<Movimiento> findHistoricoHasta(
            @Param("clienteId") Long clienteId,
            @Param("fHasta") LocalDateTime fHasta);

    @Query("SELECT m FROM Movimiento m JOIN FETCH m.cliente c " +
           "WHERE c.fechaBaja IS NULL " +
           "AND (:clienteId IS NULL OR c.id = :clienteId) " +
           "AND m.fecha >= :fDesde " +
           "AND m.fecha < :fHasta " +
           "ORDER BY m.fecha DESC")
    List<Movimiento> findHistoricoEntreFechas(
            @Param("clienteId") Long clienteId,
            @Param("fDesde") LocalDateTime fDesde,
            @Param("fHasta") LocalDateTime fHasta);

    @Query(value = "SELECT c.id, CONCAT(c.apellido, ', ', c.nombre) AS cliente, c.dni, " +
           "SUM(m.puntos) * -1 AS puntos " +
           "FROM clientes c INNER JOIN movimientos m ON c.id = m.cliente_id " +
           "AND m.tipo = 'consumo' " +
           "WHERE c.fecha_baja IS NULL " +
           "GROUP BY c.id, c.nombre, c.apellido, c.dni " +
           "ORDER BY puntos DESC LIMIT 5", nativeQuery = true)
    List<Object[]> findTopConsumidores();

    @Query(value = "SELECT c.id, CONCAT(c.apellido, ', ', c.nombre) AS cliente, c.dni, " +
           "SUM(m.puntos) AS puntos " +
           "FROM clientes c INNER JOIN movimientos m ON c.id = m.cliente_id " +
           "AND (m.tipo = 'consumo' OR (m.tipo = 'alta' AND m.vencimiento > NOW())) " +
           "WHERE c.fecha_baja IS NULL " +
           "GROUP BY c.id, c.nombre, c.apellido, c.dni " +
           "HAVING SUM(m.puntos) > 0 " +
           "ORDER BY puntos DESC LIMIT 5", nativeQuery = true)
    List<Object[]> findTopPuntos();
}
