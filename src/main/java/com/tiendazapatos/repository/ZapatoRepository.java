package com.tiendazapatos.repository;

import com.tiendazapatos.model.Zapato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ZapatoRepository extends JpaRepository<Zapato, Long>, JpaSpecificationExecutor<Zapato> {

    boolean existsByReferencia(String referencia);

    List<Zapato> findAllByOrderByNombreAsc();

    @Query("select distinct z.marca from Zapato z where z.activo = true order by z.marca")
    List<String> marcasActivas();

    @Query("select distinct z.categoria from Zapato z where z.activo = true order by z.categoria")
    List<String> categoriasActivas();
}
