package com.tiendazapatos.config;

import com.tiendazapatos.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * ADR-004: cada ruta declara qué rol la puede usar; lo que no está declarado exige sesión iniciada.
 * Un rol no autorizado recibe HTTP 403 (REQ-ZAP-009); sin sesión se redirige al login.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** REQ-ZAP-008: una cuenta inactiva no puede iniciar sesión. */
    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return correo -> usuarios.findByCorreoIgnoreCase(correo)
                .map(u -> User.withUsername(u.getCorreo())
                        .password(u.getPassword())
                        .roles(u.getRol().name())
                        .disabled(!u.isActivo())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    @Bean
    SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(a -> a
                .requestMatchers("/", "/catalogo", "/zapatos/**", "/registro", "/login",
                        "/error", "/error/**", "/css/**", "/js/**", "/img/**",
                        "/fonts/**", "/vendor/**", "/favicon.ico").permitAll()
                // HU-03: gestión de usuarios y roles
                .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
                // HU-04: catálogo de zapatos
                .requestMatchers("/almacen/zapatos/**").hasAnyRole("ADMINISTRADOR", "ALMACENISTA")
                // HU-06: inventario (solo el almacenista lo modifica)
                .requestMatchers(HttpMethod.GET, "/almacen/inventario").hasAnyRole("ADMINISTRADOR", "GERENTE", "ALMACENISTA")
                .requestMatchers("/almacen/inventario/**").hasRole("ALMACENISTA")
                // HU-07, 08 y 11: compra del cliente
                .requestMatchers("/carrito/**", "/pedidos/**", "/mis-pedidos/**").hasRole("CLIENTE")
                // HU-09: venta presencial
                .requestMatchers("/ventas/**").hasRole("VENDEDOR")
                // HU-10: el vendedor confirma, entrega y cancela; el almacenista envía
                .requestMatchers(HttpMethod.GET, "/gestion/pedidos", "/gestion/pedidos/*")
                        .hasAnyRole("ADMINISTRADOR", "GERENTE", "VENDEDOR", "ALMACENISTA")
                .requestMatchers(HttpMethod.POST, "/gestion/pedidos/*/enviar").hasRole("ALMACENISTA")
                .requestMatchers(HttpMethod.POST, "/gestion/pedidos/*/confirmar", "/gestion/pedidos/*/entregar",
                        "/gestion/pedidos/*/cancelar").hasRole("VENDEDOR")
                .requestMatchers("/gestion/**").denyAll()
                // HU-12: facturas (el cliente solo las de sus pedidos, se valida en el controlador)
                .requestMatchers("/facturas/**").hasAnyRole("VENDEDOR", "CLIENTE")
                // HU-13: reporte de ventas
                .requestMatchers("/reportes/**").hasRole("GERENTE")
                .anyRequest().authenticated())
            .formLogin(f -> f
                .loginPage("/login")
                .usernameParameter("correo")
                .passwordParameter("password")
                .successHandler(destinoPorRol())
                .failureUrl("/login?error")   // mismo mensaje para cualquier fallo (REQ-ZAP-002)
                .permitAll())
            .logout(l -> l
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")        // REQ-ZAP-003: vuelve al catálogo público
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID"));
        return http.build();
    }

    /** AC-001.1: cada rol aterriza en su panel. */
    private AuthenticationSuccessHandler destinoPorRol() {
        return (request, response, auth) -> {
            String destino = "/";
            for (GrantedAuthority g : auth.getAuthorities()) {
                switch (g.getAuthority()) {
                    case "ROLE_ADMINISTRADOR" -> destino = "/admin/usuarios";
                    case "ROLE_GERENTE" -> destino = "/reportes/ventas";
                    case "ROLE_VENDEDOR" -> destino = "/gestion/pedidos";
                    case "ROLE_ALMACENISTA" -> destino = "/almacen/inventario";
                    default -> { }
                }
            }
            response.sendRedirect(request.getContextPath() + destino);
        };
    }
}
