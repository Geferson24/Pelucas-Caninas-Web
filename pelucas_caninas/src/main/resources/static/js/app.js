// ============================================================
// FUNCIONALIDADES DOM - PELUCAS CANINAS
// Este archivo controla: carrito, modo oscuro, buscador,
// formulario, galería, animaciones, contadores, loader y botones.
// ============================================================

// Claves usadas para guardar datos en el navegador.
// localStorage mantiene la información incluso después de cerrar la página.
const STORAGE_CART = "pelucasCarrito";
const STORAGE_THEME = "pelucasTema";

// Funciones auxiliares para seleccionar elementos del HTML.
// qs devuelve el primer elemento que coincida con el selector.
const qs = (selector, parent = document) => parent.querySelector(selector);
// qsa devuelve todos los elementos encontrados en forma de arreglo.
const qsa = (selector, parent = document) => [
  ...parent.querySelectorAll(selector),
];

// Obtiene el carrito guardado en localStorage.
// Si no existe, devuelve un arreglo vacío.
function getCart() {
  return JSON.parse(localStorage.getItem(STORAGE_CART)) || [];
}

// Guarda el carrito y actualiza la interfaz.
function saveCart(cart) {
  localStorage.setItem(STORAGE_CART, JSON.stringify(cart));
  updateCartCount();
  renderCartPanel();
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
        <img src="${item.img}" alt="${item.nombre}">
        <div class="cart-item-info">
          <h4>${item.nombre}</h4>
          <p>S/ ${Number(item.precio).toFixed(2)}</p>
          <div class="cart-quantity">
            <button type="button" data-action="minus" data-name="${item.nombre}">-</button>
            <span>${item.cantidad}</span>
            <button type="button" data-action="plus" data-name="${item.nombre}">+</button>
          </div>
        </div>
        <button type="button" class="cart-remove" data-action="remove" data-name="${item.nombre}">
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
function setupDarkMode() {
  if (localStorage.getItem(STORAGE_THEME) === "oscuro") {
    document.body.classList.add("dark-mode");
  }

  qsa("#darkModeToggle").forEach((btn) => {
    btn.addEventListener("click", () => {
      document.body.classList.toggle("dark-mode");
      const oscuro = document.body.classList.contains("dark-mode");
      localStorage.setItem(STORAGE_THEME, oscuro ? "oscuro" : "claro");
      btn.innerHTML = oscuro
        ? '<i class="fa-solid fa-sun"></i>'
        : '<i class="fa-solid fa-moon"></i>';
    });
  });

  const oscuro = document.body.classList.contains("dark-mode");
  qsa("#darkModeToggle").forEach((btn) => {
    btn.innerHTML = oscuro
      ? '<i class="fa-solid fa-sun"></i>'
      : '<i class="fa-solid fa-moon"></i>';
  });
}

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

function setupMobileFiltersDrawer() {
  const panel = qs("#panelFiltros");
  const overlay = qs("#filtrosOverlay");
  const openBtn = qs("#mobileFiltersToggle");
  const closeBtn = qs("#mobileFiltersClose");
  const clearBtn = qs("#clearMobileFilters");
  const input = qs("#buscadorProductos");

  if (!panel || !overlay || !openBtn) return;

  const syncExpandedState = (abierto) => {
    openBtn.setAttribute("aria-expanded", abierto ? "true" : "false");
    panel.setAttribute("aria-hidden", abierto ? "false" : "true");
    overlay.setAttribute("aria-hidden", abierto ? "false" : "true");
  };

  const openDrawer = () => {
    if (window.innerWidth > 940) return;
    document.body.classList.add("filtros-abiertos");
    syncExpandedState(true);
  };

  const closeDrawer = () => {
    document.body.classList.remove("filtros-abiertos");
    syncExpandedState(false);
  };

  openBtn.addEventListener("click", openDrawer);
  closeBtn?.addEventListener("click", closeDrawer);
  overlay.addEventListener("click", closeDrawer);

  clearBtn?.addEventListener("click", () => {
    activeProductFilters.clear();
    if (input) input.value = "";
    syncProductFilterButtons();
    applyProductFilters();
  });

  window.addEventListener("resize", () => {
    if (window.innerWidth > 940) closeDrawer();
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") closeDrawer();
  });
}

// Obtiene los datos del producto desde una tarjeta del catálogo o una página de detalle.
function getProductDataFromButton(button) {
  const card = button.closest(".tarjeta");
  if (card) {
    return {
      nombre: card.dataset.nombre,
      precio: Number(card.dataset.precio),
      img: card.dataset.img,
    };
  }

  const detail = button.closest(".producto-grande");
  if (!detail) return null;

  const nombre =
    detail.dataset.nombre ||
    qs(".producto-info h1", detail)?.textContent.trim();
  const precioTexto =
    detail.dataset.precio ||
    qs(".producto-info h2", detail)
      ?.textContent.replace(/[^0-9.,]/g, "")
      .replace(",", ".");
  const img =
    detail.dataset.img || qs(".producto-img img", detail)?.getAttribute("src");

  const precio = Number(precioTexto);
  if (!nombre || !Number.isFinite(precio) || !img) return null;

  return { nombre, precio, img };
}

// Escucha todos los clics relacionados con el carrito.
// Usa delegación de eventos para manejar botones creados dinámicamente.
function setupCartEvents() {
  document.addEventListener("click", (e) => {
    const addBtn = e.target.closest(".btn-add-cart, .btn-add-detail");
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

// Agrega automáticamente un botón "Agregar al carrito"
// en las páginas individuales de producto.
function injectDetailAddButton() {
  const detail = qs(".producto-grande");
  if (!detail || qs(".btn-add-cart", detail) || qs(".btn-add-detail", detail))
    return;

  const title = qs(".producto-info h1")?.textContent.trim();
  const priceText = qs(".producto-info h2")
    ?.textContent.replace("S/", "")
    .trim();
  const img = qs(".producto-img img")?.getAttribute("src");
  const botones = qs(".producto-info .botones");
  if (!title || !priceText || !img || !botones) return;

  const btn = document.createElement("button");
  btn.type = "button";
  btn.className = "btn-add-detail";
  btn.innerHTML = '<i class="fa-solid fa-cart-plus"></i> Agregar al carrito';
  btn.addEventListener("click", () => {
    addToCart({ nombre: title, precio: Number(priceText), img });
  });

  botones.prepend(btn);
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

    showToast("Formulario validado correctamente");
    form.reset();
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

// Crea el botón flotante de WhatsApp si todavía no existe.
function createFloatingWhatsapp() {
  if (qs(".whatsapp-flotante")) return;

  const link = document.createElement("a");
  link.href =
    "https://wa.me/51937317249?text=Hola,%20quiero%20información%20sobre%20Pelucas%20Caninas.";
  link.target = "_blank";
  link.rel = "noopener noreferrer";
  link.className = "whatsapp-flotante";
  link.setAttribute("aria-label", "Escríbenos por WhatsApp");
  link.innerHTML = '<i class="fab fa-whatsapp"></i>';
  document.body.appendChild(link);
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

// Convierte las imágenes del carrusel en una galería ampliable.
// Permite cambiar de imagen con flechas, teclado y cerrar con Escape.
function setupGalleryModal() {
  const allImages = qsa(".galeria-track img");
  if (allImages.length === 0) return;

  // El carrusel repite imágenes para el movimiento continuo; aquí conservamos solo una vez cada foto.
  const images = allImages.filter(
    (img, index, array) =>
      array.findIndex(
        (item) => item.getAttribute("src") === img.getAttribute("src"),
      ) === index,
  );
  let currentIndex = 0;

  const modal = document.createElement("div");
  modal.className = "galeria-modal";
  modal.setAttribute("role", "dialog");
  modal.setAttribute("aria-modal", "true");
  modal.innerHTML = `
    <button type="button" class="galeria-modal-cerrar" aria-label="Cerrar imagen">
      <i class="fa-solid fa-xmark"></i>
    </button>
    <button type="button" class="galeria-modal-flecha anterior" aria-label="Foto anterior">
      <i class="fa-solid fa-chevron-left"></i>
    </button>
    <figure class="galeria-modal-contenido">
      <img src="" alt="Mascota ampliada">
      <figcaption></figcaption>
    </figure>
    <button type="button" class="galeria-modal-flecha siguiente" aria-label="Foto siguiente">
      <i class="fa-solid fa-chevron-right"></i>
    </button>`;
  document.body.appendChild(modal);

  const modalImg = qs("img", modal);
  const caption = qs("figcaption", modal);

  const showImage = (index) => {
    currentIndex = (index + images.length) % images.length;
    const selected = images[currentIndex];
    modalImg.src = selected.src;
    modalImg.alt = selected.alt || "Mascota ampliada";
    caption.textContent = selected.alt || "";
  };

  allImages.forEach((img) => {
    img.style.cursor = "zoom-in";
    img.addEventListener("click", () => {
      const uniqueIndex = images.findIndex(
        (item) => item.getAttribute("src") === img.getAttribute("src"),
      );
      showImage(uniqueIndex);
      modal.classList.add("activo");
      document.body.classList.add("modal-abierto");
    });
  });

  qs(".anterior", modal).addEventListener("click", (event) => {
    event.stopPropagation();
    showImage(currentIndex - 1);
  });

  qs(".siguiente", modal).addEventListener("click", (event) => {
    event.stopPropagation();
    showImage(currentIndex + 1);
  });

  const closeModal = () => {
    modal.classList.remove("activo");
    document.body.classList.remove("modal-abierto");
  };

  modal.addEventListener("click", (event) => {
    if (event.target === modal || event.target.closest(".galeria-modal-cerrar"))
      closeModal();
  });

  document.addEventListener("keydown", (event) => {
    if (!modal.classList.contains("activo")) return;
    if (event.key === "Escape") closeModal();
    if (event.key === "ArrowLeft") showImage(currentIndex - 1);
    if (event.key === "ArrowRight") showImage(currentIndex + 1);
  });
}

// Convierte los productos relacionados en un carrusel con flechas.
// Oculta la barra de desplazamiento y avanza una tarjeta por clic.
function setupRelatedProductsAutoScroll() {
  qsa(".contenedor-productos").forEach((contenedor) => {
    if (contenedor.closest(".productos-relacionados-carrusel")) return;

    const wrapper = document.createElement("div");
    wrapper.className = "productos-relacionados-carrusel";
    contenedor.parentNode.insertBefore(wrapper, contenedor);
    wrapper.appendChild(contenedor);

    const prev = document.createElement("button");
    prev.type = "button";
    prev.className = "relacionados-control anterior";
    prev.setAttribute("aria-label", "Productos anteriores");
    prev.innerHTML = '<i class="fa-solid fa-chevron-left"></i>';

    const next = document.createElement("button");
    next.type = "button";
    next.className = "relacionados-control siguiente";
    next.setAttribute("aria-label", "Productos siguientes");
    next.innerHTML = '<i class="fa-solid fa-chevron-right"></i>';

    wrapper.appendChild(prev);
    wrapper.appendChild(next);

    const getStep = () => {
      const card = qs(".card-producto", contenedor);
      if (!card) return Math.max(260, contenedor.clientWidth * 0.8);
      const gap = parseFloat(getComputedStyle(contenedor).gap) || 0;
      return card.getBoundingClientRect().width + gap;
    };

    const updateControls = () => {
      const hayDesplazamiento =
        contenedor.scrollWidth > contenedor.clientWidth + 2;
      prev.hidden = !hayDesplazamiento;
      next.hidden = !hayDesplazamiento;
    };

    prev.addEventListener("click", () => {
      const max = contenedor.scrollWidth - contenedor.clientWidth;
      if (contenedor.scrollLeft <= 2) {
        contenedor.scrollTo({ left: max, behavior: "smooth" });
      } else {
        contenedor.scrollBy({ left: -getStep(), behavior: "smooth" });
      }
    });

    next.addEventListener("click", () => {
      const max = contenedor.scrollWidth - contenedor.clientWidth;
      if (contenedor.scrollLeft >= max - 2) {
        contenedor.scrollTo({ left: 0, behavior: "smooth" });
      } else {
        contenedor.scrollBy({ left: getStep(), behavior: "smooth" });
      }
    });

    contenedor.addEventListener("scroll", updateControls, { passive: true });
    window.addEventListener("resize", updateControls);
    requestAnimationFrame(updateControls);
  });
}

// Aplica animaciones cuando los elementos entran o salen de la pantalla.
function setupScrollReveal() {
  const selectors = [
    ".galeria .titulo-seccion",
    ".galeria .descripcion-seccion",
    ".galeria-carrusel",
    ".recomendaciones .titulo-seccion",
    ".recomendaciones .descripcion-seccion",
    ".card-reco",
    ".contacto-info",
    ".contacto-mapa",
    ".servicio-card",
    ".otro-card",
    ".tarjeta",
    ".producto-grande",
    ".nosotros-encabezado",
    ".info-card",
    ".valores-encabezado",
    ".valor-card",
    ".nosotros-compromiso",
    ".formulario-libro",
    ".footer-contenedor",
  ];

  const elements = qsa(selectors.join(",")).filter(
    (element) => !element.classList.contains("reveal"),
  );
  elements.forEach((element, index) => {
    element.classList.add("reveal");
    element.style.setProperty(
      "--reveal-delay",
      `${Math.min(index % 4, 3) * 70}ms`,
    );
  });

  if (!("IntersectionObserver" in window)) {
    elements.forEach((element) => element.classList.add("visible"));
    return;
  }

  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        // Se añade y retira para que la animación funcione al bajar y al volver a subir.
        entry.target.classList.toggle("visible", entry.isIntersecting);
      });
    },
    { threshold: 0.12, rootMargin: "0px 0px -35px 0px" },
  );

  elements.forEach((element) => observer.observe(element));
}

// Anima los contadores numéricos del hero desde 0 hasta su objetivo.
function setupHeroCounters() {
  const counters = qsa(".contador");
  if (counters.length === 0) return;

  counters.forEach((counter) => {
    const target = Number(counter.dataset.target || 0);
    const prefix = counter.dataset.prefix || "";
    const suffix = counter.dataset.suffix || "";
    const duration = 1700;
    const startTime = performance.now();

    const update = (now) => {
      const progress = Math.min((now - startTime) / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      const value = Math.round(target * eased);
      counter.textContent = `${prefix}${value.toLocaleString("es-PE")}${suffix}`;
      if (progress < 1) requestAnimationFrame(update);
    };

    requestAnimationFrame(update);
  });
}

// Crea el botón "Volver arriba" y lo muestra después de hacer scroll.
function createBackToTopButton() {
  if (qs("#volverArriba")) return;
  const button = document.createElement("button");
  button.type = "button";
  button.id = "volverArriba";
  button.className = "volver-arriba";
  button.setAttribute("aria-label", "Volver arriba");
  button.innerHTML = '<i class="fa-solid fa-arrow-up"></i>';
  document.body.appendChild(button);

  const toggle = () => button.classList.toggle("visible", window.scrollY > 450);
  window.addEventListener("scroll", toggle, { passive: true });
  button.addEventListener("click", () =>
    window.scrollTo({ top: 0, behavior: "smooth" }),
  );
  toggle();
}

// Crea el loader de entrada. Se muestra una sola vez por sesión.
function createPageLoader() {
  if (qs("#pageLoader")) return;

  // El loader se muestra una sola vez durante la sesión de navegación.
  // Al cambiar entre las páginas del sitio, sessionStorage conserva esta marca.
  const LOADER_SESSION_KEY = "pelucasLoaderMostrado";
  if (sessionStorage.getItem(LOADER_SESSION_KEY) === "si") return;
  sessionStorage.setItem(LOADER_SESSION_KEY, "si");

  const loader = document.createElement("div");
  loader.id = "pageLoader";
  loader.className = "page-loader";
  loader.innerHTML = `
    <div class="page-loader-contenido">
      <img src="img/Imgloader.svg" alt="Cargando Pelucas Caninas">
      <p>Preparando todo para tu mascota...</p>
    </div>`;
  document.body.prepend(loader);

  const hide = () => {
    loader.classList.add("oculto");
    setTimeout(() => loader.remove(), 550);
  };

  if (document.readyState === "complete") {
    setTimeout(hide, 500);
  } else {
    window.addEventListener("load", () => setTimeout(hide, 500), {
      once: true,
    });
    setTimeout(hide, 3500);
  }
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
            <div class="mb-3"><label class="form-label" for="reservaServicio">Servicio</label><select class="form-select" id="reservaServicio" required><option disabled selected value="">Selecciona un servicio</option><option>Baño básico</option><option>Baño premium</option><option>Baño y corte</option><option>Corte de raza</option></select></div>
            <div class="row g-3"><div class="col-sm-6"><label class="form-label" for="reservaFecha">Fecha preferida</label><input class="form-control" id="reservaFecha" required type="date"></div><div class="col-sm-6"><label class="form-label" for="reservaTelefono">Teléfono</label><input class="form-control" id="reservaTelefono" placeholder="999 999 999" required type="tel"></div></div>
          </form>
        </div>
        <div class="modal-footer"><button class="btn btn-outline-secondary" data-bs-dismiss="modal" type="button">Cancelar</button><button class="btn btn-reserva-modal" form="formReserva" type="submit"><i class="fab fa-whatsapp"></i> Continuar por WhatsApp</button></div>
      </div>
    </div>`;
  document.body.append(modal);
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
    showToast("Formulario enviado correctamente");
    form.reset();
  });
}

// ============================================================
// INICIALIZACIÓN GENERAL
// DOMContentLoaded espera a que el HTML esté completamente cargado
// antes de ejecutar todas las funciones del sitio.
// ============================================================
document.addEventListener("DOMContentLoaded", () => {
  createPageLoader();
  createCartPanel();
  setupGalleryModal();
  setupRelatedProductsAutoScroll();
  setupDarkMode();
  setupProductSearch();
  setupMobileFiltersDrawer();
  setupCartEvents();
  injectDetailAddButton();
  setupFormValidation();
  setupContactFormValidation();
  createReservationModal();
  setupReservationButtons();
  setupReservationModal();
  createFloatingWhatsapp();
  createBackToTopButton();
  setupScrollReveal();
  setupHeroCounters();
  updateCartCount();
});
