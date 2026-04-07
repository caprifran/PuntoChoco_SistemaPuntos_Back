package com.puntochoco.service;

import com.puntochoco.model.Cliente;
import com.puntochoco.model.Movimiento;
import com.puntochoco.repository.ClienteRepository;
import com.puntochoco.repository.MovimientoRepository;

import com.puntochoco.specification.MovimientoSpecification;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final MovimientoRepository movimientoRepository;
    private final ProductoService productoService;

    public ClienteService(ClienteRepository clienteRepository,
                          MovimientoRepository movimientoRepository,
                          ProductoService productoService) {
        this.clienteRepository = clienteRepository;
        this.movimientoRepository = movimientoRepository;
        this.productoService = productoService;
    }

    public List<Cliente> getClientes(String search) {
        List<Cliente> clientes;
        if (search != null && !search.isBlank()) {
            clientes = clienteRepository.searchActivos(search);
        } else {
            clientes = clienteRepository.findByFechaBajaIsNull();
        }

        LocalDateTime ahora = LocalDateTime.now();
        for (Cliente c : clientes) {
            Long puntos = movimientoRepository.sumPuntosVigentes(c.getId(), ahora);
            c.setTotalPuntos(puntos);
        }
        return clientes;
    }

    public Map<String, Object> obtenerClientePorId(Long id) {
        Optional<Cliente> opt = clienteRepository.findById(id);
        if (opt.isEmpty()) return null;

        Cliente cliente = opt.get();
        Long totalPuntos = movimientoRepository.sumPuntosVigentes(id, LocalDateTime.now());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", cliente.getId());
        result.put("nombre", cliente.getNombre());
        result.put("apellido", cliente.getApellido());
        result.put("dni", cliente.getDni());
        result.put("fechaBaja", cliente.getFechaBaja());
        result.put("createdAt", cliente.getCreatedAt());
        result.put("updatedAt", cliente.getUpdatedAt());
        result.put("totalPuntos", totalPuntos);
        return result;
    }

    public Cliente crearCliente(Cliente data) {
        Optional<Cliente> existente = clienteRepository.findByDniAndFechaBajaIsNull(data.getDni());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente activo con ese DNI");
        }
        return clienteRepository.save(data);
    }

    public Cliente actualizarCliente(Long id, Cliente data) {
        Optional<Cliente> opt = clienteRepository.findById(id);
        if (opt.isEmpty()) {
            throw new NoSuchElementException("No existe el ID del cliente");
        }

        Optional<Cliente> duplicado = clienteRepository.findByDniAndFechaBajaIsNullAndIdNot(data.getDni(), id);
        if (duplicado.isPresent()) {
            throw new IllegalArgumentException("Ya existe un cliente activo con ese DNI");
        }

        Cliente cliente = opt.get();
        cliente.setNombre(data.getNombre());
        cliente.setApellido(data.getApellido());
        cliente.setDni(data.getDni());
        return clienteRepository.save(cliente);
    }

    public boolean eliminarCliente(Long id) {
        Optional<Cliente> opt = clienteRepository.findById(id);
        if (opt.isEmpty()) return false;

        Cliente cliente = opt.get();
        cliente.setFechaBaja(LocalDateTime.now());
        clienteRepository.save(cliente);
        return true;
    }

    public Movimiento agregarPuntos(Long clienteId, int puntos, String nroFactura) {
        if (puntos <= 0) {
            throw new IllegalArgumentException("Los puntos deben ser un número positivo y entero");
        }

        if (nroFactura != null && !nroFactura.isBlank() && movimientoRepository.existsByNroFactura(nroFactura)) {
            throw new IllegalArgumentException("Ya existe un movimiento con el número de factura: " + nroFactura);
        }

        Optional<Cliente> opt = clienteRepository.findById(clienteId);
        if (opt.isEmpty()) {
            throw new NoSuchElementException("Cliente no encontrado");
        }

        LocalDateTime vencimiento = LocalDateTime.now().plusMonths(6);

        Movimiento mov = new Movimiento();
        mov.setTipo("alta");
        mov.setPuntos(puntos);
        mov.setFecha(LocalDateTime.now());
        mov.setVencimiento(vencimiento);
        mov.setCliente(opt.get());
        mov.setNroFactura(nroFactura);
        return movimientoRepository.save(mov);
    }

    public Object descontarPuntos(Long clienteId, Long productoId, int cantidad) {
        Map<String, Object> cliente = obtenerClientePorId(clienteId);
        if (cliente == null) {
            return Map.of("code", -1, "msj", "No se pudo obtener el cliente.");
        }

        var producto = productoService.obtenerProductoPorId(productoId);
        if (producto == null) {
            return Map.of("code", -1, "msj", "No se pudo obtener el producto.");
        }

        long totalPuntos = (Long) cliente.get("totalPuntos");
        if (totalPuntos < (long) producto.getPrecio() * cantidad) {
            return Map.of("code", -1, "msj", "Puntos no disponibles para este consumo.");
        }

        int puntosDescontar = (producto.getPrecio() * cantidad) * -1;

        Movimiento mov = new Movimiento();
        mov.setTipo("consumo");
        mov.setPuntos(puntosDescontar);
        mov.setFecha(LocalDateTime.now());
        mov.setCliente(clienteRepository.findById(clienteId).orElseThrow());
        mov.setDetalle(producto.getDescripcion() + " X" + cantidad);
        return movimientoRepository.save(mov);
    }
    
    public List<Map<String, Object>> obtenerMovimientos(
            Long clienteId, String fDesde, String fHasta, String tipo, String clienteDesc, String nroFactura) {

        LocalDateTime desde = (fDesde != null && !fDesde.isBlank())
                ? LocalDate.parse(fDesde.substring(0, 10)).atStartOfDay()
                : null;

        LocalDateTime hasta = (fHasta != null && !fHasta.isBlank())
                ? LocalDate.parse(fHasta.substring(0, 10)).plusDays(1).atStartOfDay()
                : null;

        String tipoFiltro = (tipo != null && !tipo.isBlank()) ? tipo.toLowerCase() : null;
        String clienteDescFiltro = (clienteDesc != null && !clienteDesc.isBlank()) ? clienteDesc : null;

        Specification<Movimiento> spec = MovimientoSpecification.conFiltros(
                clienteId, desde, hasta, tipoFiltro, clienteDescFiltro, nroFactura
        );

        List<Movimiento> movimientos = movimientoRepository.findAll(spec);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Movimiento m : movimientos) {
            Map<String, Object> map = new LinkedHashMap<>();

            map.put("id", m.getId());
            map.put("tipo", m.getTipo());
            map.put("puntos", m.getPuntos());
            map.put("fecha", m.getFecha());
            map.put("vencimiento", m.getVencimiento());
            map.put("detalle", m.getDetalle());
            map.put("clienteId", m.getClienteId());

            if (clienteId == null) {
                Cliente c = m.getCliente();
                map.put("clienteCompleto", c.getApellido() + ", " + c.getNombre());
                map.put("DNI", c.getDni());
            }

            map.put("nroFactura", m.getNroFactura());

            result.add(map);
        }

        return result;
    }

    public List<Map<String, Object>> getClientesTop(String tipo) {
        List<Object[]> rows;
        if ("consumidores".equals(tipo)) {
            rows = movimientoRepository.findTopConsumidores();
        } else {
            rows = movimientoRepository.findTopPuntos();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", row[0]);
            map.put("cliente", row[1]);
            map.put("dni", row[2]);
            map.put("puntos", row[3]);
            result.add(map);
        }
        return result;
    }

    public boolean bajaMovimiento(Long id) {
        Optional<Movimiento> opt = movimientoRepository.findById(id);
        if (opt.isEmpty()) return false;

        Movimiento mov = opt.get();
        if (mov.getFechaBaja() != null) return false;

        mov.setFechaBaja(LocalDateTime.now());
        movimientoRepository.save(mov);
        return true;
    }
}
