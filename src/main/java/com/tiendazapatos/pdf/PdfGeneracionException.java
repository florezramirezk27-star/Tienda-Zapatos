package com.tiendazapatos.pdf;

/** La API de PDF falló (REQ-ZAP-035). El controlador la convierte en un mensaje para el usuario. */
public class PdfGeneracionException extends RuntimeException {

    public PdfGeneracionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
