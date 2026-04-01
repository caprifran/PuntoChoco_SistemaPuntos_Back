package com.puntochoco.controller;

import com.puntochoco.model.Cliente;
import com.puntochoco.model.Movimiento;
import com.puntochoco.service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<Cliente> getClientes(@RequestParam(required = false) String search) {
        return clienteService.getClientes(search);
    }

    @GetMapping("/top")
    public List<Map<String, Object>> getClientesTop(@RequestParam(required = false) String tipo) {
        return clienteService.getClientesTop(tipo);
    }

    @GetMapping("/historico")
    public ResponseEntity<?> getHistorico(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String fDesde,
            @RequestParam(required = false) String fHasta) {
        try {
            List<Map<String, Object>> resultado = clienteService.obtenerMovimientos(clienteId, fDesde, fHasta);
            if (resultado.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al obtener histórico"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCliente(@PathVariable Long id) {
        Map<String, Object> cliente = clienteService.obtenerClientePorId(id);
        if (cliente == null) {
            return ResponseEntity.status(404).body(Map.of("msg", "Cliente no encontrado"));
        }
        return ResponseEntity.ok(cliente);
    }

    @PostMapping
    public ResponseEntity<?> createCliente(@RequestBody Cliente data) {
        try {
            Cliente nuevo = clienteService.crearCliente(data);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCliente(@PathVariable Long id, @RequestBody Cliente data) {
        try {
            Cliente actualizado = clienteService.actualizarCliente(id, data);
            return ResponseEntity.ok(actualizado);
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al modificar el cliente"));
        }
    }

    @PutMapping("/{id}/bajaCliente")
    public ResponseEntity<?> deleteCliente(@PathVariable Long id) {
        boolean eliminado = clienteService.eliminarCliente(id);
        if (!eliminado) {
            return ResponseEntity.status(404).body(Map.of("msg", "Cliente no encontrado"));
        }
        return ResponseEntity.ok(Map.of("msg", "Cliente eliminado"));
    }

    @PutMapping("/{id}/agregarPuntos")
    public ResponseEntity<?> agregarPuntos(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        try {
            int puntos = body.getOrDefault("puntos", 0);
            Movimiento mov = clienteService.agregarPuntos(id, puntos);
            return ResponseEntity.ok(mov);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/descontarPuntos")
    public ResponseEntity<?> descontarPuntos(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long idProducto = Long.valueOf(body.get("idProducto").toString());
        int cantProd = Integer.parseInt(body.get("cantProd").toString());
        Object result = clienteService.descontarPuntos(id, idProducto, cantProd);
        return ResponseEntity.ok(result);
    }
}
