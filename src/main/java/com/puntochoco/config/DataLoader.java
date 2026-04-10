package com.puntochoco.config;

import com.puntochoco.dto.RegisterRequest;
import com.puntochoco.model.*;
import com.puntochoco.repository.ClienteRepository;
import com.puntochoco.repository.MovimientoRepository;
import com.puntochoco.repository.ProductoRepository;
import com.puntochoco.repository.UsuarioRepository;
import com.puntochoco.service.KeycloakService;
import org.keycloak.representations.idm.UserRepresentation;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("develop")
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoRepository movimientoRepository;
    private final KeycloakService keycloakService;

    public DataLoader(UsuarioRepository usuarioRepository,
                      ClienteRepository clienteRepository,
                      ProductoRepository productoRepository,
                      MovimientoRepository movimientoRepository,
                      KeycloakService keycloakService) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
        this.keycloakService = keycloakService;
    }

    @Override
    public void run(String... args) {
        cargarUsuarios();
        cargarClientes();
        cargarProductos();
        cargarMovimientos();
    }

    private void cargarUsuarios() {
        // 1. Asegurar usuarios semilla (opcional, si no existen en Keycloak los crea)
        crearOSincronizarUsuario("admin", "admin@puntochoco.com", "Administrador", "Sistema", Rol.ADMIN);
        crearOSincronizarUsuario("vendedor", "vendedor@puntochoco.com", "Juan", "Pérez", Rol.SELLER);
        crearOSincronizarUsuario("user", "user@example.com", "María", "García", Rol.USER);

        // 2. Sincronizar TODOS los demás usuarios que existan en Keycloak
        System.out.println("DataLoader: Iniciando sincronización masiva desde Keycloak...");
        try {
            List<UserRepresentation> allKeycloakUsers = keycloakService.listarTodosLosUsuarios();
            for (UserRepresentation uk : allKeycloakUsers) {
                // Si el usuario no fue procesado arriba (por username), lo procesamos ahora
                if (!usuarioRepository.existsById(uk.getId())) {
                    actualizarOLocalizarUsuario(uk);
                }
            }
            System.out.println("DataLoader: Sincronización masiva finalizada.");
        } catch (Exception e) {
            System.err.println("DataLoader: Error en sincronización masiva: " + e.getMessage());
        }
    }

    private void actualizarOLocalizarUsuario(UserRepresentation uk) {
        try {
            List<String> roles = keycloakService.getRoles(uk.getId());
            Rol rolLocal = mapearRol(roles);

            Usuario u = new Usuario();
            u.setId(uk.getId());
            u.setUsername(uk.getUsername());
            u.setEmail(uk.getEmail());
            u.setNombre(uk.getFirstName() != null ? uk.getFirstName() : uk.getUsername());
            u.setApellido(uk.getLastName() != null ? uk.getLastName() : "");
            u.setRol(rolLocal);
            u.setActivo(uk.isEnabled());
            usuarioRepository.save(u);
            System.out.println("DataLoader: Usuario '" + uk.getUsername() + "' sincronizado desde Keycloak.");
        } catch (Exception e) {
            System.err.println("DataLoader: Error localizando usuario '" + uk.getUsername() + "': " + e.getMessage());
        }
    }

    private Rol mapearRol(List<String> roles) {
        if (roles.contains("ADMIN")) return Rol.ADMIN;
        if (roles.contains("SELLER")) return Rol.SELLER;
        return Rol.USER;
    }

    private void crearOSincronizarUsuario(String username, String email, String nombre, String apellido, Rol rol) {
        try {
            String keycloakId = keycloakService.getUserIdByUsername(username);

            if (keycloakId == null) {
                // No existe en Keycloak, lo creamos
                RegisterRequest reg = new RegisterRequest();
                reg.setUsername(username);
                reg.setEmail(email);
                reg.setNombre(nombre);
                reg.setApellido(apellido);
                reg.setPassword(username); // Usamos el username como password por defecto
                reg.setRol(rol.name());
                
                keycloakId = keycloakService.crearUsuario(reg);
                System.out.println("DataLoader: Usuario '" + username + "' creado en Keycloak.");
            } else {
                System.out.println("DataLoader: Usuario '" + username + "' ya existe en Keycloak (ID: " + keycloakId + "). Sincronizando...");
                // Aseguramos que tenga el rol correcto en Keycloak también
                keycloakService.asignarRol(keycloakId, rol.name());
            }

            // Guardar en base de datos local
            Usuario u = new Usuario();
            u.setId(keycloakId);
            u.setUsername(username);
            u.setEmail(email);
            u.setNombre(nombre);
            u.setApellido(apellido);
            u.setRol(rol);
            u.setActivo(true);
            usuarioRepository.save(u);

        } catch (Exception e) {
            System.err.println("DataLoader: Error procesando usuario '" + username + "': " + e.getMessage());
        }
    }

    private void cargarClientes() {
        if (clienteRepository.count() > 0) return;

        String[][] datos = {
            {"Carlos", "López", "30123456"},
            {"Ana", "Martínez", "28654321"},
            {"Pedro", "Rodríguez", "35987654"},
            {"Laura", "Fernández", "32456789"},
            {"Diego", "González", "29876543"}
        };

        for (String[] d : datos) {
            Cliente c = new Cliente();
            c.setNombre(d[0]);
            c.setApellido(d[1]);
            c.setDni(d[2]);
            clienteRepository.save(c);
        }
    }

    private void cargarProductos() {
        if (productoRepository.count() > 0) return;

        Object[][] datos = {
            {"Caja de Bombones x12", 500},
            {"Tableta de Chocolate con Leche", 300},
            {"Tableta de Chocolate Amargo 70%", 350},
            {"Alfajor de Chocolate Triple", 150},
            {"Huevo de Pascua Mediano", 800},
            {"Bolsa de Trufas x6", 450},
            {"Barra de Chocolate Blanco", 280},
            {"Fondue de Chocolate para 2", 600}
        };

        for (Object[] d : datos) {
            Producto p = new Producto();
            p.setDescripcion((String) d[0]);
            p.setPrecio((Integer) d[1]);
            productoRepository.save(p);
        }
    }

    private void cargarMovimientos() {
        if (movimientoRepository.count() > 0) return;

        Cliente carlos = clienteRepository.findByDniAndFechaBajaIsNull("30123456").orElse(null);
        if (carlos == null) return;

        LocalDateTime ahora = LocalDateTime.now();

        Movimiento alta = new Movimiento();
        alta.setTipo("alta");
        alta.setPuntos(1000);
        alta.setFecha(ahora);
        alta.setVencimiento(ahora.plusMonths(6));
        alta.setCliente(carlos);
        movimientoRepository.save(alta);

        Movimiento consumo = new Movimiento();
        consumo.setTipo("consumo");
        consumo.setPuntos(-500);
        consumo.setFecha(ahora);
        consumo.setDetalle("Caja de Bombones x12 X1");
        consumo.setCliente(carlos);
        movimientoRepository.save(consumo);
    }
}
