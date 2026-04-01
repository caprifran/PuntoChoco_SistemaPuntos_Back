package com.puntochoco.controller;

import com.puntochoco.dto.CambiarRolRequest;
import com.puntochoco.model.Usuario;
import com.puntochoco.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

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
