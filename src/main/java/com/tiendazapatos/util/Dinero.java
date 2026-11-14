package com.tiendazapatos.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Formatea pesos colombianos (RN-03). En las vistas se usa como ${@dinero.formato(valor)}. */
@Component("dinero")
public class Dinero {

    public static String cop(BigDecimal valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        DecimalFormat formato = new DecimalFormat("'$'#,##0", simbolos);
        return formato.format(valor == null ? BigDecimal.ZERO : valor);
    }

    public String formato(BigDecimal valor) {
        return cop(valor);
    }
}
