package com.tiendazapatos.config;

import com.tiendazapatos.model.Rol;
import com.tiendazapatos.model.TallaStock;
import com.tiendazapatos.model.Usuario;
import com.tiendazapatos.model.Zapato;
import com.tiendazapatos.repository.UsuarioRepository;
import com.tiendazapatos.repository.ZapatoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Crea un usuario por cada uno de los 5 roles y 14 zapatos de ejemplo (más de 12 para ver
 * la paginación). Solo actúa si las tablas están vacías. Se desactiva con tienda.datos-demo=false.
 */
@Component
@ConditionalOnProperty(name = "tienda.datos-demo", havingValue = "true", matchIfMissing = true)
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);
    static final String CLAVE_DEMO = "Zapatos2026";

    private final UsuarioRepository usuarios;
    private final ZapatoRepository zapatos;
    private final PasswordEncoder encoder;

    public DatosIniciales(UsuarioRepository usuarios, ZapatoRepository zapatos, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.zapatos = zapatos;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarios.count() == 0) {
            String clave = encoder.encode(CLAVE_DEMO);
            usuarios.save(new Usuario("Administrador", "admin@tienda.test", clave, "3000000001", Rol.ADMINISTRADOR));
            usuarios.save(new Usuario("Gerente", "gerente@tienda.test", clave, "3000000002", Rol.GERENTE));
            usuarios.save(new Usuario("Vendedor", "vendedor@tienda.test", clave, "3000000003", Rol.VENDEDOR));
            usuarios.save(new Usuario("Almacenista", "almacenista@tienda.test", clave, "3000000004", Rol.ALMACENISTA));
            usuarios.save(new Usuario("Cliente de prueba", "cliente@tienda.test", clave, "3000000005", Rol.CLIENTE));
            // NFR-005: la clave demo nunca se escribe en los logs.
            log.info("Usuarios de demostración creados: admin@, gerente@, vendedor@, almacenista@, cliente@tienda.test");
        }
        if (zapatos.count() == 0) {
            Object[][] datos = {
                {"Air Runner", "Nike", "Deportivo", "Negro", "329000"},
                {"Court Classic", "Nike", "Casual", "Blanco", "289000"},
                {"Ultra Boost Lite", "Adidas", "Deportivo", "Gris", "399000"},
                {"Superstar Clásico", "Adidas", "Casual", "Blanco", "319000"},
                {"Suede Urbano", "Puma", "Casual", "Azul", "259000"},
                {"RS Trail", "Puma", "Deportivo", "Rojo", "279000"},
                {"Chuck Alto", "Converse", "Casual", "Negro", "249000"},
                {"Chuck Bajo", "Converse", "Casual", "Rojo", "229000"},
                {"Oxford Ejecutivo", "Hush", "Formal", "Café", "359000"},
                {"Mocasín Cuero", "Hush", "Formal", "Negro", "339000"},
                {"Sandalia Verano", "Crocs", "Sandalia", "Beige", "139000"},
                {"Clog Comfort", "Crocs", "Sandalia", "Azul", "159000"},
                {"Bota Montaña", "Timberland", "Bota", "Miel", "469000"},
                {"Bota Urbana", "Timberland", "Bota", "Negro", "439000"}
            };
            int i = 0;
            for (Object[] d : datos) {
                Zapato z = new Zapato();
                z.setReferencia(Zapato.nuevaReferencia());
                z.setNombre((String) d[0]);
                z.setMarca((String) d[1]);
                z.setCategoria((String) d[2]);
                z.setColor((String) d[3]);
                z.setPrecio(new BigDecimal((String) d[4]));
                z.setDescripcion("Zapato de demostración " + d[0] + " de " + d[1] + ".");
                z.crearTallasVacias();
                for (TallaStock t : z.getTallas()) {
                    if (t.getTalla() >= 38 && t.getTalla() <= 43) {
                        t.setStock(3 + (i + t.getTalla()) % 6);
                    }
                }
                zapatos.save(z);
                i++;
            }
            log.info("{} zapatos de demostración creados", datos.length);
        }
    }
}
