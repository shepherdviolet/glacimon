package glacimon;

import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricConfig;
import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricStringEncryptor;
import com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography;
import org.jasypt.encryption.StringEncryptor;

/**
 * <p>Spring属性加解密工具-非对称 (适用于非Springboot或低版本Springboot项目, Springboot2.0+请使用jasypt-spring-boot-starter).</p>
 * <p>与jasypt-spring-boot-starter默认加解密逻辑相同, 可以根据实际需求调整代码.</p>
 * <p>依赖: com.github.ulisesbocchio:jasypt-spring-boot</p>
 * <p></p>
 *
 * <p>Spring属性(参数/配置)加密方案, 例如: 数据库密码加密, 应用私钥加密等.
 * SpringBoot2.0以上可以使用jasypt-spring-boot-starter对Spring Environment中的所有Properties进行解密.
 * 非SpringBoot或低版本SpringBoot项目我们利用jasypt和SpEL实现部分属性解密.</p>
 * <p></p>
 *
 * <pre>{@code
 *
 * * XML中支持SpEL
 *
 * 	<bean id="dataSource" class="org.apache.tomcat.jdbc.pool.DataSource" destroy-method="close">
 * 	  <property name="url" value="${datasource.url}" />
 * 	  <property name="username" value="${datasource.username}" />
 *     <!-- XML中支持SpEL, 利用AsymParamEnc对密文解密 -->
 * 	  <property name="password" value="#{T(glacimon.AsymParamEnc).decrypt('${datasource.password}', '${paramEnc.privateKey}')}" />
 *     ......
 * 	</bean>
 *
 * * @Value中支持SpEL
 *
 *   //@Value中支持SpEL, 利用AsymParamEnc对密文解密
 *   @Value("#{T(glacimon.AsymParamEnc).decrypt('${datasource.password}', '${paramEnc.privateKey}')}")
 *   private String password;
 *
 * * 属性设置密文, ENC(开头, )结尾, 支持设置明文(明文就不解密)
 *
 * datasource.password=ENC(ES3JF+WF......Qj+1JBI=)
 *
 * * 可以在启动参数配置私钥(DER格式)
 *
 * -DparamEnc.privateKey=MIICdwIB......KM2wnjk1ZY=
 *
 * * 可以在properties中配置私钥文件路径(私钥文件PEM格式, 头部必须是`-----BEGIN PRIVATE KEY-----`尾部必须是`-----END PRIVATE KEY-----`)
 *
 * paramEnc.privateKey=file:/home/yourname/jasypt_private.pem
 *
 * }</pre>
 */
public class AsymParamEnc {

    /**
     * 加密
     * @param plain 明文
     * @param publicKey 公钥, 支持DER格式字符串, 支持PEM格式文件路径(文件路径file:或classpath:开头)
     */
    public static String encrypt(String plain, String publicKey) {
        // 送空默认不加密
        if (plain == null || plain.isEmpty() || publicKey == null || publicKey.isEmpty()) {
            return plain;
        }

        SimpleAsymmetricConfig config = new SimpleAsymmetricConfig();

        // 公钥: file:或classpath:开头视为PEM格式的文件路径, 否则视为DER格式的字符串
        if (publicKey.startsWith("file:") || publicKey.startsWith("classpath:")) {
            // 文件默认PEM格式, "-----BEGIN PUBLIC KEY-----"开头, "-----END PUBLIC KEY-----"结尾, 如果文件内不是PEM格式, 请自行修改
            config.setKeyFormat(AsymmetricCryptography.KeyFormat.PEM);
            config.setPublicKeyLocation(publicKey);
        } else {
            // 字符串默认DER格式, 就是一串BASE64, 如果不是DER格式, 请自行修改
            config.setPublicKey(publicKey);
        }

        StringEncryptor encryptor = new SimpleAsymmetricStringEncryptor(config);
        try {
            // 追加头尾
            return "ENC(" + encryptor.encrypt(plain) + ")";
        } catch (Throwable t) {
            throw new RuntimeException("Encryption failed, plain: " + plain + ", publicKey: " + publicKey, t);
        }
    }

    /**
     * 加密
     * @param cipher 密文, "ENC("开头, ")"结尾, 头尾不符不解密返回
     * @param privateKey 私钥, 支持DER格式字符串, 支持PEM格式文件路径(文件路径file:或classpath:开头)
     */
    public static String decrypt(String cipher, String privateKey) {
        // 送空默认不解密
        if (cipher == null || cipher.isEmpty() || privateKey == null || privateKey.isEmpty()) {
            return cipher;
        }
        // 头尾不是"ENC()"或"ENC[]"不解密
        if (!(cipher.startsWith("ENC(") && cipher.endsWith(")"))
                && !(cipher.startsWith("ENC[") && cipher.endsWith("]"))) {
            return cipher;
        }

        SimpleAsymmetricConfig config = new SimpleAsymmetricConfig();

        // 私钥: file:或classpath:开头视为PEM格式的文件路径, 否则视为DER格式的字符串
        if (privateKey.startsWith("file:") || privateKey.startsWith("classpath:")) {
            // 文件默认PEM格式, "-----BEGIN PRIVATE KEY-----"开头, "-----END PRIVATE KEY-----"结尾, 如果文件内不是PEM格式, 请自行修改
            config.setKeyFormat(AsymmetricCryptography.KeyFormat.PEM);
            config.setPrivateKeyLocation(privateKey);
        } else {
            // 字符串默认DER格式, 就是一串BASE64, 如果不是DER格式, 请自行修改
            config.setPrivateKey(privateKey);
        }

        StringEncryptor encryptor = new SimpleAsymmetricStringEncryptor(config);
        try {
            // 去掉头尾
            String rawCipher = cipher.substring(4, cipher.length() - 1);
            return encryptor.decrypt(rawCipher);
        } catch (Throwable t) {
            if (privateKey.startsWith("file:") || privateKey.startsWith("classpath:")) {
                throw new RuntimeException("Decryption failed, cipher: " + cipher + ", privateKey: " + privateKey, t);
            }
            throw new RuntimeException("Decryption failed, cipher: " + cipher + ", privateKey: ******", t);
        }
    }

}
