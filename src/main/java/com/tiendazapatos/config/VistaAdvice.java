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
}
