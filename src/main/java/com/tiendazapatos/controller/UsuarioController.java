package com.tiendazapatos.controller;

import com.tiendazapatos.exception.ReglaNegocioException;
import com.tiendazapatos.form.UsuarioForm;
import com.tiendazapatos.model.Rol;
import com.tiendazapatos.model.Usuario;
import com.tiendazapatos.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** HU-03: el administrador crea usuarios con uno de los 5 roles y desactiva cuentas. */
@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioController(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @GetMapping
    public String lista(Model model) {
        model.addAttribute("usuarios", usuarios.findAllByOrderByNombreAsc());
        return "admin/usuarios";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new UsuarioForm());
        model.addAttribute("roles", Rol.values());
        return "admin/usuario-form";
    }

    /** REQ-ZAP-007: el usuario queda con exactamente un rol. */
    @PostMapping
    public String crear(@Valid @ModelAttribute("form") UsuarioForm form, BindingResult resultado,
                        Model model, RedirectAttributes ra) {
        String correo = form.getCorreo() == null ? "" : form.getCorreo().trim().toLowerCase();
        if (!resultado.hasFieldErrors("correo") && usuarios.existsByCorreoIgnoreCase(correo)) {
            resultado.rejectValue("correo", "duplicado", "El correo ya está registrado");
        }
        if (resultado.hasErrors()) {
            model.addAttribute("roles", Rol.values());
            return "admin/usuario-form";
        }
        usuarios.save(new Usuario(form.getNombre().trim(), correo, encoder.encode(form.getPassword()),
                form.getTelefono().trim(), form.getRol()));
        ra.addFlashAttribute("mensaje", "Usuario creado con el rol " + form.getRol().getEtiqueta());
        return "redirect:/admin/usuarios";
    }

    /** REQ-ZAP-008 y 010: se desactiva sin borrar el historial, pero nunca al último administrador activo. */
    @PostMapping("/{id}/desactivar")
    @Transactional
    public String desactivar(@PathVariable Long id, RedirectAttributes ra) {
        Usuario u = buscar(id);
        if (u.isActivo() && u.getRol() == Rol.ADMINISTRADOR
                && usuarios.countByRolAndActivoTrue(Rol.ADMINISTRADOR) <= 1) {
            throw new ReglaNegocioException("Debe existir al menos un administrador activo");
        }
        u.setActivo(false);
        ra.addFlashAttribute("mensaje", "Cuenta de " + u.getNombre() + " desactivada");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/activar")
    @Transactional
    public String activar(@PathVariable Long id, RedirectAttributes ra) {
        Usuario u = buscar(id);
        u.setActivo(true);
        ra.addFlashAttribute("mensaje", "Cuenta de " + u.getNombre() + " activada");
        return "redirect:/admin/usuarios";
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
