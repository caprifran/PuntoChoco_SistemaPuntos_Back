package com.puntochoco.controller;

import com.puntochoco.model.Producto;
import com.puntochoco.service.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public List<Producto> getProductos(@RequestParam(required = false) String search) {
        return productoService.getProductos(search);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProducto(@PathVariable Long id) {
        Producto producto = productoService.obtenerProductoPorId(id);
        if (producto == null) {
            return ResponseEntity.status(404).body(Map.of("msg", "Producto no encontrado"));
        }
        return ResponseEntity.ok(producto);
    }

    @PostMapping
    public ResponseEntity<?> createProducto(@RequestBody Producto data) {
        try {
            Producto nuevo = productoService.crearProducto(data);
            return ResponseEntity.ok(nuevo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al crear producto"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProducto(@PathVariable Long id, @RequestBody Producto data) {
        try {
            Producto actualizado = productoService.actualizarProducto(id, data);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al modificar el producto"));
        }
    }

    @PutMapping("/{id}/bajaProducto")
    public ResponseEntity<?> deleteProducto(@PathVariable Long id) {
        boolean eliminado = productoService.eliminarProducto(id);
        if (!eliminado) {
            return ResponseEntity.status(404).body(Map.of("msg", "Producto no encontrado"));
        }
        return ResponseEntity.ok(Map.of("msg", "Producto eliminado"));
    }
}
