package com.puntochoco.controller;

import com.puntochoco.model.Producto;
import com.puntochoco.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Gestión de productos canjeables")
@SecurityRequirement(name = "bearerAuth")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(summary = "Listar productos activos, con búsqueda opcional por descripción")
    @ApiResponse(responseCode = "200", description = "Lista de productos obtenida correctamente")
    @GetMapping
    public List<Producto> getProductos(@RequestParam(required = false) String search) {
        return productoService.getProductos(search);
    }

    @Operation(summary = "Obtener un producto por su ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto encontrado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getProducto(@PathVariable Long id) {
        Producto producto = productoService.obtenerProductoPorId(id);
        if (producto == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("msg", "Producto no encontrado"));
        }
        return ResponseEntity.ok(producto);
    }

    @Operation(summary = "Crear un nuevo producto")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto creado correctamente"),
        @ApiResponse(responseCode = "400", description = "Precio inválido o descripción duplicada"),
        @ApiResponse(responseCode = "500", description = "Error al crear producto")
    })
    @PostMapping
    public ResponseEntity<?> createProducto(@RequestBody Producto data) {
        try {
            Producto nuevo = productoService.crearProducto(data);
            return ResponseEntity.ok(nuevo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al crear producto"));
        }
    }

    @Operation(summary = "Actualizar un producto existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "ID inexistente, precio inválido o descripción duplicada"),
        @ApiResponse(responseCode = "500", description = "Error al modificar el producto")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProducto(@PathVariable Long id, @RequestBody Producto data) {
        try {
            Producto actualizado = productoService.actualizarProducto(id, data);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error al modificar el producto"));
        }
    }

    @Operation(summary = "Dar de baja un producto (baja lógica)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto eliminado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @PutMapping("/{id}/bajaProducto")
    public ResponseEntity<?> deleteProducto(@PathVariable Long id) {
        boolean eliminado = productoService.eliminarProducto(id);
        if (!eliminado) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("msg", "Producto no encontrado"));
        }
        return ResponseEntity.ok(Map.of("msg", "Producto eliminado"));
    }
}
