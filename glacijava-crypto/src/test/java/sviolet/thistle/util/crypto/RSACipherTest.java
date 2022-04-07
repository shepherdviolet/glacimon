package sviolet.thistle.util.crypto;

import org.junit.Assert;
import org.junit.Test;
import sviolet.thistle.util.conversion.Base64Utils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;

public class RSACipherTest {

    private static final String TEST_FILE = "../LICENSE";

    private static final String STRING = "English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413";
    private static final String PUBLIC = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhhTxwCe+Pamw1J5J5vvdw76Abt74hnqsvUPiz86iaq2XwmUUy0UV7Li+e+y1Qu60s8MSDAysMYdUfV2W0Y+/v0fs1mA57rjBq/T6dblZBxempToS0PnjZquiOo3J/laOWJ4QfHpGk0grOGET5ETeHNJMNURdsnCmFqbqXPypoq0zdD5gIbu7lC9bbhrPV+bQrKn2uf6eUzuSzjXkOgavTf5F6zdeTDk6xuRrxqQTGyjwqiLz5caM54eMSSoWdNGEfBj2yIvcMJ/jSJ3i9NxQIPEYy6/x39irx1DJo9/9vUfBW2PrpxdLCoFB7au4imKEZJBCnJlQ4gBwoAcyj4CiUwIDAQAB";
    private static final String PRIVATE = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCGFPHAJ749qbDUnknm+93DvoBu3viGeqy9Q+LPzqJqrZfCZRTLRRXsuL577LVC7rSzwxIMDKwxh1R9XZbRj7+/R+zWYDnuuMGr9Pp1uVkHF6alOhLQ+eNmq6I6jcn+Vo5YnhB8ekaTSCs4YRPkRN4c0kw1RF2ycKYWpupc/KmirTN0PmAhu7uUL1tuGs9X5tCsqfa5/p5TO5LONeQ6Bq9N/kXrN15MOTrG5GvGpBMbKPCqIvPlxoznh4xJKhZ00YR8GPbIi9wwn+NIneL03FAg8RjLr/Hf2KvHUMmj3/29R8FbY+unF0sKgUHtq7iKYoRkkEKcmVDiAHCgBzKPgKJTAgMBAAECggEARzvlXBNBTP1URwF5fdAd95rEHSM0oi2WjE7+tyyuuUJUuCB/taaUiVl1Sd1uR4sOUdq3QdORwBaH2rrYH6nhUzxhJVumK5/YtQTH87dvBweXr3x09rVsDOXuoHEn+Yn+wjHnzFoiGKlk3OUmbkXwQxuvBTW3GdAjTTGMna5WaWgXNYe/vgUbHcdZXDGTPIKMPM4b0Ope76y16OKcW5GyTawJo+BbafDt5ymHz3NKz/8jBy0tMhEEmhfOpmMCnDQfW09R4uVZ9iZVdW6i0MrgeXtoeSmTTThaq0SxJmo5yCSR9l4/Ao1iuHrIVbw0OHgWo8TwdxtpCRJ/GTcId0dT8QKBgQDd5U1HmUw2FUbFNRlUmuNJasHBliv++XIq5zWHFSoY6NHj+0kheBe3xkMWBHrJuFR17LCTAGE4Y+AVVal/NOHeOEuqCI4TIzwmXjU5RYESiW2wpIyVImpVoMJIIkB0vB4TUTa/I3PDO672DReQMpUetHAjZxEw8T5p8kKVginUVQKBgQCasIVjBAKyIjvlrWdrQNiVinHIiSOarJfpaZ9Yd2pUjPo2yxRui08c+m3JZELx7Pv0d01gvWlH52U6Sp4RLMyrXyX6Iq61xxUoOFSD3iowolKHEsk3i4oSL8vO4Ei62hbEtUDakmZVXuAsBbg+e9tYGgE9VuI4s0AMCJXak2CEBwKBgQDcTQ3+qdoaT+FlZaRydRx3BYC8XAXuLWYF5mskZATZmjzYZeFjU0Ho+PQd5fCqWVGxin58VAIx5CbTx6pyWuRspeOpOrkjkvXi+eFJrRHKf5rhp1zTq8l6nhKFX0wzGZmagCUke2QxTw4Tx0e/qBiY1XgIDgnpV3op4ZXtrb8tyQKBgG2KfRqN936/gVz2u5qW1AipfkO84Yqhl/3BPwa9oX14S6PLkY9qdT6XFHGd305EPHN0nEXaO5iggu2Rc3fEGrdsbI9CSigNb60Insi25XeFKx3drsH3vXF9iTzZVIeE8sSfeeqN64ue4O4rqroMqVotKB2QzifKv5sF5WBgJuO3AoGAeRTbMxnOf25YZFOjwcAVMW+2EVUF161IsvyMSTMrySoN7mrURsbDfskiwGFN3VVQ/SO0UGgCleRDkDbSCAfC+jTwFnlg2woaPlaT2isbe/J1i4SWJJp4g8R6oGd8DxFGQVYXJwyLSqiWK/aUKElAzoK6I5sIddz4LLXN63H3DNs=";

    @Test
    public void bytesSignVerify() throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        RSAKeyGenerator.RSAKeyPair pair = RSAKeyGenerator.generateKeyPair();

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(pair);

        byte[] sign = RSACipher.sign(dataBytes, pair.getPrivateKey(), RSACipher.SIGN_ALGORITHM_RSA_SHA1);

//        System.out.println(ByteUtils.bytesToHex(sign));

        Assert.assertEquals(true, RSACipher.verify(dataBytes, sign, pair.getPublicKey(), RSACipher.SIGN_ALGORITHM_RSA_SHA1));

    }

    @Test
    public void fileIoSignVerify() throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {

        RSAPrivateKey privateKey = RSAKeyGenerator.generatePrivateKeyByPKCS8(Base64Utils.decode(PRIVATE));
        RSAPublicKey publicKey = RSAKeyGenerator.generatePublicKeyByX509(Base64Utils.decode(PUBLIC));

        byte[] sign = RSACipher.signIo(new File(TEST_FILE), privateKey, RSACipher.SIGN_ALGORITHM_RSA_MD5);

//        System.out.println(ByteUtils.bytesToHex(sign));

        Assert.assertEquals(true, RSACipher.verifyIo(new File(TEST_FILE), sign, publicKey, RSACipher.SIGN_ALGORITHM_RSA_MD5));

    }

    @Test
    public void fileNioSignVerify() throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {

        RSAPrivateKey privateKey = RSAKeyGenerator.generatePrivateKeyByPKCS8(Base64Utils.decode(PRIVATE));
        RSAPublicKey publicKey = RSAKeyGenerator.generatePublicKeyByX509(Base64Utils.decode(PUBLIC));

        byte[] sign = RSACipher.signNio(new File(TEST_FILE), privateKey, RSACipher.SIGN_ALGORITHM_RSA_SHA256);

//        System.out.println(ByteUtils.bytesToHex(sign));

        Assert.assertEquals(true, RSACipher.verifyNio(new File(TEST_FILE), sign, publicKey, RSACipher.SIGN_ALGORITHM_RSA_SHA256));

    }

    @Test
    public void encryptDecrypt1() throws InvalidKeySpecException, IOException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        RSAPrivateKey privateKey = RSAKeyGenerator.generatePrivateKeyByPKCS8(Base64Utils.decode(PRIVATE));
        RSAPublicKey publicKey = RSAKeyGenerator.generatePublicKeyByX509(Base64Utils.decode(PUBLIC));

        byte[] encrypted = RSACipher.encrypt(dataBytes, publicKey, RSACipher.CRYPTO_ALGORITHM_RSA_ECB_PKCS1PADDING);

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        byte[] decrypted = RSACipher.decrypt(encrypted, privateKey, RSACipher.CRYPTO_ALGORITHM_RSA_ECB_PKCS1PADDING);

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assert.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

    @Test
    public void encryptDecrypt2() throws InvalidKeySpecException, IOException, NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        RSAPrivateKey privateKey = RSAKeyGenerator.generatePrivateKeyByPKCS8(Base64Utils.decode(PRIVATE));
        RSAPublicKey publicKey = RSAKeyGenerator.generatePublicKeyByX509(Base64Utils.decode(PUBLIC));

        byte[] encrypted = RSACipher.encrypt(dataBytes, privateKey, RSACipher.CRYPTO_ALGORITHM_RSA_ECB_PKCS1PADDING);

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        byte[] decrypted = RSACipher.decrypt(encrypted, publicKey, RSACipher.CRYPTO_ALGORITHM_RSA_ECB_PKCS1PADDING);

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assert.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

}
