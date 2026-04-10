package com.puntochoco.controller;

import com.puntochoco.dto.RegisterRequest;
import com.puntochoco.dto.CambiarRolRequest;
import com.puntochoco.model.Rol;
import com.puntochoco.model.Usuario;
import com.puntochoco.repository.UsuarioRepository;
import com.puntochoco.service.KeycloakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final com.puntochoco.service.KeycloakService keycloakService;

    public UsuarioController(UsuarioRepository usuarioRepository, com.puntochoco.service.KeycloakService keycloakService) {
        this.usuarioRepository = usuarioRepository;
        this.keycloakService = keycloakService;
    }

    @Operation(summary = "Crear un nuevo usuario en Keycloak y DB local")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuario creado correctamente"),
        @ApiResponse(responseCode = "500", description = "Error interno al crear el usuario")
    })
    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody RegisterRequest request) {
        try {
            // 1. Crear en Keycloak
            String keycloakId = keycloakService.crearUsuario(request);

            // 2. Crear en DB local
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setId(keycloakId);
            nuevoUsuario.setUsername(request.getUsername());
            nuevoUsuario.setEmail(request.getEmail());
            nuevoUsuario.setNombre(request.getNombre());
            nuevoUsuario.setApellido(request.getApellido());
            nuevoUsuario.setRol(Rol.valueOf(request.getRol().toUpperCase()));
            nuevoUsuario.setActivo(true);

            Usuario guardado = usuarioRepository.save(nuevoUsuario);
            return ResponseEntity.status(201).body(guardado);
        } catch (Exception e) {
            e.printStackTrace(); // Ver bug en logs de Docker
            return ResponseEntity.status(500).body(Map.of(
                "error", "Falla al sincronizar con DB local tras crear en Keycloak: " + e.getMessage()
            ));
        }
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
        return usuarios;
    }

    @Operation(summary = "Obtener un usuario por su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuario(@PathVariable String id) {
        return usuarioRepository.findById(id)
                .map(u -> {
                    Map<String, Object> resp = new java.util.HashMap<>();
                    resp.put("id", u.getId());
                    resp.put("username", u.getUsername());
                    resp.put("email", u.getEmail());
                    resp.put("nombre", u.getNombre());
                    resp.put("apellido", u.getApellido());
                    resp.put("activo", u.getActivo());
                    resp.put("rol", u.getRol() != null ? u.getRol().name() : "USER");
                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cambiar el rol de un usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Rol actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}/rol")
    public ResponseEntity<?> cambiarRol(@PathVariable String id, @RequestBody CambiarRolRequest request) {
        return usuarioRepository.findById(id)
                .map(u -> {
                    try {
                        keycloakService.asignarRol(id, request.getRol().name());
                        
                        // Sincronizar localmente
                        u.setRol(request.getRol());
                        usuarioRepository.save(u);
                        
                        Map<String, Object> resp = new java.util.HashMap<>();
                        resp.put("id", u.getId());
                        resp.put("username", u.getUsername());
                        resp.put("email", u.getEmail());
                        resp.put("nombre", u.getNombre());
                        resp.put("apellido", u.getApellido());
                        resp.put("activo", u.getActivo());
                        resp.put("rol", request.getRol().name());
                        
                        return ResponseEntity.ok(resp);
                    } catch (Exception e) {
                        return ResponseEntity.status(500).body("Error al actualizar rol en Keycloak: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Activar un usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario activado correctamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}/activar")
    public ResponseEntity<?> activarUsuario(@PathVariable String id) {
        try {
            keycloakService.setUsuarioActivo(id, true);
            return usuarioRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al activar en Keycloak: " + e.getMessage());
        }
    }

    @Operation(summary = "Desactivar un usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Usuario desactivado correctamente"),
        @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivarUsuario(@PathVariable String id) {
        try {
            keycloakService.setUsuarioActivo(id, false);
            return usuarioRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al desactivar en Keycloak: " + e.getMessage());
        }
    }
}
