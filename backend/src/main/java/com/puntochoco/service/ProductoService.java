package com.puntochoco.service;

import com.puntochoco.model.Producto;
import com.puntochoco.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> getProductos(String search) {
        if (search != null && !search.isBlank()) {
            return productoRepository.searchActivos(search);
        }
        return productoRepository.findByFechaBajaIsNull();
    }

    public Producto obtenerProductoPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public Producto crearProducto(Producto data) {
        if (data.getPrecio() == null || data.getPrecio() <= 0) {
            throw new IllegalArgumentException("El precio debe ser un número positivo y entero");
        }

        String descNorm = data.getDescripcion().trim().replaceAll("\\s+", "").toUpperCase();
        Optional<Producto> existente = productoRepository.findDuplicado(descNorm, null);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto activo con esa descripción.");
        }

        data.setDescripcion(data.getDescripcion().trim().replaceAll("\\s+", " "));
        return productoRepository.save(data);
    }

    public Producto actualizarProducto(Long id, Producto data) {
        Producto producto = productoRepository.findById(id).orElse(null);
        if (producto == null) {
            throw new IllegalArgumentException("No existe el ID del producto");
        }

        if (data.getPrecio() == null || data.getPrecio() <= 0) {
            throw new IllegalArgumentException("El precio debe ser un número positivo y entero");
        }

        String descNorm = data.getDescripcion().trim().replaceAll("\\s+", "").toUpperCase();
        Optional<Producto> existente = productoRepository.findDuplicado(descNorm, id);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto activo con esa descripción.");
        }

        producto.setDescripcion(data.getDescripcion().trim().replaceAll("\\s+", " "));
        producto.setPrecio(data.getPrecio());
        if (data.getImagenURL() != null) {
            producto.setImagenURL(data.getImagenURL());
        }
        return productoRepository.save(producto);
    }

    public boolean eliminarProducto(Long id) {
        Optional<Producto> opt = productoRepository.findById(id);
        if (opt.isEmpty()) return false;

        Producto producto = opt.get();
        producto.setFechaBaja(LocalDateTime.now());
        productoRepository.save(producto);
        return true;
    }
}
