package com.demo.apptracky.services;

import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service
public class EncryptionService {
    private final SecretKey secretKey;

    private final IvParameterSpec ivParameterSpec;

    public EncryptionService() {
        try {
            secretKey = generateKey();
        } catch (final NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        ivParameterSpec = generateIv();
    }

    private static SecretKey generateKey() throws NoSuchAlgorithmException {
        // TODO swap on prod profile
        KeyGenerator keygenerator = KeyGenerator.getInstance("AES");
        keygenerator.init(128);
        return keygenerator.generateKey();
    }

    private static IvParameterSpec generateIv() {
        byte[] initializationVector = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(initializationVector);
        return new IvParameterSpec(initializationVector);
    }

    public byte[] encrypt(byte[] input) throws
            NoSuchPaddingException, NoSuchAlgorithmException,
            IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        return cipher.doFinal(input);
    }

    public byte[] decrypt(byte[] cipherText) throws
            NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
        return cipher.doFinal(cipherText);
    }

}
