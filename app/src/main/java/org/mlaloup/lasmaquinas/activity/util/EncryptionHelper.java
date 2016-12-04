package org.mlaloup.lasmaquinas.activity.util;


import android.util.Base64;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class EncryptionHelper {

    public static String convert(int n) {
        return Integer.toHexString(n).toUpperCase();
//        return Integer.valueOf(String.valueOf(n), 16);
    }

//    public static void main(String[] args ){
//        System.out.println(doSomething("matlaloup"));
//        System.out.println(doSomething("matlaloup").length());
//        System.out.println(doSomething("matlaloup"));
//        System.out.println(doSomething("dsdjsdjksjdksjdksjdksjdksjdsdsdsdsddksjdksdjksjdksjdsdjksc,nkscldklskdlskdlsdklskdlskdlsdkls"));
//        System.out.println(doSomething("dsdjsdjksjdksjdksjdksjdksjdsdsdsdsddksjdksdjksjdksjdsdjksc,nkscldklskdlskdlsdklskdlskdlsdkls").length());
//        System.out.println();
//        String encrypted = encrypt("bleaunsql",doSomething("matlaloup"));
//        System.out.println(encrypted);
//        System.out.println(decrypt(encrypted,doSomething("matlaloup")));
//    }

    public static String doSomething(String somethingElse) {
        StringBuilder result = new StringBuilder();
        char[] chars = somethingElse.toCharArray();
        int var = chars.length;
        for (int i = 0; i < chars.length; i++) {
            result.append(Integer.toHexString((int)chars[i]).toUpperCase());
        }
        String partialResult = result.toString();
        if(partialResult.length()>32){
            return partialResult.substring(0,32);
        } else {
            while (result.length()<32){
                result.append("0");
            }
            return result.toString();
        }
    }

    public static String encrypt(final String plainMessage,
                                 final String symKeyHex) {
        final byte[] symKeyData = Base64.decode(symKeyHex, Base64.DEFAULT);

        final byte[] encodedMessage = plainMessage.getBytes(Charset
                .forName("UTF-8"));
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final int blockSize = cipher.getBlockSize();

            // create the key
            final SecretKeySpec symKey = new SecretKeySpec(symKeyData, "AES");

            // generate random IV using block size (possibly create a method for
            // this)
            final byte[] ivData = new byte[blockSize];
            final SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
            rnd.nextBytes(ivData);
            final IvParameterSpec iv = new IvParameterSpec(ivData);

            cipher.init(Cipher.ENCRYPT_MODE, symKey, iv);

            final byte[] encryptedMessage = cipher.doFinal(encodedMessage);

            // concatenate IV and encrypted message
            final byte[] ivAndEncryptedMessage = new byte[ivData.length
                    + encryptedMessage.length];
            System.arraycopy(ivData, 0, ivAndEncryptedMessage, 0, blockSize);
            System.arraycopy(encryptedMessage, 0, ivAndEncryptedMessage,
                    blockSize, encryptedMessage.length);

            final String ivAndEncryptedMessageBase64 = Base64.encodeToString(ivAndEncryptedMessage,Base64.DEFAULT);

            return ivAndEncryptedMessageBase64;
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(
                    "key argument does not contain a valid AES key");
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Unexpected exception during encryption", e);
        }
    }

    public static String decrypt(final String ivAndEncryptedMessageBase64,
                                 final String symKeyHex) {
        final byte[] symKeyData = Base64.decode((symKeyHex),Base64.DEFAULT);

        final byte[] ivAndEncryptedMessage = Base64.decode(ivAndEncryptedMessageBase64,Base64.DEFAULT);
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            final int blockSize = cipher.getBlockSize();

            // create the key
            final SecretKeySpec symKey = new SecretKeySpec(symKeyData, "AES");

            // retrieve random IV from start of the received message
            final byte[] ivData = new byte[blockSize];
            System.arraycopy(ivAndEncryptedMessage, 0, ivData, 0, blockSize);
            final IvParameterSpec iv = new IvParameterSpec(ivData);

            // retrieve the encrypted message itself
            final byte[] encryptedMessage = new byte[ivAndEncryptedMessage.length
                    - blockSize];
            System.arraycopy(ivAndEncryptedMessage, blockSize,
                    encryptedMessage, 0, encryptedMessage.length);

            cipher.init(Cipher.DECRYPT_MODE, symKey, iv);

            final byte[] encodedMessage = cipher.doFinal(encryptedMessage);

            // concatenate IV and encrypted message
            final String message = new String(encodedMessage,
                    Charset.forName("UTF-8"));

            return message;
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException(
                    "key argument does not contain a valid AES key");
        } catch (BadPaddingException e) {
            // you'd better know about padding oracle attacks
            return null;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "Unexpected exception during decryption", e);
        }
    }

//    public static String encrypt(String seed, String cleartext) {
//        try {
//            byte[] rawKey = getRawKey(seed.getBytes());
//            byte[] result = encrypt(rawKey, cleartext.getBytes());
//            return toHex(result);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static String decrypt(String seed, String encrypted) {
//        try {
//            byte[] rawKey = getRawKey(seed.getBytes());
//            byte[] enc = toByte(encrypted);
//            byte[] result = decrypt(rawKey, enc);
//            return new String(result);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static byte[] getRawKey(byte[] seed) throws Exception {
//        KeyGenerator kgen = KeyGenerator.getInstance("AES");
//        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
//        sr.setSeed(seed);
//        kgen.init(128, sr); // 192 and 256 bits may not be available
//        SecretKey skey = kgen.generateKey();
//        byte[] raw = skey.getEncoded();
//        return raw;
//    }
//
//    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
//        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
//        Cipher cipher = Cipher.getInstance("AES");
//        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
//        byte[] encrypted = cipher.doFinal(clear);
//        return encrypted;
//    }
//
//    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
//        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
//        Cipher cipher = Cipher.getInstance("AES");
//        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
//        byte[] decrypted = cipher.doFinal(encrypted);
//        return decrypted;
//    }
//
//    public static String toHex(String txt) {
//        return toHex(txt.getBytes());
//    }
//
//    public static String fromHex(String hex) {
//        return new String(toByte(hex));
//    }
//
//    public static byte[] toByte(String hexString) {
//        int len = hexString.length() / 2;
//        byte[] result = new byte[len];
//        for (int i = 0; i < len; i++)
//            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
//        return result;
//    }
//
//    public static String toHex(byte[] buf) {
//        BigInteger bi = new BigInteger(1, buf);
//        return String.format("%0" + (buf.length << 1) + "X", bi);
//    }

}

