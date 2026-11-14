package com.tiendazapatos.model;

import com.tiendazapatos.exception.ReglaNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Pedido de la tienda. Como el proyecto no tiene capa service, las reglas del pedido
 * (totales, IVA, descuento de stock, estados y cancelación) viven aquí (ADR-002).
 */
@Entity
@Table(name = "pedidos")
public class Pedido {

    /** RN-03: IVA del 19 %. */
    public static final BigDecimal TASA_IVA = new BigDecimal("0.19");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String numero = nuevoNumero();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Canal canal = Canal.WEB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(length = 250)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MetodoPago metodoPago;

    /** Nulo en una venta de mostrador (REQ-ZAP-027). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    /** Vendedor que confirmó o registró el pedido, si aplica. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendedor_id")
    private Usuario vendedor;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal iva = BigDecimal.ZERO;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    public static String nuevoNumero() {
        String dia = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "PED-" + dia + "-" + String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000));
    }

    /** REQ-ZAP-025: el IVA es el 19 % del subtotal. */
    public static BigDecimal ivaDe(BigDecimal subtotal) {
        return subtotal.multiply(TASA_IVA).setScale(0, RoundingMode.HALF_UP);
    }

    /** REQ-ZAP-023: descuenta el stock de la talla y agrega la línea al pedido. */
    public void agregarDetalle(TallaStock talla, int cantidad) {
        talla.descontar(cantidad);
        detalles.add(new DetallePedido(this, talla, cantidad));
        recalcular();
    }

    public void recalcular() {
        subtotal = detalles.stream().map(DetallePedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        iva = ivaDe(subtotal);
        total = subtotal.add(iva);
    }

    /** REQ-ZAP-028 y 029: solo se permiten las transiciones de RN-05. */
    public void cambiarEstado(EstadoPedido nuevo) {
        if (!estado.permite(nuevo)) {
            throw new ReglaNegocioException("Transición no permitida: " + estado + " → " + nuevo);
        }
        estado = nuevo;
    }

    /** REQ-ZAP-030 y RN-06: solo se cancela PENDIENTE o CONFIRMADO, y se devuelve el stock. */
    public void cancelar() {
        if (estado != EstadoPedido.PENDIENTE && estado != EstadoPedido.CONFIRMADO) {
            throw new ReglaNegocioException("Solo se pueden cancelar pedidos PENDIENTE o CONFIRMADO");
        }
        detalles.forEach(d -> d.getTallaStock().agregar(d.getCantidad()));
        estado = EstadoPedido.CANCELADO;
    }

    public int getUnidades() {
        return detalles.stream().mapToInt(DetallePedido::getCantidad).sum();
    }

    public String getNombreCliente() {
        return cliente != null ? cliente.getNombre() : "Cliente de mostrador";
    }

    public Pedido() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Canal getCanal() {
        return canal;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Usuario getCliente() {
        return cliente;
    }

    public void setCliente(Usuario cliente) {
        this.cliente = cliente;
    }

    public Usuario getVendedor() {
        return vendedor;
    }

    public void setVendedor(Usuario vendedor) {
        this.vendedor = vendedor;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }
}
