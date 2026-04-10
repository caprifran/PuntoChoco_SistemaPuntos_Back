package com.puntochoco.controller;

import com.puntochoco.model.Cliente;
import com.puntochoco.model.Movimiento;
import com.puntochoco.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Gestión de clientes y sus puntos")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(summary = "Listar clientes activos, con búsqueda opcional por nombre, apellido o DNI")
    @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida correctamente")
    @GetMapping
    public List<Cliente> getClientes(@RequestParam(required = false) String search) {
        return clienteService.getClientes(search);
    }

    @Operation(summary = "Obtener ranking de clientes top por puntos o consumos")
    @ApiResponse(responseCode = "200", description = "Ranking obtenido correctamente")
    @GetMapping("/top")
    public List<Map<String, Object>> getClientesTop(@RequestParam(required = false) String tipo) {
        return clienteService.getClientesTop(tipo);
    }

    @Operation(summary = "Obtener histórico de movimientos con filtros opcionales")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico obtenido correctamente"),
            @ApiResponse(responseCode = "500", description = "Error al obtener histórico")
    })
    @GetMapping("/historico")
    public ResponseEntity<?> getHistorico(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String fDesde,
            @RequestParam(required = false) String fHasta,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String clienteDesc,
            @RequestParam(required = false) String nroFactura) {
        try {
            List<Map<String, Object>> resultado = clienteService.obtenerMovimientos(clienteId, fDesde, fHasta, tipo,
                    clienteDesc, nroFactura);
            if (resultado.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener histórico"));
        }
    }

    @Operation(summary = "Obtener un cliente por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getCliente(@PathVariable Long id) {
        Map<String, Object> cliente = clienteService.obtenerClientePorId(id);
        if (cliente == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("msg", "Cliente no encontrado"));
        }
        return ResponseEntity.ok(cliente);
    }

    @Operation(summary = "Crear un nuevo cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Ya existe un cliente activo con ese DNI")
    })
    @PostMapping
    public ResponseEntity<?> createCliente(@RequestBody Cliente data) {
        try {
            Cliente nuevo = clienteService.crearCliente(data);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Actualizar datos de un cliente existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "No existe el ID del cliente o DNI duplicado"),
            @ApiResponse(responseCode = "500", description = "Error al modificar el cliente")
    })
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al modificar el cliente"));
        }
    }

    @Operation(summary = "Dar de baja un cliente (baja lógica)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente eliminado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PutMapping("/{id}/bajaCliente")
    public ResponseEntity<?> deleteCliente(@PathVariable Long id) {
        boolean eliminado = clienteService.eliminarCliente(id);
        if (!eliminado) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("msg", "Cliente no encontrado"));
        }
        return ResponseEntity.ok(Map.of("msg", "Cliente eliminado"));
    }

    @Operation(summary = "Agregar puntos a un cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Puntos agregados correctamente"),
            @ApiResponse(responseCode = "400", description = "Puntos inválidos, factura duplicada o cliente no encontrado")
    })
    @PutMapping("/{id}/agregarPuntos")
    public ResponseEntity<?> agregarPuntos(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            int puntos = (int) body.getOrDefault("puntos", 0);
            String nroFactura = (String) body.getOrDefault("nroFactura", "");
            Movimiento mov = clienteService.agregarPuntos(id, puntos, nroFactura);
            return ResponseEntity.ok(mov);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Descontar puntos de un cliente canjeando un producto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Puntos descontados correctamente o mensaje de error en el body"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno al descontar puntos")
    })
    @PutMapping("/{id}/descontarPuntos")
    public ResponseEntity<?> descontarPuntos(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long idProducto = Long.valueOf(body.get("idProducto").toString());
        int cantProd = Integer.parseInt(body.get("cantProd").toString());
        Object result = clienteService.descontarPuntos(id, idProducto, cantProd);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Dar de baja un movimiento (solo ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Movimiento dado de baja"),
            @ApiResponse(responseCode = "404", description = "Movimiento no encontrado o ya dado de baja")
    })
    @PutMapping("/historico/{id}/baja")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> bajaMovimiento(@PathVariable Long id) {
        boolean eliminado = clienteService.bajaMovimiento(id);
        if (!eliminado) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("msg", "Movimiento no encontrado o ya dado de baja"));
        }
        return ResponseEntity.ok(Map.of("msg", "Movimiento dado de baja"));
    }
}
