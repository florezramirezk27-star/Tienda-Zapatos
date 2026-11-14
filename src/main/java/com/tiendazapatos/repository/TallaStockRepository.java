package com.tiendazapatos.repository;

import com.tiendazapatos.model.TallaStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TallaStockRepository extends JpaRepository<TallaStock, Long> {

    /**
     * Bloquea la fila para que dos clientes no compren el mismo último par (AC-024.1).
     * Debe llamarse dentro de una transacción.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TallaStock t where t.id = :id")
    Optional<TallaStock> bloquear(@Param("id") Long id);

    /** Bloquea, en orden, todas las tallas de un pedido (al cancelarlo y devolver el stock). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TallaStock t where t.id in "
            + "(select d.tallaStock.id from DetallePedido d where d.pedido.id = :pedidoId) order by t.id")
    List<TallaStock> bloquearPorPedido(@Param("pedidoId") Long pedidoId);

    /** Tallas de zapatos activos, para la pantalla de venta presencial. */
    @Query("select t from TallaStock t join fetch t.zapato z where z.activo = true order by z.nombre, t.talla")
    List<TallaStock> disponiblesParaVenta();
}
