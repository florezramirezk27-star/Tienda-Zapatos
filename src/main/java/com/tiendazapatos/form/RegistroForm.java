package com.tiendazapatos.form;

import com.tiendazapatos.util.PoliticaContrasena;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** HU-02: registro de un cliente. */
public class RegistroForm {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre admite máximo 100 caracteres")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es válido")
    @Size(max = 150, message = "El correo admite máximo 150 caracteres")
    private String correo;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9+ ]{7,20}$", message = "El teléfono debe tener entre 7 y 20 dígitos")
    private String telefono;

    @NotNull(message = PoliticaContrasena.MENSAJE)
    @Pattern(regexp = PoliticaContrasena.REGEX, message = PoliticaContrasena.MENSAJE)
    private String password;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
