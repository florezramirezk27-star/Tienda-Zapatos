/* ============================================================
   STEP · Interacciones de la tienda
   ============================================================ */
(function () {
  'use strict';

  const $ = (selector, context) => (context || document).querySelector(selector);
  const $$ = (selector, context) => Array.from((context || document).querySelectorAll(selector));
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  /* ---------- Menú móvil ---------- */
  const botonMenu = $('#botonMenu');
  const menuMovil = $('#menuMovil');

  function cerrarMenu() {
    if (!menuMovil || menuMovil.hidden) return;
    menuMovil.hidden = true;
    document.body.classList.remove('menu-abierto');
    if (botonMenu) {
      botonMenu.setAttribute('aria-expanded', 'false');
      botonMenu.setAttribute('aria-label', 'Abrir menú');
    }
  }

  if (botonMenu && menuMovil) {
    botonMenu.addEventListener('click', function () {
      const abrir = menuMovil.hidden;
      menuMovil.hidden = !abrir;
      document.body.classList.toggle('menu-abierto', abrir);
      botonMenu.setAttribute('aria-expanded', String(abrir));
      botonMenu.setAttribute('aria-label', abrir ? 'Cerrar menú' : 'Abrir menú');
    });

    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape') cerrarMenu();
    });

    window.addEventListener('resize', function () {
      if (window.innerWidth >= 992) cerrarMenu();
    });
  }

  /* ---------- Alertas con autocierre ---------- */
  if (window.bootstrap) {
    $$('.alert-dismissible').forEach(function (alert) {
      window.setTimeout(function () {
        try {
          window.bootstrap.Alert.getOrCreateInstance(alert).close();
        } catch (e) {
          alert.remove();
        }
      }, 6500);
    });
  }

  /* ---------- Enlaces internos suaves ---------- */
  if (!reduceMotion) {
    $$('a[href^="#"]').forEach(function (anchor) {
      anchor.addEventListener('click', function (event) {
        const destino = document.querySelector(anchor.getAttribute('href'));
        if (!destino) return;
        event.preventDefault();
        destino.scrollIntoView({ behavior: 'smooth', block: 'start' });
      });
    });
  }

  /* ---------- Filtros del catálogo: panel compacto en móvil ---------- */
  const filtrosPanel = $('.filtros-panel');
  if (filtrosPanel && window.matchMedia('(max-width: 991.98px)').matches) {
    filtrosPanel.open = false;
  }

  /* ---------- Detalle de producto: tallas, stock y cantidad ---------- */
  const formCompra = $('#form-compra');
  if (formCompra) {
    const botonesTalla = $$('.talla-chip', formCompra);
    const tallaId = $('#tallaId', formCompra);
    const cantidad = $('#cantidad', formCompra);
    const stockInfo = $('#stockInfo');
    const botonesRestar = $$('[data-qty="-1"]', formCompra);
    const botonesSumar = $$('[data-qty="1"]', formCompra);
    const btnAgregar = $('#btnAgregar');
    const btnAgregarMovil = $('#btnAgregarMovil');
    const precioMovil = $('#precioMovil');
    let stockSeleccionado = 0;

    function mensajeStock(icono, texto, clase) {
      if (!stockInfo) return;
      stockInfo.className = 'stock-info' + (clase ? ' ' + clase : '');
      stockInfo.innerHTML = '';
      const punto = document.createElement('span');
      punto.className = 'stock-dot';
      stockInfo.appendChild(punto);
      const textoNodo = document.createTextNode(texto);
      stockInfo.appendChild(textoNodo);
    }

    function limpiarSeleccion() {
      botonesTalla.forEach(function (b) {
        b.classList.remove('seleccionada');
        b.setAttribute('aria-pressed', 'false');
      });
      if (tallaId) tallaId.value = '';
      stockSeleccionado = 0;
      if (cantidad) {
        cantidad.max = '1';
        cantidad.value = '1';
      }
      if (btnAgregar) btnAgregar.disabled = true;
      if (btnAgregarMovil) btnAgregarMovil.disabled = true;
    }

    function seleccionarTalla(boton) {
      if (boton.disabled) return;
      botonesTalla.forEach(function (b) {
        const activa = b === boton;
        b.classList.toggle('seleccionada', activa);
        b.setAttribute('aria-pressed', String(activa));
      });

      stockSeleccionado = Number.parseInt(boton.dataset.stock || '0', 10);
      if (tallaId) tallaId.value = boton.dataset.tallaId || '';
      if (cantidad) {
        cantidad.max = String(Math.max(1, stockSeleccionado));
        const actual = Number.parseInt(cantidad.value || '1', 10);
        cantidad.value = String(Math.min(Math.max(actual, 1), Math.max(1, stockSeleccionado)));
      }

      const talla = boton.dataset.talla;
      if (stockSeleccionado <= 0) {
        mensajeStock('x-circle', 'Esta talla está agotada.', 'agotado');
      } else if (stockSeleccionado <= 3) {
        mensajeStock('alert-circle', 'Talla ' + talla + ': quedan ' + stockSeleccionado + ' unidades.', 'pocas');
      } else {
        mensajeStock('check-circle', 'Talla ' + talla + ': ' + stockSeleccionado + ' unidades disponibles.', 'disponible');
      }

      if (btnAgregar) btnAgregar.disabled = false;
      if (btnAgregarMovil) {
        btnAgregarMovil.disabled = false;
        const texto = $('#textoBotonMovil');
        if (texto) texto.textContent = 'Talla ' + talla;
      }
    }

    botonesTalla.forEach(function (boton) {
      boton.addEventListener('click', function () {
        if (boton.classList.contains('seleccionada')) {
          limpiarSeleccion();
          if (stockInfo) {
            stockInfo.className = 'stock-info';
            stockInfo.textContent = 'Selecciona una talla para ver la disponibilidad.';
          }
          const texto = $('#textoBotonMovil');
          if (texto) texto.textContent = 'Elegir talla';
          return;
        }
        seleccionarTalla(boton);
      });
    });

    function cambiarCantidad(delta) {
      if (!cantidad || !stockSeleccionado) return;
      const minimo = 1;
      const maximo = Math.max(minimo, stockSeleccionado);
      let valor = Number.parseInt(cantidad.value || '1', 10) + delta;
      valor = Math.min(Math.max(valor, minimo), maximo);
      cantidad.value = String(valor);
    }

    botonesRestar.forEach(function (b) { b.addEventListener('click', function () { cambiarCantidad(-1); }); });
    botonesSumar.forEach(function (b) { b.addEventListener('click', function () { cambiarCantidad(1); }); });

    if (cantidad) {
      cantidad.addEventListener('change', function () {
        const maximo = Math.max(1, stockSeleccionado || 1);
        const valor = Math.min(Math.max(Number.parseInt(cantidad.value || '1', 10), 1), maximo);
        cantidad.value = String(valor);
      });
    }

    formCompra.addEventListener('submit', function (event) {
      if (!tallaId || !tallaId.value) {
        event.preventDefault();
        if (stockInfo) {
          stockInfo.className = 'stock-info agotado';
          stockInfo.textContent = 'Elige una talla antes de agregar el producto.';
          stockInfo.scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth', block: 'center' });
        }
      }
    });

    if (precioMovil) {
      const precio = formCompra.dataset.precio;
      if (precio) precioMovil.textContent = precio;
    }
  }

  /* ---------- Carrito: pasos de cantidad ---------- */
  $$('[data-cart-step]').forEach(function (boton) {
    boton.addEventListener('click', function () {
      const form = boton.closest('form');
      const input = form ? $('input[name="cantidad"]', form) : null;
      if (!input) return;
      const delta = Number.parseInt(boton.dataset.cartStep, 10);
      const actual = Number.parseInt(input.value || '1', 10);
      input.value = String(Math.max(1, actual + delta));
      if (form.requestSubmit) form.requestSubmit();
    });
  });
})();
