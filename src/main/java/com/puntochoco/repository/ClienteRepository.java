package com.puntochoco.repository;

import com.puntochoco.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByDniAndFechaBajaIsNull(String dni);

    Optional<Cliente> findByDniAndFechaBajaIsNullAndIdNot(String dni, Long id);

    @Query("SELECT c FROM Cliente c WHERE c.fechaBaja IS NULL " +
           "AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.dni) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<Cliente> searchActivos(@Param("search") String search);

    List<Cliente> findByFechaBajaIsNull();
}
