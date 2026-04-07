package com.puntochoco.specification;

import com.puntochoco.model.Cliente;
import com.puntochoco.model.Movimiento;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MovimientoSpecification {

    public static Specification<Movimiento> conFiltros(
            Long clienteId,
            LocalDateTime desde,
            LocalDateTime hasta,
            String tipo,
            String clienteDesc,
            String nroFactura
    ) {
        return (root, query, cb) -> {

            root.fetch("cliente", JoinType.INNER);
            query.distinct(true);

            Join<Movimiento, Cliente> cliente = root.join("cliente");

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isNull(cliente.get("fechaBaja")));
            predicates.add(cb.isNull(root.get("fechaBaja")));

            if (clienteId != null) {
                predicates.add(cb.equal(cliente.get("id"), clienteId));
            }

            if (desde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), desde));
            }

            if (hasta != null) {
                predicates.add(cb.lessThan(root.get("fecha"), hasta));
            }

            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }

            if (clienteDesc != null) {
                String like = "%" + clienteDesc.toLowerCase() + "%";

                Predicate nombre = cb.like(cb.lower(cliente.get("nombre")), like);
                Predicate apellido = cb.like(cb.lower(cliente.get("apellido")), like);

                predicates.add(cb.or(nombre, apellido));
            }

            if (nroFactura != null) {
                Predicate facturaLike = cb.like(cb.lower(root.get("nroFactura")), "%" + nroFactura.toLowerCase() + "%");
                predicates.add(facturaLike);
            }

            query.orderBy(cb.desc(root.get("fecha")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}