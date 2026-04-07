package com.puntochoco.config;

import com.puntochoco.model.Cliente;
import com.puntochoco.model.Producto;
import com.puntochoco.model.Rol;
import com.puntochoco.model.Usuario;
import com.puntochoco.model.Movimiento;
import com.puntochoco.repository.ClienteRepository;
import com.puntochoco.repository.MovimientoRepository;
import com.puntochoco.repository.ProductoRepository;
import com.puntochoco.repository.UsuarioRepository;

import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("develop")
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoRepository movimientoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UsuarioRepository usuarioRepository,
                      ClienteRepository clienteRepository,
                      ProductoRepository productoRepository,
                      MovimientoRepository movimientoRepository,
                      PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        cargarUsuarios();
        cargarClientes();
        cargarProductos();
        cargarMovimientos();
    }

    private void cargarUsuarios() {
        if (usuarioRepository.count() > 0) return;

        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setNombre("Administrador");
        admin.setApellido("Sistema");
        admin.setRol(Rol.ADMIN);
        admin.setActivo(true);
        usuarioRepository.save(admin);

        Usuario seller = new Usuario();
        seller.setUsername("vendedor");
        seller.setPassword(passwordEncoder.encode("vendedor"));
        seller.setNombre("Juan");
        seller.setApellido("Pérez");
        seller.setRol(Rol.SELLER);
        seller.setActivo(true);
        usuarioRepository.save(seller);

        Usuario user = new Usuario();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("user"));
        user.setNombre("María");
        user.setApellido("García");
        user.setRol(Rol.USER);
        user.setActivo(true);
        usuarioRepository.save(user);
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
