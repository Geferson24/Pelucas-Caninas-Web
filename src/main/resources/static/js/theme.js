// Se ejecuta en el head para recuperar el tema antes del primer dibujo.
(() => {
  const root = document.documentElement;
  let dark = false;
  try {
    dark = localStorage.getItem("pelucasTema") === "oscuro";
  } catch {}
  root.classList.add("theme-initializing");

  function apply(value = root.dataset.theme === "dark") {
    root.dataset.theme = value ? "dark" : "light";
    root.style.colorScheme = root.dataset.theme;
    if (document.body) {
      document.body.classList.add("page");
      document.body.classList.toggle("dark-mode", value);
      document.body.dataset.theme = root.dataset.theme;
    }
    document.querySelectorAll("#darkModeToggle").forEach((button) => {
      button.classList.toggle("is-dark", value);
      button.setAttribute("aria-checked", String(value));
      button.setAttribute(
        "aria-label",
        value ? "Cambiar a modo claro" : "Cambiar a modo oscuro",
      );
    });
  }

  window.PelucasTheme = {
    apply,
    toggle() {
      const value = root.dataset.theme !== "dark";
      try {
        localStorage.setItem("pelucasTema", value ? "oscuro" : "claro");
      } catch {}
      apply(value);
    },
    ready() {
      requestAnimationFrame(() =>
        requestAnimationFrame(() =>
          root.classList.remove("theme-initializing"),
        ),
      );
    },
  };
  apply(dark);
})();
