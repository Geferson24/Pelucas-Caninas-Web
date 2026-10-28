// Entradas por bloques que se repiten al volver a pasar por ellos.
function setupScrollReveal() {
  const motionPreference = window.matchMedia(
    "(prefers-reduced-motion: reduce)",
  );
  if (motionPreference.matches || !("IntersectionObserver" in window)) return;

  const selectors = [
    ".hero-copy > *",
    ".hero-visual",
    ".stats",
    ".happy-pets__header",
    ".pet-carousel",
    ".pet-carousel__footer",
    ".visit__intro",
    ".visit-action",
    ".visit-details",
    ".visit-map",
    ".faq-container > .titulo-seccion",
    ".faq-container > .descripcion-seccion",
    ".accordion-item",
    ".services__header",
    ".service-plan",
    ".services-empty",
    ".extra-services__heading",
    ".extra-service",
    ".products__header",
    ".product-toolbar",
    ".products__meta",
    ".product-card",
    ".products-empty",
    ".about-duo__copy",
    ".about-duo__media",
    ".contact__intro",
    ".contact-form",
    ".claims__intro > *",
    ".claims-step",
    ".claims-submit",
    ".login__visual",
    ".login__content > *",
    ".legal-page__header",
    ".legal-section",
    ".legal-contact",
    ".error-page__visual",
    ".error-page__copy",
    ".admin-intro",
    ".admin-note",
    ".admin-nav",
    ".admin-heading",
    ".admin-card",
    ".admin-recent",
    ".admin-guide",
    ".admin-module",
    ".admin-table-wrap",
    ".admin-form > *",
    ".admin-footer",
    ".footer__brand",
    ".footer-column",
    ".footer__bottom",
  ];
  const elements = [];
  document.querySelectorAll(selectors.join(",")).forEach((element) => {
    // Un solo efecto por bloque: no animar también sus descendientes.
    if (element.parentElement.closest(".reveal")) return;
    element.classList.add("reveal");
    const siblings = element.parentElement.children;
    const order = Array.from(siblings).indexOf(element);
    element.style.setProperty("--reveal-delay", `${Math.min(order, 3) * 65}ms`);
    if (
      element.matches(
        ".hero-visual, .visit-map, .about-duo__media, .login__visual",
      )
    ) {
      element.dataset.reveal = "right";
    } else if (
      element.matches(".about-duo__copy, .contact__intro, .visit__intro")
    ) {
      element.dataset.reveal = "left";
    }
    elements.push(element);
  });

  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        const element = entry.target;
        if (entry.isIntersecting || element.contains(document.activeElement)) {
          element.classList.add("visible");
        } else {
          element.dataset.revealEdge =
            entry.boundingClientRect.bottom <= 0 ? "above" : "below";
          element.classList.remove("visible");
        }
      });
    },
    { threshold: 0, rootMargin: "0px 0px -24px 0px" },
  );
  elements.forEach((element) => observer.observe(element));

  // Mantener visibles los controles cuando se navega con el teclado.
  document.addEventListener("focusin", (event) => {
    event.target.closest(".reveal")?.classList.add("visible");
  });
  motionPreference.addEventListener("change", (event) => {
    if (!event.matches) return;
    observer.disconnect();
    elements.forEach((element) => element.classList.add("visible"));
  });
}

document.addEventListener("DOMContentLoaded", setupScrollReveal, {
  once: true,
});
