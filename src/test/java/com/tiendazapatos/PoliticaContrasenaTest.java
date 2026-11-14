package com.tiendazapatos;

import com.tiendazapatos.util.PoliticaContrasena;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** REQ-ZAP-006: mínimo 8 caracteres con al menos una letra y un número. */
class PoliticaContrasenaTest {

    @Test
    void ac006_1_seisCaracteresSeRechaza() {
        assertThat(PoliticaContrasena.cumple("abc123")).isFalse();
    }

    @Test
    void ac006_2_ochoCaracteresConLetrasYNumeroSeAcepta() {   // borde
        assertThat(PoliticaContrasena.cumple("abcdefg1")).isTrue();
    }

    @Test
    void sinNumeroOSinLetraSeRechaza() {
        assertThat(PoliticaContrasena.cumple("abcdefgh")).isFalse();
        assertThat(PoliticaContrasena.cumple("12345678")).isFalse();
    }

    @Test
    void nuloSeRechaza() {
        assertThat(PoliticaContrasena.cumple(null)).isFalse();
    }
}
