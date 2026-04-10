package com.puntochoco.repository;

import com.puntochoco.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByUsernameAndActivoTrue(String username);

    Optional<Usuario> findByUsername(String username);

    Boolean existsByUsername(String username);

    @Query("SELECT u FROM Usuario u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Usuario> search(@Param("search") String search);
}
