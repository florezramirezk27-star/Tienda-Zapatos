package com.tiendazapatos.controller;

import com.tiendazapatos.exception.ReglaNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Convierte una regla de negocio violada en un mensaje de error para el usuario y lo devuelve
 * a la página desde la que vino. Como el método del controlador es @Transactional, la excepción
 * ya revirtió la transacción cuando llega aquí.
 */
@ControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(ReglaNegocioException.class)
    public String reglaDeNegocio(ReglaNegocioException ex, HttpServletRequest request, RedirectAttributes ra) {
        ra.addFlashAttribute("error", ex.getMessage());
        return "redirect:" + paginaAnterior(request);
    }

    /** Solo vuelve a rutas de esta misma aplicación; cualquier otra cosa lleva al inicio. */
    private String paginaAnterior(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        String base = ServletUriComponentsBuilder.fromContextPath(request).build().toUriString();
        if (referer != null && referer.startsWith(base)) {
            String ruta = referer.substring(base.length());
            if (ruta.startsWith("/") && !ruta.startsWith("//")) {
                return ruta;
            }
        }
        return "/";
    }
}
