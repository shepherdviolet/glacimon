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

import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginLookup;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;
import java.util.List;

/**
 * [Spring Boot 专用] 使用PropertySource注解加载YAML配置文件
 *
 * <pre>
 * <code>@</code>PropertySource(factory = YamlPropertySourceFactory.class, value = {
 *      "properties/yaml1.yml",
 *      "properties/yaml2.yml",
 * })
 * </pre>
 *
 * @author S.Violet
 */
public class YamlPropertySourceFactory implements PropertySourceFactory {

     /*
        @PropertySource(factory = YamlPropertySourceFactory.class, value = {
            "properties/yaml1.yml",
            "properties/yaml2.yml",
        })
     */

    private static final YamlPropertySourceLoader LOADER = new YamlPropertySourceLoader();
    protected static final EmptyPropertySource EMPTY_PROPERTY_SOURCE = new EmptyPropertySource();

    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        if (resource == null){
            return EMPTY_PROPERTY_SOURCE;
        }
        List<PropertySource<?>> propertySources = LOADER.load(resource.getResource().getFilename(), resource.getResource());
        if (propertySources == null || propertySources.size() <= 0) {
            return EMPTY_PROPERTY_SOURCE;
        }
        return propertySources.get(0);
    }

    /**
     * 空配置源
     */
    private static class EmptyPropertySource extends EnumerablePropertySource<Object> implements OriginLookup<String> {

        private static final String[] NULL_PROPERTY_NAMES = new String[0];

        private EmptyPropertySource() {
            super("YamlPropertySourceFactory.EmptyPropertySource");
        }

        @Override
        public Object getProperty(String name) {
            return null;
        }

        @Override
        public String[] getPropertyNames() {
            return NULL_PROPERTY_NAMES;
        }

        @Override
        public Origin getOrigin(String key) {
            return null;
        }

        @Override
        public boolean containsProperty(String name) {
            return false;
        }

    }

}
