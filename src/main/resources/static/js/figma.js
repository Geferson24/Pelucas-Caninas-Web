document.addEventListener("DOMContentLoaded", () => {
  const menu = document.getElementById("navbarPelucas");
  const toggle = document.getElementById("siteMenuToggle");
  const closeMenu = () => {
    menu?.classList.remove("is-open");
    toggle?.setAttribute("aria-expanded", "false");
  };
  toggle?.addEventListener("click", () => {
    const open = menu.classList.toggle("is-open");
    toggle.setAttribute("aria-expanded", String(open));
  });
  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") closeMenu();
  });
  document.addEventListener("click", (event) => {
    if (!event.target.closest(".site-header")) closeMenu();
  });
  window.addEventListener("resize", () => {
    if (window.innerWidth > 980) closeMenu();
  });

  const source = document.getElementById("pet-data");
  if (!source) return;
  const pets = Array.from(source.content.querySelectorAll("img"));
  if (!pets.length) return;
  let active = 0;
  const draw = () => {
    const slots = [
      [".pet-slide--previous", (active - 1 + pets.length) % pets.length],
      [".pet-slide--active", active],
      [".pet-slide--next", (active + 1) % pets.length],
    ];
    slots.forEach(([selector, index]) => {
      const card = document.querySelector(selector);
      const image = card.querySelector("img");
      image.src = pets[index].src;
      image.alt = pets[index].alt;
      const name =
        card.querySelector("strong") || card.querySelector(":scope > span");
      if (name) name.textContent = pets[index].alt;
    });
    document.getElementById("pet-position").textContent =
      `${String(active + 1).padStart(2, "0")} / ${String(pets.length).padStart(2, "0")}`;
    document.querySelectorAll("[data-pet-index]").forEach((dot) => {
      const selected = Number(dot.dataset.petIndex) === active;
      dot.classList.toggle("is-active", selected);
      if (selected) dot.setAttribute("aria-current", "true");
      else dot.removeAttribute("aria-current");
    });
  };
  document.querySelectorAll("[data-pet-step]").forEach((button) => {
    button.addEventListener("click", () => {
      active =
        (active + Number(button.dataset.petStep) + pets.length) % pets.length;
      draw();
    });
  });
  document.querySelectorAll("[data-pet-index]").forEach((dot) => {
    dot.addEventListener("click", () => {
      active = Number(dot.dataset.petIndex);
      draw();
    });
  });
  draw();
  const carousel = document.querySelector(".happy-pets");
  const autoplay = document.getElementById("petAutoToggle");
  const motionPreference = window.matchMedia(
    "(prefers-reduced-motion: reduce)",
  );
  let paused = motionPreference.matches;
  let timer;
  const stop = () => {
    clearInterval(timer);
    timer = undefined;
  };
  const start = () => {
    stop();
    if (
      paused ||
      document.hidden ||
      carousel.matches(":hover") ||
      carousel.contains(document.activeElement)
    )
      return;
    timer = setInterval(() => {
      active = (active + 1) % pets.length;
      draw();
    }, 5000);
  };
  const syncAutoplay = () => {
    if (autoplay) {
      autoplay.textContent = paused ? "Reanudar carrusel" : "Pausar carrusel";
      autoplay.setAttribute("aria-pressed", String(paused));
    }
    start();
  };
  autoplay?.addEventListener("click", () => {
    paused = !paused;
    syncAutoplay();
  });
  carousel.addEventListener("mouseenter", stop);
  carousel.addEventListener("mouseleave", start);
  carousel.addEventListener("focusin", stop);
  carousel.addEventListener("focusout", () => setTimeout(start, 0));
  document.addEventListener("visibilitychange", start);
  window.addEventListener("pagehide", stop);
  window.addEventListener("pageshow", start);
  motionPreference.addEventListener("change", () => {
    paused = motionPreference.matches;
    syncAutoplay();
  });
  syncAutoplay();
});
