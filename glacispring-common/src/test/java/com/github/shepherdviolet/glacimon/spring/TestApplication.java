/*
 * Copyright (C) 2022-2024 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/glacimon
 * Email: shepherdviolet@163.com
 */

package com.github.shepherdviolet.glacimon.spring;

import com.github.shepherdviolet.glacimon.spring.x.crypto.cryptoprop.EnableCryptoProp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        //这个方法只能排除spring.factories里声明的自动配置类, 对@Import导入或者@Enable注解启用的无效!
        excludeName = {
                "com.alicp.jetcache.autoconfigure.JetCacheAutoConfiguration", // 排除JetCache自动配置 (依赖了JetCache却不设置它, 启动会报错, 我们自定义了GlobalCacheConfig用不上它)
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration", // 排除数据源自动配置(暂不测试它)
                "com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure", // 排除druid数据库连接池的自动配置(暂不测试它, 而且在工程依赖druid时构建test会报错)
                "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration", // 排除(禁用)SpringSecurity (未依赖spring-boot-starter-security则无需排除)
                "org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration", // 排除(禁用)SpringSecurity (未依赖spring-boot-starter-security则无需排除)
        }
)
@EnableCryptoProp // 启用CryptoProp
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

}