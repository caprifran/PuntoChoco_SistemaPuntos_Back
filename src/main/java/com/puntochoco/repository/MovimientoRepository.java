package com.puntochoco.repository;

import com.puntochoco.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long>, JpaSpecificationExecutor<Movimiento> {

    @Query("SELECT COALESCE(SUM(m.puntos), 0) FROM Movimiento m " +
           "WHERE m.cliente.id = :clienteId " +
           "AND m.fechaBaja IS NULL " +
           "AND (m.tipo = 'consumo' OR (m.tipo = 'alta' AND m.vencimiento > :ahora))")
    Long sumPuntosVigentes(@Param("clienteId") Long clienteId, @Param("ahora") LocalDateTime ahora);

    @Query(value = "SELECT c.id, CONCAT(c.apellido, ', ', c.nombre) AS cliente, c.dni, " +
           "SUM(m.puntos) * -1 AS puntos " +
           "FROM clientes c INNER JOIN movimientos m ON c.id = m.cliente_id " +
           "AND m.tipo = 'consumo' " +
           "WHERE c.fecha_baja IS NULL " +
           "AND m.fecha_baja IS NULL " +
           "GROUP BY c.id, c.nombre, c.apellido, c.dni " +
           "ORDER BY puntos DESC LIMIT 5", nativeQuery = true)
    List<Object[]> findTopConsumidores();

    @Query(value = "SELECT c.id, CONCAT(c.apellido, ', ', c.nombre) AS cliente, c.dni, " +
           "SUM(m.puntos) AS puntos " +
           "FROM clientes c INNER JOIN movimientos m ON c.id = m.cliente_id " +
           "AND (m.tipo = 'consumo' OR (m.tipo = 'alta' AND m.vencimiento > NOW())) " +
           "WHERE c.fecha_baja IS NULL " +
           "AND m.fecha_baja IS NULL " +
           "GROUP BY c.id, c.nombre, c.apellido, c.dni " +
           "HAVING SUM(m.puntos) > 0 " +
           "ORDER BY puntos DESC LIMIT 5", nativeQuery = true)
    List<Object[]> findTopPuntos();
}
