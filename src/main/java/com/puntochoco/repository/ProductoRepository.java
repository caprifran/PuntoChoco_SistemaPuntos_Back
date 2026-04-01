package com.puntochoco.repository;

import com.puntochoco.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByFechaBajaIsNull();

    @Query("SELECT p FROM Producto p WHERE p.fechaBaja IS NULL " +
           "AND LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Producto> searchActivos(@Param("search") String search);

    @Query("SELECT p FROM Producto p WHERE p.fechaBaja IS NULL " +
           "AND UPPER(REPLACE(TRIM(p.descripcion), ' ', '')) = :descNorm " +
           "AND (:excludeId IS NULL OR p.id <> :excludeId)")
    Optional<Producto> findDuplicado(@Param("descNorm") String descNorm,
                                     @Param("excludeId") Long excludeId);
}
