// Carrito, modo oscuro, filtros, formularios, reservas y animaciones del sitio.

// Claves usadas para guardar datos en el navegador.
// localStorage mantiene la información incluso después de cerrar la página.
const STORAGE_CART = "pelucasCarrito";

// Funciones auxiliares para seleccionar elementos del HTML.
// qs devuelve el primer elemento que coincida con el selector.
const qs = (selector, parent = document) => parent.querySelector(selector);
// qsa devuelve todos los elementos encontrados en forma de arreglo.
const qsa = (selector, parent = document) => [
  ...parent.querySelectorAll(selector),
];

function escapeHtml(value) {
  return String(value).replace(
    /[&<>"']/g,
    (char) =>
      ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;",
      })[char],
  );
}
function safeImageUrl(value) {
  try {
    const url = new URL(value, window.location.origin);
    return url.origin === window.location.origin &&
      url.pathname.startsWith("/img/")
      ? url.href
      : "";
  } catch {
    return "";
  }
}

// Obtiene el carrito guardado en localStorage.
// Si no existe, devuelve un arreglo vacío.
function getCart() {
  try {
    const saved = JSON.parse(localStorage.getItem(STORAGE_CART));
    return Array.isArray(saved)
      ? saved.filter(
          (item) =>
            typeof item?.nombre === "string" &&
            typeof item?.img === "string" &&
            Number.isFinite(Number(item.precio)) &&
            Number(item.precio) >= 0 &&
            Number.isInteger(item.cantidad) &&
            item.cantidad > 0,
        )
      : [];
  } catch {
    return [];
  }
}

// Guarda el carrito y actualiza la interfaz.
function saveCart(cart) {
  localStorage.setItem(STORAGE_CART, JSON.stringify(cart));
  updateCartCount();
  renderCartPanel();
  updateWhatsappCartLink();
}

// Suma las cantidades de todos los productos y muestra el total
// en el contador del ícono del carrito.
function updateCartCount() {
  const count = getCart().reduce((total, item) => total + item.cantidad, 0);
  qsa("#cartCount").forEach((el) => (el.textContent = count));
}

// Agrega un producto al carrito. Si ya existe, aumenta su cantidad.
function addToCart(producto) {
  if (
    !producto?.nombre ||
    !Number.isFinite(Number(producto.precio)) ||
    !producto?.img
  ) {
    console.warn("No se pudo agregar el producto: faltan datos.", producto);
    return false;
  }

  const productoNormalizado = {
    nombre: producto.nombre.trim(),
    precio: Number(producto.precio),
    img: producto.img,
  };

  const cart = getCart();
  const existente = cart.find(
    (item) => item.nombre === productoNormalizado.nombre,
  );

  if (existente) {
    existente.cantidad += 1;
  } else {
    cart.push({ ...productoNormalizado, cantidad: 1 });
  }

  saveCart(cart);
  showToast("Producto agregado al carrito");
  return true;
}

// Elimina del carrito el producto cuyo nombre coincida.
function removeFromCart(nombre) {
  const cart = getCart().filter((item) => item.nombre !== nombre);
  saveCart(cart);
}

// Cambia la cantidad de un producto.
// cantidad puede ser 1 para sumar o -1 para restar.
function changeQuantity(nombre, cantidad) {
  const cart = getCart();
  const item = cart.find((producto) => producto.nombre === nombre);
  if (!item) return;

  item.cantidad += cantidad;
  if (item.cantidad <= 0) {
    removeFromCart(nombre);
    return;
  }

  saveCart(cart);
}

// Dibuja el contenido del carrito dentro del panel lateral.
function renderCartPanel() {
  const cartBody = qs("#cartItems");
  const cartTotal = qs("#cartTotal");
  if (!cartBody || !cartTotal) return;

  const cart = getCart();

  if (cart.length === 0) {
    cartBody.innerHTML = `<p class="cart-empty">Tu carrito está vacío.</p>`;
    cartTotal.textContent = "S/ 0.00";
    return;
  }

  cartBody.innerHTML = cart
    .map(
      (item) => `
      <div class="cart-item">
        <img src="${escapeHtml(safeImageUrl(item.img))}" alt="${escapeHtml(item.nombre)}">
        <div class="cart-item-info">
          <h4>${escapeHtml(item.nombre)}</h4>
          <p>S/ ${Number(item.precio).toFixed(2)}</p>
          <div class="cart-quantity">
            <button type="button" data-action="minus" data-name="${escapeHtml(item.nombre)}">-</button>
            <span>${item.cantidad}</span>
            <button type="button" data-action="plus" data-name="${escapeHtml(item.nombre)}">+</button>
          </div>
        </div>
        <button type="button" class="cart-remove" data-action="remove" data-name="${escapeHtml(item.nombre)}">
          <i class="fa-solid fa-trash"></i>
        </button>
      </div>`,
    )
    .join("");

  const total = cart.reduce(
    (sum, item) => sum + Number(item.precio) * item.cantidad,
    0,
  );
  cartTotal.textContent = `S/ ${total.toFixed(2)}`;
}

// Crea dinámicamente el panel lateral del carrito y el fondo oscuro.
// Solo se crea una vez.
function createCartPanel() {
  if (qs("#cartPanel")) return;

  const panel = document.createElement("aside");
  panel.id = "cartPanel";
  panel.className = "cart-panel";
  panel.setAttribute("role", "dialog");
  panel.setAttribute("aria-modal", "true");
  panel.setAttribute("aria-label", "Carrito de compras");
  panel.innerHTML = `
    <div class="cart-header">
      <h3>Carrito de compras</h3>
      <button type="button" id="cartClose" aria-label="Cerrar carrito">
        <i class="fa-solid fa-xmark"></i>
      </button>
    </div>
    <div id="cartItems" class="cart-items"></div>
    <div class="cart-footer">
      <div class="cart-total-row">
        <strong>Total:</strong>
        <strong id="cartTotal">S/ 0.00</strong>
      </div>
      <a id="cartWhatsapp" class="cart-whatsapp" target="_blank" href="#">
        <i class="fab fa-whatsapp"></i> Pedir por WhatsApp
      </a>
    </div>`;

  const overlay = document.createElement("div");
  overlay.id = "cartOverlay";
  overlay.className = "cart-overlay";

  document.body.appendChild(overlay);
  document.body.appendChild(panel);
  renderCartPanel();
}

// Abre el panel del carrito y actualiza el enlace de WhatsApp.
function openCart() {
  createCartPanel();
  qs("#cartPanel").classList.add("activo");
  qs("#cartOverlay").classList.add("activo");
  updateWhatsappCartLink();
}

// Cierra el panel del carrito y su overlay.
function closeCart() {
  qs("#cartPanel")?.classList.remove("activo");
  qs("#cartOverlay")?.classList.remove("activo");
}

// Construye el mensaje de pedido y lo coloca en el enlace de WhatsApp.
function updateWhatsappCartLink() {
  const link = qs("#cartWhatsapp");
  if (!link) return;

  const cart = getCart();
  if (cart.length === 0) {
    link.href =
      "https://wa.me/51937317249?text=Hola,%20quiero%20información%20sobre%20sus%20productos.";
    return;
  }

  const detalle = cart
    .map(
      (item) =>
        `- ${item.nombre} x${item.cantidad} (S/ ${Number(item.precio).toFixed(2)})`,
    )
    .join("\n");
  const total = cart.reduce(
    (sum, item) => sum + Number(item.precio) * item.cantidad,
    0,
  );
  const mensaje = `Hola, quiero hacer un pedido:\n${detalle}\nTotal: S/ ${total.toFixed(2)}`;
  link.href = `https://wa.me/51937317249?text=${encodeURIComponent(mensaje)}`;
}

// Configura el modo oscuro y recuerda la preferencia del usuario.
// Filtra las tarjetas de productos por texto y por una o varias categorías.
// Un conjunto vacío equivale a mostrar todas las categorías.
const activeProductFilters = new Set();

function syncProductFilterButtons() {
  const buttons = qsa(".filtro-btn");
  const allButton = buttons.find((button) => button.dataset.filter === "all");
  const showAll = activeProductFilters.size === 0;

  buttons.forEach((button) => {
    const filter = button.dataset.filter || "all";
    const active =
      filter === "all" ? showAll : activeProductFilters.has(filter);
    button.classList.toggle("active", active);
    button.classList.toggle("is-active", active);
    button.setAttribute("aria-pressed", active ? "true" : "false");
  });

  if (allButton)
    allButton.setAttribute("aria-pressed", showAll ? "true" : "false");
}

function applyProductFilters() {
  const input = qs("#buscadorProductos");
  const cards = qsa(".productos-grid .tarjeta");
  const empty = qs("#sinResultados");
  const texto = (input?.value || "").toLowerCase().trim();
  let visibles = 0;

  cards.forEach((card) => {
    const categoria = (card.dataset.categoria || "all").toLowerCase();
    const nombre = (card.dataset.nombre || "").toLowerCase();
    const contenido = (card.textContent || "").toLowerCase();
    const coincideCategoria =
      activeProductFilters.size === 0 || activeProductFilters.has(categoria);
    const coincideTexto =
      !texto || contenido.includes(texto) || nombre.includes(texto);
    const visible = coincideCategoria && coincideTexto;

    card.style.display = visible ? "flex" : "none";
    if (visible) visibles++;
  });

  if (empty) empty.classList.toggle("activo", visibles === 0);
  const count = qs("#productResultCount");
  if (count)
    count.textContent = `${visibles} ${visibles === 1 ? "producto" : "productos"}`;
}

function setupProductSearch() {
  const input = qs("#buscadorProductos");
  const cards = qsa(".productos-grid .tarjeta");
  const filterButtons = qsa(".filtro-btn");
  const empty = qs("#sinResultados");

  if (!input || cards.length === 0) return;

  input.addEventListener("input", applyProductFilters);

  filterButtons.forEach((button) => {
    button.addEventListener("click", () => {
      const filter = button.dataset.filter || "all";

      if (filter === "all") {
        activeProductFilters.clear();
      } else if (activeProductFilters.has(filter)) {
        activeProductFilters.delete(filter);
      } else {
        activeProductFilters.add(filter);
      }

      syncProductFilterButtons();
      // Se aplica en el instante tanto en escritorio como en móvil.
      applyProductFilters();
    });
  });

  if (empty) empty.classList.toggle("activo", false);
  syncProductFilterButtons();
  applyProductFilters();
}

// Obtiene los datos de la tarjeta del catálogo para el carrito.
function getProductDataFromButton(button) {
  const card = button.closest(".tarjeta");
  if (!card) return null;
  return {
    nombre: card.dataset.nombre,
    precio: Number(card.dataset.precio),
    img: card.dataset.img,
  };
}

// Escucha todos los clics relacionados con el carrito.
// Usa delegación de eventos para manejar botones creados dinámicamente.
function setupCartEvents() {
  document.addEventListener("click", (e) => {
    const addBtn = e.target.closest(".btn-add-cart");
    if (addBtn) {
      e.preventDefault();
      e.stopPropagation();

      const producto = getProductDataFromButton(addBtn);
      if (producto) {
        addToCart(producto);
      } else {
        console.warn("No se encontraron los datos del producto.");
      }
    }

    const serviceButton = e.target.closest(".btn-servicio");
    if (serviceButton) {
      const service = serviceButton
        .closest(".servicio-card")
        ?.querySelector("h3")
        ?.textContent.trim();
      showToast(`${service || "Servicio"} seleccionado. Abriendo WhatsApp...`);
    }

    const cartBtn = e.target.closest("#cartToggle");
    if (cartBtn) openCart();

    if (e.target.closest("#cartClose") || e.target.closest("#cartOverlay"))
      closeCart();

    const cartAction = e.target.closest("[data-action]");
    if (cartAction && cartAction.closest("#cartPanel")) {
      const name = cartAction.dataset.name;
      const action = cartAction.dataset.action;
      if (action === "plus") changeQuantity(name, 1);
      if (action === "minus") changeQuantity(name, -1);
      if (action === "remove") removeFromCart(name);
      updateWhatsappCartLink();
    }
  });
}


// Valida el formulario del libro de reclamaciones antes de enviarlo.
function setupFormValidation() {
  const form = qs(".formulario-libro");
  if (!form) return;

  form.addEventListener("submit", (e) => {
    e.preventDefault();
    clearErrors(form);

    let valid = true;
    const requiredFields = qsa("input[required], textarea[required]", form);

    requiredFields.forEach((field) => {
      if (
        (field.type === "checkbox" || field.type === "radio") &&
        !field.checked
      )
        return;
      if (!field.value.trim()) {
        showFieldError(field, "Este campo es obligatorio.");
        valid = false;
      }
    });

    const email = qs('input[type="email"]', form);
    if (
      email &&
      email.value.trim() &&
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim())
    ) {
      showFieldError(email, "Ingresa un correo válido.");
      valid = false;
    }

    const phone = qs('input[type="tel"]', form);
    if (
      phone &&
      phone.value.trim() &&
      !/^[0-9+\s]{7,15}$/.test(phone.value.trim())
    ) {
      showFieldError(phone, "Ingresa un teléfono válido.");
      valid = false;
    }

    ["bien", "tipo"].forEach((name) => {
      if (!qs(`input[name="${name}"]:checked`, form)) {
        const group = qs(`input[name="${name}"]`, form);
        showFieldError(group, "Selecciona una opción.");
        valid = false;
      }
    });

    qsa('input[type="checkbox"][required]', form).forEach((check) => {
      if (!check.checked) {
        showFieldError(check, "Debes marcar esta casilla.");
        valid = false;
      }
    });

    if (!valid) return;

    showToast("Campos válidos. Este ejemplo no registra ni envía un reclamo.");
  });
}

// Muestra un mensaje de error junto a un campo inválido.
function showFieldError(field, message) {
  const target = field.closest("label") || field;
  target.classList.add("campo-error");
  const error = document.createElement("small");
  error.className = "mensaje-error";
  error.textContent = message;
  target.insertAdjacentElement("afterend", error);
}

// Elimina los mensajes y estilos de error anteriores.
function clearErrors(form) {
  qsa(".campo-error", form).forEach((el) => el.classList.remove("campo-error"));
  qsa(".mensaje-error", form).forEach((el) => el.remove());
}

// Muestra una notificación temporal en pantalla.
function showToast(message) {
  let toast = qs("#toast");
  if (!toast) {
    toast = document.createElement("div");
    toast.id = "toast";
    toast.className = "pelucas-toast";
    document.body.appendChild(toast);
  }

  // Usamos una clase propia para evitar conflictos con el componente .toast de Bootstrap.
  toast.className = "pelucas-toast";
  toast.textContent = message;

  // Reinicia la animación si el usuario agrega varios productos seguidos.
  toast.classList.remove("activo");
  void toast.offsetWidth;
  toast.classList.add("activo");

  clearTimeout(showToast.timer);
  showToast.timer = setTimeout(() => toast.classList.remove("activo"), 2600);
}



function createReservationModal() {
  if (qs("#formReserva")) return;

  const modal = document.createElement("div");
  modal.className = "modal fade";
  modal.id = "modalReserva";
  modal.tabIndex = -1;
  modal.setAttribute("aria-labelledby", "modalReservaLabel");
  modal.setAttribute("aria-hidden", "true");
  modal.innerHTML = `
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content modal-reserva-content">
        <div class="modal-header">
          <div>
            <span class="modal-kicker">Pelucas Caninas</span>
            <h2 class="modal-title fs-4" id="modalReservaLabel">Reserva una cita</h2>
          </div>
          <button aria-label="Cerrar" class="btn-close" data-bs-dismiss="modal" type="button"></button>
        </div>
        <div class="modal-body">
          <form id="formReserva">
            <div class="mb-3"><label class="form-label" for="reservaNombre">Tu nombre</label><input class="form-control" id="reservaNombre" required type="text"></div>
            <div class="mb-3"><label class="form-label" for="reservaMascota">Nombre de tu mascota</label><input class="form-control" id="reservaMascota" required type="text"></div>
            <div class="mb-3"><label class="form-label" for="reservaServicio">Servicio</label><select class="form-select" id="reservaServicio" required><option disabled selected value="">Selecciona un servicio</option></select></div>
            <div class="row g-3"><div class="col-sm-6"><label class="form-label" for="reservaFecha">Fecha preferida</label><input class="form-control" id="reservaFecha" required type="date"></div><div class="col-sm-6"><label class="form-label" for="reservaTelefono">Teléfono</label><input class="form-control" id="reservaTelefono" placeholder="999 999 999" required type="tel"></div></div>
          </form>
        </div>
        <div class="modal-footer"><button class="btn btn-outline-secondary" data-bs-dismiss="modal" type="button">Cancelar</button><button class="btn btn-reserva-modal" form="formReserva" type="submit"><i class="fab fa-whatsapp"></i> Continuar por WhatsApp</button></div>
      </div>
    </div>`;
  document.body.append(modal);
  const opciones = qs("#serviciosReservaOpciones");
  const selector = qs("#reservaServicio", modal);
  if (opciones) selector.append(opciones.content.cloneNode(true));
  if (selector.options.length === 1) {
    selector.options[0].textContent = "No hay servicios disponibles";
    selector.disabled = true;
    qs('button[form="formReserva"]', modal).disabled = true;
  }
}

function setupReservationButtons() {
  qsa(".btn-reserva[href]").forEach((button) => {
    if (button.dataset.reservationReady === "true") return;
    button.dataset.reservationReady = "true";
    button.addEventListener("click", (event) => {
      if (!qs("#formReserva")) createReservationModal();
      event.preventDefault();
      bootstrap.Modal.getOrCreateInstance(qs("#modalReserva")).show();
    });
  });
}

function setupReservationModal() {
  const form = qs("#formReserva");
  if (!form) return;

  form.addEventListener("submit", (event) => {
    event.preventDefault();
    if (!form.checkValidity()) {
      form.reportValidity();
      return;
    }

    const nombre = qs("#reservaNombre")?.value.trim();
    const mascota = qs("#reservaMascota")?.value.trim();
    const servicio = qs("#reservaServicio")?.value;
    const fecha = qs("#reservaFecha")?.value;
    const telefono = qs("#reservaTelefono")?.value.trim();
    const mensaje = `Hola, soy ${nombre}. Quiero reservar una cita para ${mascota}. Servicio: ${servicio}. Fecha preferida: ${fecha}. Teléfono: ${telefono}.`;
    window.open(
      `https://wa.me/51937317249?text=${encodeURIComponent(mensaje)}`,
      "_blank",
      "noopener,noreferrer",
    );
  });
}

function setupContactFormValidation() {
  const form = qs(".contact-form");
  if (!form) return;

  form.addEventListener("submit", (event) => {
    event.preventDefault();
    if (!form.checkValidity()) {
      form.reportValidity();
      return;
    }
    const data = new FormData(form);
    const message = `Hola, soy ${data.get("nombre")} ${data.get("apellido") || ""}.\nTeléfono: ${data.get("telefono")}\nCorreo: ${data.get("correo")}\nConsulta: ${data.get("mensaje")}`;
    window.open(
      `https://wa.me/51937317249?text=${encodeURIComponent(message)}`,
      "_blank",
      "noopener,noreferrer",
    );
    showToast("Continúa y envía tu consulta desde WhatsApp");
  });
}

// ============================================================
// INICIALIZACIÓN GENERAL
// DOMContentLoaded espera a que el HTML esté completamente cargado
// antes de ejecutar todas las funciones del sitio.
// ============================================================
document.addEventListener("DOMContentLoaded", () => {
  createCartPanel();
  setupProductSearch();
  setupCartEvents();
  setupFormValidation();
  setupContactFormValidation();
  createReservationModal();
  setupReservationButtons();
  setupReservationModal();
  updateCartCount();
});
