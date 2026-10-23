const passwordInput = document.getElementById("password");
const passwordToggle = document.getElementById("togglePassword");

if (passwordInput && passwordToggle) {
  passwordToggle.hidden = false;
  passwordToggle.addEventListener("click", () => {
    const mostrar = passwordInput.type === "password";
    passwordInput.type = mostrar ? "text" : "password";
    passwordToggle.setAttribute("aria-pressed", String(mostrar));
    passwordToggle.setAttribute(
      "aria-label",
      mostrar ? "Ocultar contraseña" : "Mostrar contraseña",
    );
    passwordToggle.querySelector("i").className = mostrar
      ? "fa-regular fa-eye-slash"
      : "fa-regular fa-eye";
  });
}
