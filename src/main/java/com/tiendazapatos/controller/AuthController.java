package com.tiendazapatos.controller;

import com.tiendazapatos.form.RegistroForm;
import com.tiendazapatos.model.Rol;
import com.tiendazapatos.model.Usuario;
import com.tiendazapatos.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** HU-01 (inicio de sesión) y HU-02 (registro de clientes). */
@Controller
public class AuthController {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public AuthController(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String formularioRegistro(Model model) {
        model.addAttribute("form", new RegistroForm());
        return "registro";
    }

    /** REQ-ZAP-004, 005 y 006. */
    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("form") RegistroForm form, BindingResult resultado,
                            RedirectAttributes ra) {
        String correo = form.getCorreo() == null ? "" : form.getCorreo().trim().toLowerCase();
        if (!resultado.hasFieldErrors("correo") && usuarios.existsByCorreoIgnoreCase(correo)) {
            resultado.rejectValue("correo", "duplicado", "El correo ya está registrado");
        }
        if (resultado.hasErrors()) {
            return "registro";
        }
        Usuario nuevo = new Usuario(form.getNombre().trim(), correo, encoder.encode(form.getPassword()),
                form.getTelefono().trim(), Rol.CLIENTE);
        usuarios.save(nuevo);
        ra.addFlashAttribute("mensaje", "Cuenta creada. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }
}
