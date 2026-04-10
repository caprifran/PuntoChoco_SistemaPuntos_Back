package com.puntochoco.controller;

import com.puntochoco.dto.AuthResponse;
import com.puntochoco.dto.LoginRequest;
import com.puntochoco.dto.RegisterRequest;
import com.puntochoco.model.Rol;
import com.puntochoco.model.Usuario;
import com.puntochoco.repository.UsuarioRepository;
import com.puntochoco.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para login y registro de usuarios")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                          UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Iniciar sesión para obtener un token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales inválidas"));
        }

        Usuario usuario = usuarioRepository.findByUsernameAndActivoTrue(request.getUsername())
                .orElseThrow();

        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRol().name());

        return ResponseEntity.ok(new AuthResponse(
                token,
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol().name()
        ));
    }

    @Operation(summary = "Registrar un nuevo usuario (rol USER por defecto)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente"),
            @ApiResponse(responseCode = "409", description = "El nombre de usuario ya existe")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "El nombre de usuario ya existe"));
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setRol(Rol.USER);
        usuario.setActivo(true);

        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRol().name());

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                token,
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol().name()
        ));
    }
}
