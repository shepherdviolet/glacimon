/*
 * Copyright (C) 2015-2019 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.spring.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;

/**
 * [Spring Boot 专用] 使用PropertySource注解加载YAML配置文件, 如果文件不存在不会抛出异常
 *
 * <pre>
 * <code>@</code>PropertySource(factory = OptionalYamlPropertySourceFactory.class, value = {
 *      "properties/yaml1.yml",
 *      "properties/yaml2.yml",
 * })
 * </pre>
 *
 * @author S.Violet
 */
public class OptionalYamlPropertySourceFactory extends YamlPropertySourceFactory {

    /*
        @PropertySource(factory = OptionalYamlPropertySourceFactory.class, value = {
            "properties/yaml1.yml",
            "properties/yaml2.yml",
        })
     */

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        try {
            return super.createPropertySource(name, resource);
        } catch (Exception e) {
            logger.warn("No YAML files found in " + resource + ", cause by " + e);
            return EMPTY_PROPERTY_SOURCE;
        }
    }

}
