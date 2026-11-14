package com.tiendazapatos.repository;

import com.tiendazapatos.model.EstadoPedido;
import com.tiendazapatos.model.Pedido;
import com.tiendazapatos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    /** REQ-ZAP-031: del más reciente al más antiguo. */
    List<Pedido> findByClienteOrderByFechaDesc(Usuario cliente);

    List<Pedido> findAllByOrderByFechaDesc();

    List<Pedido> findByEstadoOrderByFechaDesc(EstadoPedido estado);

    /** Pedidos de un rango de fechas (desde inclusive, hasta exclusive) para el reporte. */
    @Query("select p from Pedido p where p.estado in :estados and p.fecha >= :desde and p.fecha < :hasta order by p.fecha")
    List<Pedido> ventasEntre(@Param("estados") Collection<EstadoPedido> estados,
                             @Param("desde") LocalDateTime desde,
                             @Param("hasta") LocalDateTime hasta);
}
