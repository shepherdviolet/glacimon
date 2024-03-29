package glacimon;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

/**
 * <p>Spring属性加解密工具-对称 (适用于非Springboot或低版本Springboot项目, Springboot2.0+请使用jasypt-spring-boot-starter).</p>
 * <p>与jasypt-spring-boot-starter默认加解密逻辑相同, 但加密算法改为PBEWITHHMACSHA512ANDAES_128 (兼容老版本JDK), 可以根据实际需求调整代码.</p>
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
 *     <!-- XML中支持SpEL, 利用ParamEnc对密文解密; decrypt方法第二个参数'密钥'建议设置个默认值'空'(就是key:), 这样不设置密钥也不会报错, 只是不解密(保持原文) -->
 * 	  <property name="password" value="#{T(glacimon.ParamEnc).decrypt('${datasource.password}', '${paramEnc.password:}')}" />
 *     ......
 * 	</bean>
 *
 * * @Value中支持SpEL
 *
 *   //@Value中支持SpEL, 利用ParamEnc对密文解密; decrypt方法第二个参数'密钥'建议设置个默认值'空'(就是key:), 这样不设置密钥也不会报错, 只是不解密(保持原文)
 *   @Value("#{T(glacimon.ParamEnc).decrypt('${datasource.password}', '${paramEnc.password:}')}")
 *   private String password;
 *
 * * 属性设置密文, ENC(开头, )结尾, 支持设置明文(明文就不解密)
 *
 * datasource.password=ENC(ES3JF+WF......Qj+1JBI=)
 *
 * * 可以在启动参数配置密码
 *
 * -DparamEnc.password=IPRGkutx3FfsCYty
 *
 * }</pre>
 */
public class ParamEnc {

    /**
     * 加密
     * @param plain 明文
     * @param password 密钥
     */
    public static String encrypt(String plain, String password) {
        // 送空默认不加密
        if (plain == null || plain.isEmpty() || password == null || password.isEmpty()) {
            return plain;
        }

        try {
            // 追加头尾
            return "ENC(" + encryptor(password).encrypt(plain) + ")";
        } catch (Throwable t) {
            throw new RuntimeException("Encryption failed, plain: " + plain + ", password: ******", t);
        }
    }

    /**
     * 加密
     * @param cipher 密文, "ENC("开头, ")"结尾, 头尾不符不解密返回
     * @param password 密钥
     */
    public static String decrypt(String cipher, String password) {
        // 送空默认不解密
        if (cipher == null || cipher.isEmpty() || password == null || password.isEmpty()) {
            return cipher;
        }
        // 头尾不是"ENC()"或"ENC[]"不解密
        if (!(cipher.startsWith("ENC(") && cipher.endsWith(")"))
                && !(cipher.startsWith("ENC[") && cipher.endsWith("]"))) {
            return cipher;
        }

        try {
            // 去掉头尾
            String rawCipher = cipher.substring(4, cipher.length() - 1);
            return encryptor(password).decrypt(rawCipher);
        } catch (Throwable t) {
            throw new RuntimeException("Decryption failed, cipher: " + cipher + ", password: ******", t);
        }
    }

    /**
     * jasypt-spring-boot-starter 默认的加密器.
     * 如果配置了自定义加密器或者参数, 请相应调整.
     */
    public static StandardPBEStringEncryptor encryptor(String password) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password);
        // algorithm PBEWITHHMACSHA512ANDAES_128 -> PBEWITHHMACSHA512ANDAES_256. Adapt to lower versions of JDK.
        // 默认是PBEWITHHMACSHA512ANDAES_256, 为了兼容老版本JDK改为128
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_128");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName(null);
        config.setProviderClassName(null);
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }

}
