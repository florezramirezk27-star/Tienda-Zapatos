package com.tiendazapatos.repository;

import com.tiendazapatos.model.Rol;
import com.tiendazapatos.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);

    /** RN-04: siempre debe existir al menos un administrador activo. */
    long countByRolAndActivoTrue(Rol rol);

    List<Usuario> findAllByOrderByNombreAsc();
}
