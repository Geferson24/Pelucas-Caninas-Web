package com.veterinaria.pelucascaninas.service;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Service;

/** Almacenamiento de contraseñas; no autentica ni protege rutas. */
@Service
public class ContrasenaService {
    private static final int ITERACIONES = 600_000;
    private final SecureRandom random = new SecureRandom();

    public String generarHash(String contrasena) {
        byte[] sal = new byte[16];
        random.nextBytes(sal);
        char[] caracteres = contrasena.toCharArray();
        var especificacion = new PBEKeySpec(caracteres, sal, ITERACIONES, 256);
        try {
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(especificacion).getEncoded();
            var base64 = Base64.getEncoder();
            return "pbkdf2_sha256$" + ITERACIONES + "$" + base64.encodeToString(sal)
                    + "$" + base64.encodeToString(hash);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo preparar la contraseña.", ex);
        } finally {
            especificacion.clearPassword();
            Arrays.fill(caracteres, '\0');
        }
    }
}
