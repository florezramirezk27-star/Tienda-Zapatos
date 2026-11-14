package com.tiendazapatos.form;

import com.tiendazapatos.model.Rol;
import jakarta.validation.constraints.NotNull;

/** HU-03: el administrador crea un usuario y le asigna uno de los 5 roles. */
public class UsuarioForm extends RegistroForm {

    @NotNull(message = "Selecciona un rol")
    private Rol rol;

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
