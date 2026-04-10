package com.puntochoco.controller;

import com.puntochoco.dto.CambiarRolRequest;
import com.puntochoco.model.Usuario;
import com.puntochoco.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Operation(summary = "Listar todos los usuarios, con búsqueda opcional")
    @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida correctamente")
    @GetMapping
    public List<Usuario> listarUsuarios(@RequestParam(required = false) String search) {
        List<Usuario> usuarios;
        if (search != null && !search.isBlank()) {
            usuarios = usuarioRepository.search(search);
        } else {
            usuarios = usuarioRepository.findAll();
        }
        usuarios.forEach(u -> u.setPassword(null));
        return usuarios;
    }

    @Operation(summary = "Obtener un usuario por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuario(@PathVariable Long id) {
        try {
            Usuario usuario = usuarioRepository.findById(id).orElseThrow();
            usuario.setPassword(null);
            return ResponseEntity.ok(usuario);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Cambiar el rol de un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}/rol")
    public ResponseEntity<?> cambiarRol(@PathVariable Long id, @RequestBody CambiarRolRequest request) {
        try {
            Usuario usuario = usuarioRepository.findById(id).orElseThrow();
            usuario.setRol(request.getRol());
            usuarioRepository.save(usuario);
            usuario.setPassword(null);
            return ResponseEntity.ok(usuario);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Activar un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario activado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}/activar")
    public ResponseEntity<?> activarUsuario(@PathVariable Long id) {
        try {
            Usuario usuario = usuarioRepository.findById(id).orElseThrow();
            usuario.setActivo(true);
            usuarioRepository.save(usuario);
            usuario.setPassword(null);
            return ResponseEntity.ok(usuario);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Desactivar un usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario desactivado correctamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivarUsuario(@PathVariable Long id) {
        try {
            Usuario usuario = usuarioRepository.findById(id).orElseThrow();
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
            usuario.setPassword(null);
            return ResponseEntity.ok(usuario);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
