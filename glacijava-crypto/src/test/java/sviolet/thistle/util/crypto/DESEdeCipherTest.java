package sviolet.thistle.util.crypto;

import org.junit.Assert;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class DESEdeCipherTest {

    private static final String STRING = "English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413English中文#$%@#$%@GSDFG654465rq43we5■☝▌▋卍¶¶¶☹ΥΥθΕサイけにケ◆♂‥√▒卍ЫПЬрпㅂㅝㅂ㉹㉯╠╕┚╜ㅛㅛ㉰㉯⑩⒅⑯413";

    /**
     * byte[]加解密
     */
    @Test
    public void bytesCrypto() throws UnsupportedEncodingException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        byte[] key = DESKeyGenerator.generateDesEde128();

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        byte[] encrypted = DESEdeCipher.encrypt(dataBytes, key, DESEdeCipher.CRYPTO_ALGORITHM_DES_EDE_ECB_PKCS5PADDING);

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        byte[] decrypted = DESEdeCipher.decrypt(encrypted, key, DESEdeCipher.CRYPTO_ALGORITHM_DES_EDE_ECB_PKCS5PADDING);

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assert.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

    /**
     * byte[]加解密 ESB无填充
     */
    @Test
    public void bytesCryptoNoPadding() throws UnsupportedEncodingException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

        byte[] dataBytes = ZeroPaddingUtils.padding(STRING.getBytes("UTF-8"), 8);
        byte[] key = DESKeyGenerator.generateDesEde128();

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        byte[] encrypted = DESEdeCipher.encrypt(dataBytes, key, DESEdeCipher.CRYPTO_ALGORITHM_DES_EDE_ECB_NOPADDING);

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        byte[] decrypted = DESEdeCipher.decrypt(encrypted, key, DESEdeCipher.CRYPTO_ALGORITHM_DES_EDE_ECB_NOPADDING);

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assert.assertEquals(STRING, new String(ZeroPaddingUtils.trimZero(decrypted), "UTF-8"));

    }

    /**
     * byte[]加解密, CBC填充
     */
    @Test
    public void bytesCryptoCBC() throws UnsupportedEncodingException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        byte[] key = DESKeyGenerator.generateShaKey192("wowowo".getBytes());

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        byte[] encrypted = DESEdeCipher.encryptCBC(dataBytes, key, "12345678".getBytes(), DESEdeCipher.CRYPTO_ALGORITHM_DES_EDE_CBC_PKCS5PADDING);

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        byte[] decrypted = DESEdeCipher.decryptCBC(encrypted, key, "12345678".getBytes(), DESEdeCipher.CRYPTO_ALGORITHM_DES_EDE_CBC_PKCS5PADDING);

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assert.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

    /**
     * 输入输出流加解密
     */
    @Test
    public void streamCrypto() throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchProviderException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        byte[] key = DESKeyGenerator.generateDesEde192();

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        ByteArrayInputStream in = new ByteArrayInputStream(dataBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DESEdeCipher.encrypt(in, out, key, DESEdeCipher.CRYPTO_ALGORITHM_DES_EDE_ECB_PKCS5PADDING);
        byte[] encrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        in = new ByteArrayInputStream(encrypted);
        out = new ByteArrayOutputStream();
        DESEdeCipher.decrypt(in, out, key, DESEdeCipher.CRYPTO_ALGORITHM_DES_EDE_ECB_PKCS5PADDING);
        byte[] decrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assert.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

    /**
     * 输入输出流加解密
     */
    @Test
    public void streamCryptoCBC() throws IOException, NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        byte[] dataBytes = STRING.getBytes("UTF-8");
        byte[] key = DESKeyGenerator.generateShaKey192("wowowo".getBytes());

//        System.out.println(ByteUtils.bytesToHex(dataBytes));
//        System.out.println(ByteUtils.bytesToHex(key));

        ByteArrayInputStream in = new ByteArrayInputStream(dataBytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DESEdeCipher.encryptCBC(in, out, key, "12345678".getBytes(), DESEdeCipher.CRYPTO_ALGORITHM_DES_EDE_CBC_PKCS5PADDING);
        byte[] encrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(encrypted));

        in = new ByteArrayInputStream(encrypted);
        out = new ByteArrayOutputStream();
        DESEdeCipher.decryptCBC(in, out, key, "12345678".getBytes(), DESEdeCipher.CRYPTO_ALGORITHM_DES_EDE_CBC_PKCS5PADDING);
        byte[] decrypted = out.toByteArray();

//        System.out.println(ByteUtils.bytesToHex(decrypted));

        Assert.assertEquals(STRING, new String(decrypted, "UTF-8"));

    }

}
