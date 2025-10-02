package com.taller5.aggregator.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Intenta reservar stock de forma atómica.
     * Suma +1 a version para mantener el lock optimista, evita SELECT + UPDATE.
     * Devuelve #filas actualizadas (0 = sin stock o colisión concurrente).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = false)
    @Query("""
           update Product p
              set p.stock = p.stock - :qty,
                  p.version = p.version + 1
            where p.id = :id
              and p.stock >= :qty
           """)
    int tryReserve(@Param("id") long productId, @Param("qty") int quantity);
}
