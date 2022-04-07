# 国际/国密算法加解密/加解签手册

* [Source Code](https://github.com/shepherdviolet/thistle/tree/master/thistle-common/src/main/java/sviolet/thistle/util/crypto) | [Source Code (Advanced)](https://github.com/shepherdviolet/thistle/tree/master/thistle-crypto-plus/src/main/java/sviolet/thistle/util/crypto)
* 这些加密工具`线程安全`
* `Maven/Gradle依赖配置`在本文最后

# 小知识

* 常见的加密算法分为`对称算法`和`非对称算法`, 对称算法通常用于加解密, 非对称算法通常用于签名验签

### 对称算法

* 约定对称算法时, 要约定`加密算法`/`密钥长度`/`填充算法`, 例如:AES, 128位密钥(16bytes), ECB/PKCS5Padding填充算法
* `填充算法`:数据加密时, 通常要求数据长度符合块(block)的倍数, 因此需要一种算法将数据填充到规定长度. 银联有自定义的填充方式, 另外还有全补0的填充方式, 这些需要自行实现.
* AES/SM4算法密钥长度通常为128位(16bytes)
* DES算法密钥长度通常为64位(8bytes)
* 3DES(DESede)算法秘钥长度通常为128位(16bytes)和192位(24bytes)
* 对称密钥通常转为Base64传输

### 非对称算法

* 约定非对称算法时, 要约定`签名算法(包含摘要方式)`/`密钥长度`, 例如:SHA256withRSA(RSA算法, SHA256摘要), 2048位密钥(注意RSA的密钥长度不等于密钥字符串的长度)
* RSA/SM2算法除了签名验签, 还能进行数据加解密, 但效率较低, 通常用于交换对称密钥(加密AES/SM4密钥等)
* RSA用到数据加解密时, 还需要约定`填充算法`, 例如:ECB/PKCS1Padding
* 国际非对称算法除了RSA, 还有ECC算法(椭圆曲率算法), JDK中实现了ECDSA签名验签, 未提供加解密
* ECDSA对比RSA, 拥有更短的密钥长度, 更快的签名速度, 和理论上更高的安全性
* RSA算法密钥长度通常为1024位和2048位, `私钥`自己保管, `公钥`提供给对方
* RSA签名方式有`MD5withRSA`/`SHA1withRSA`/`SHA256withRSA`, 建议使用SHA256withRSA
* SM2算法需要约定椭圆曲线参数, 官方推荐`sm2p256v1`, 密钥长度256位
* SM2用到数据加解密时, 需要约定密文格式, 有 `C1C2C3` / `C1C3C2` 两种, 
* SM2签名方式目前只有`SM2withSM3`

### 摘要算法

* 摘要(杂凑)算法是将一段数据不可逆地转为一段固定的摘要信息, 用于验证数据是否被篡改/是否一致, 或配合非对称算法进行签名
* 常见的摘要算法有MD5/SHA1/SHA256/SM3

# 摘要

### DigestCipher

* MD5/SHA1/SHA256摘要

### SM3DigestCipher (thistle-crypto-plus)

* SM3摘要

# RSA 加解密/加解签

### RSAKeyGenerator

* RSA密钥生成
* 将密钥转换为各种编码

* 解析密钥示例

```gradle
    RSAPublicKey publicKey = RSAKeyGenerator.generatePublicKeyByX509(publicKeyData);
    RSAPrivateKey privateKey = RSAKeyGenerator.generatePrivateKeyByPKCS8(privateKeyData);
```

### RSACipher

* RSA签名/验签
* RSA加密/解密

* 签名示例

```gradle
    //支持byte[]数据和File文件的签名
    byte[] sign = RSACipher.sign(data, privateKey, RSACipher.SIGN_ALGORITHM_RSA_SHA256);
```

* 验签示例

```gradle
    //支持byte[]数据和File文件的验签
    boolean valid = RSACipher.verify(data, sign, publicKey, RSACipher.SIGN_ALGORITHM_RSA_SHA256);
```

# SM2 加解密/加解签 (thistle-crypto-plus)

### SM2KeyGenerator (thistle-crypto-plus)

* SM2密钥生成
* 将密钥转换为各种编码

### SM2Cipher (thistle-crypto-plus)

* SM2签名/验签
* SM2加密/解密

# AES 加解密

### AESKeyGenerator

* AES密钥生成

* 密钥生成示例

```gradle
    byte[] key = AESKeyGenerator.generateAes128();
```

### AESCipher

* AES加密解密
* 注意, CBC填充算法需使用encryptCBC/decryptCBC方法, 并送入iv量

* 加解密示例

```gradle
    byte[] encrypted = AESKeyGenerator.encrypt(raw, key, AESKeyGenerator.CRYPTO_ALGORITHM_AES_ECB_PKCS5PADDING);
    byte[] decrypted = AESKeyGenerator.encrypt(encrypted, key, AESKeyGenerator.CRYPTO_ALGORITHM_AES_ECB_PKCS5PADDING);
```

# SM4 加解密 (thistle-crypto-plus)

### SM4KeyGenerator (thistle-crypto-plus)

* SM4密钥生成

### SM4Cipher (thistle-crypto-plus)

* AES加密解密
* 注意, CBC填充算法需使用encryptCBC/decryptCBC方法, 并送入iv量

# DES DESede(3DES) 加解密

### DESKeyGenerator

* DES密钥生成
* DESede(3DES)密钥生成

### DESCipher

* DES加密解密

### DESEdeCipher

* DESede(3DES)加密解密

# ECDSA 加解签

### ECDSAKeyGenerator

* ECDSA密钥生成
* 将密钥转换为各种编码

### ECDSACipher

* ECDSA签名验签

# 证书

### CertificateUtils

* RSA证书编码/解析

### PKCS12KeyStoreUtils

* 从p12/pfx文件中读取RSA证书和私钥
* 将RSA证书和私钥写入到p12/pfx文件中

* 读取示例

```gradle
    PKCS12KeyStoreUtils.CertificateChainAndKey certificateChainAndKey = PKCS12KeyStoreUtils.loadCertificateAndKey(
        //文件输入流
        getClassLoader().getResourceAsStream("cert/cert.p12"),
        //文件密钥
        "000000",
        //证书别名
        "alias"
        );
```

# 证书高级版 (thistle-crypto-plus)

### AdvancedCertificateUtils (thistle-crypto-plus)

* RSA证书/根证书签发
* SM2证书/根证书签发
* SM2证书编码/解析(SM2要用*Advanced方法)

### AdvancedPKCS12KeyStoreUtils (thistle-crypto-plus)

* 从p12/pfx文件中读取SM2证书和私钥(SM2要用*Advanced方法)
* 将SM2证书和私钥写入到p12/pfx文件中(SM2要用*Advanced方法)

# 其他

### PEMEncodeUtils

* 将证书/密钥数据转为PEM格式文本(crt/cer)

### SecureRandomUtils

* 高安全性的随机数产生工具

### ZeroPaddingUtils

* 特殊的0填充工具

<br>
<br>
<br>

# 依赖

```gradle

repositories {
    //Thistle in mavenCentral
    mavenCentral()
}
dependencies {
    //Common crypto utils
    compile 'com.github.shepherdviolet:thistle-common:?'
    //Advanced crypto utils
    compile 'com.github.shepherdviolet:thistle-crypto-plus:?'
}

```

```maven
    <!-- Common crypto utils -->
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-common</artifactId>
        <version>?</version> 
    </dependency>
    <!-- Advanced crypto utils -->
    <dependency>    
        <groupId>com.github.shepherdviolet</groupId>
        <artifactId>thistle-crypto-plus</artifactId>
        <version>?</version> 
    </dependency>
```
