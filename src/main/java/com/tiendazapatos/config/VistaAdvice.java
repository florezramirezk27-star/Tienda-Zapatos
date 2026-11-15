package com.tiendazapatos.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Solo presentación: indica a las plantillas si la página que se está mostrando es la principal ("/"),
 * para que el pie de página se renderice únicamente ahí. No toca la lógica de negocio ni las rutas.
 */
@ControllerAdvice
public class VistaAdvice {

    @ModelAttribute("inicio")
    public boolean inicio(HttpServletRequest request) {
        return request.getRequestURI().equals(request.getContextPath() + "/");
    }

    /** Ruta actual sin el context path, para marcar el enlace activo de la navegación. */
    @ModelAttribute("rutaActual")
    public String rutaActual(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contexto = request.getContextPath();
        return contexto.isEmpty() ? uri : uri.substring(contexto.length());
    }
}
