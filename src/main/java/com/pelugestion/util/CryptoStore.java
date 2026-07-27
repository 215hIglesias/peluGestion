package com.pelugestion.util;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Cifrado del fichero de base de datos con AES-256-GCM.
 * La clave se deriva de la contrasena con PBKDF2 (todo con el JDK estandar,
 * sin librerias nativas, por lo que funciona igual en Windows de 32 y 64 bits).
 *
 * Formato del fichero cifrado:
 *   MAGIC(4) | VERSION(1) | SALT(16) | IV(12) | CIPHERTEXT(+tag GCM)
 */
public final class CryptoStore {

    private CryptoStore() {}

    private static final byte[] MAGIC = {'P', 'G', 'D', 'B'};
    private static final byte VERSION = 1;
    private static final int ITERATIONS = 200_000;
    private static final int SALT_LEN = 16;
    private static final int IV_LEN = 12;
    private static final int KEY_BITS = 256;
    private static final int TAG_BITS = 128;
    private static final int HEADER = MAGIC.length + 1 + SALT_LEN; // hasta el final del salt

    public static byte[] randomSalt() {
        byte[] salt = new byte[SALT_LEN];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /** Deriva la clave AES a partir de la contrasena y el salt. */
    public static SecretKey deriveKey(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error al derivar la clave", e);
        }
    }

    /** Lee el salt de la cabecera de un fichero cifrado. */
    public static byte[] extractSalt(byte[] file) throws IOException {
        validateHeader(file);
        return Arrays.copyOfRange(file, MAGIC.length + 1, MAGIC.length + 1 + SALT_LEN);
    }

    /** Cifra los datos y devuelve el contenido completo del fichero (con cabecera). */
    public static byte[] encrypt(byte[] plain, SecretKey key, byte[] salt) {
        try {
            byte[] iv = new byte[IV_LEN];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plain);

            ByteBuffer buf = ByteBuffer.allocate(HEADER + IV_LEN + ct.length);
            buf.put(MAGIC).put(VERSION).put(salt).put(iv).put(ct);
            return buf.array();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error al cifrar", e);
        }
    }

    /**
     * Descifra el fichero. Lanza AEADBadTagException si la contrasena es
     * incorrecta o el fichero esta corrupto/manipulado.
     */
    public static byte[] decrypt(byte[] file, SecretKey key)
            throws AEADBadTagException, IOException {
        validateHeader(file);
        int ivPos = MAGIC.length + 1 + SALT_LEN;
        byte[] iv = Arrays.copyOfRange(file, ivPos, ivPos + IV_LEN);
        byte[] ct = Arrays.copyOfRange(file, ivPos + IV_LEN, file.length);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return cipher.doFinal(ct);
        } catch (AEADBadTagException e) {
            throw e; // contrasena incorrecta o datos manipulados
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error al descifrar", e);
        }
    }

    private static void validateHeader(byte[] file) throws IOException {
        if (file.length < HEADER + IV_LEN) {
            throw new IOException("Fichero de base de datos demasiado corto o corrupto");
        }
        for (int i = 0; i < MAGIC.length; i++) {
            if (file[i] != MAGIC[i]) {
                throw new IOException("El fichero no es una base de datos de PeluGestion");
            }
        }
    }
}
