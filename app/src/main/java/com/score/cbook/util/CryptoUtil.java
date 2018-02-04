package com.score.cbook.util;

import android.content.Context;
import android.util.Base64;

import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * RSR encryption
 */
public class CryptoUtil {

    public static final String PUBLIC_KEY = "PUBLIC_KEY";
    public static final String PRIVATE_KEY = "PRIVATE_KEY";

    // size of RSA keys
    private static final int RSA_KEY_SIZE = 1024;
    private static final int SESSION_KEY_SIZE = 128;

    public static KeyPair initKeys(Context context) throws NoSuchProviderException, NoSuchAlgorithmException {
        // generate keypair
        KeyPairGenerator keyPairGenerator;
        keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(RSA_KEY_SIZE, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // save keys in shared preferences
        savePublicKey(context, keyPair);
        savePrivateKey(context, keyPair);

        return keyPair;
    }

    private static void savePublicKey(Context context, KeyPair keyPair) {
        // get public key from keypair
        byte[] keyContent = keyPair.getPublic().getEncoded();
        String publicKey = Base64.encodeToString(keyContent, Base64.DEFAULT).replaceAll("\n", "").replaceAll("\r", "");

        // save public key in shared preference
        PreferenceUtil.saveRsaKey(context, publicKey, CryptoUtil.PUBLIC_KEY);
    }

    private static void savePrivateKey(Context context, KeyPair keyPair) {
        // get public key from keypair
        byte[] keyContent = keyPair.getPrivate().getEncoded();
        String privateKey = Base64.encodeToString(keyContent, Base64.DEFAULT).replaceAll("\n", "").replaceAll("\r", "");

        // save private key in shared preference
        PreferenceUtil.saveRsaKey(context, privateKey, CryptoUtil.PRIVATE_KEY);
    }

    public static PublicKey getPublicKey(Context context) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        // get key string from shared preference
        String keyString = PreferenceUtil.getRsaKey(context, CryptoUtil.PUBLIC_KEY);

        // convert to string key public key
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decode(keyString, Base64.DEFAULT));
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return kf.generatePublic(spec);
    }

    public static PublicKey getPublicKey(String keyString) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException {
        // convert to string key public key
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decode(keyString, Base64.DEFAULT));
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return kf.generatePublic(spec);
    }

    public static PrivateKey getPrivateKey(Context context) throws InvalidKeySpecException, NoSuchAlgorithmException {
        // get key string from shared preference
        String keyString = PreferenceUtil.getRsaKey(context, CryptoUtil.PRIVATE_KEY);

        // convert to string key public key
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.decode(keyString, Base64.DEFAULT));
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return kf.generatePrivate(spec);
    }

    public static String getSenzieAddress(Context context) throws NoSuchAlgorithmException {
        // get public key
        byte[] key = Base64.decode(PreferenceUtil.getRsaKey(context, CryptoUtil.PUBLIC_KEY), Base64.DEFAULT);

        // generate digest
        byte[] ph = new byte[20];
        byte[] sha256 = MessageDigest.getInstance("SHA-256").digest(key);
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(sha256, 0, sha256.length);
        digest.doFinal(ph, 0);

        // encode base58
        return Base58.encode(ph);
    }

    public static String getDigitalSignature(String payload, PrivateKey privateKey) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        // reformat payload
        String fPayload = payload.replaceAll(" ", "")
                .replaceAll("\n", "")
                .replaceAll("\r", "")
                .trim();
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(fPayload.getBytes());


        // Base64 encoded string
        return Base64.encodeToString(signature.sign(), Base64.DEFAULT).replaceAll("\n", "").replaceAll("\r", "");
    }

    public static boolean verifyDigitalSignature(String payload, String signedPayload, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        String fPayload = payload.replaceAll(" ", "")
                .replaceAll("\n", "")
                .replaceAll("\r", "")
                .trim();
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(fPayload.getBytes());

        byte[] signedPayloadContent = Base64.decode(signedPayload, Base64.DEFAULT);

        return signature.verify(signedPayloadContent);
    }

    public static String getSessionKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(SESSION_KEY_SIZE);
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
    }

    public static SecretKey getSecretKey(String encodedKey) {
        byte[] key = Base64.decode(encodedKey, Base64.DEFAULT);
        return new SecretKeySpec(key, 0, key.length, "AES");
    }

    public static byte[] getSalt(String key) {
        return new StringBuilder(key.substring(0, 12)).reverse().toString().getBytes();
    }

    public static String encryptRSA(PublicKey publicKey, String payload) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] data = cipher.doFinal(payload.getBytes());
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public static String decryptRSA(PrivateKey privateKey, String payload) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] data = Base64.decode(payload, Base64.DEFAULT);
        return new String(cipher.doFinal(data));
    }

    public static String encryptECB(SecretKey secretKey, String payload) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] data = cipher.doFinal(payload.getBytes());
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public static String decryptECB(SecretKey secretKey, String payload) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        byte[] data = Base64.decode(payload, Base64.DEFAULT);
        return new String(cipher.doFinal(data));
    }

    public static byte[] encryptECB(SecretKey secretKey, byte[] payload, int offset, int lenght) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        return cipher.doFinal(payload, offset, lenght);
    }

    public static byte[] decryptECB(SecretKey secretKey, byte[] payload) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        return cipher.doFinal(payload);
    }

    public static String encryptCCM(SecretKey secretKey, String salt, String payload) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("AES/CCM/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(salt.getBytes("UTF-8")));

        byte[] data = cipher.doFinal(payload.getBytes());
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public static String decryptCCM(SecretKey secretKey, String salt, String payload) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("AES/CCM/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(salt.getBytes("UTF-8")));

        byte[] data = Base64.decode(payload, Base64.DEFAULT);
        return new String(cipher.doFinal(data));
    }

    public static byte[] encryptCCM(SecretKey secretKey, byte[] salt, byte[] payload, int offset, int lenght) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CCM/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(salt));

        return cipher.doFinal(payload, offset, lenght);
    }

    public static byte[] decryptCCM(SecretKey secretKey, byte[] salt, byte[] payload) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CCM/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(salt));

        return cipher.doFinal(payload);
    }

    public static String encryptGCM(SecretKey secretKey, byte[] salt, String payload) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(salt));

        byte[] data = cipher.doFinal(payload.getBytes());
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public static String decryptGCM(SecretKey secretKey, byte[] salt, String payload) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, InvalidAlgorithmParameterException, UnsupportedEncodingException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(salt));

        byte[] data = Base64.decode(payload, Base64.DEFAULT);
        return new String(cipher.doFinal(data));
    }

    public static byte[] encryptGCM(SecretKey secretKey, byte[] salt, byte[] payload, int offset, int lenght) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(salt));

        return cipher.doFinal(payload, offset, lenght);
    }

    public static byte[] decryptGCM(SecretKey secretKey, byte[] salt, byte[] payload) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchProviderException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(salt));

        return cipher.doFinal(payload);
    }

}
