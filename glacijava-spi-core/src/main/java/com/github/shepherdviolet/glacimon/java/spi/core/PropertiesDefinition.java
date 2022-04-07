/*
 * Copyright (C) 2022-2022 S.Violet
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

package com.github.shepherdviolet.glacimon.java.spi.core;

import java.util.Map;
import java.util.TreeMap;

/**
 * Properties definition loaded from file
 *
 * @author S.Violet
 */
class PropertiesDefinition {

    private final String bindType;
    private final String url;
    private int priority = 0;
    private final Map<String, String> properties = new TreeMap<>();
    private Integer propertiesHash;

    PropertiesDefinition(String bindType, String url) {
        this.bindType = bindType;
        this.url = url;
    }

    public String getBindType() {
        return bindType;
    }

    public String getUrl() {
        return url;
    }

    int getPriority() {
        return priority;
    }

    void setPriority(int priority) {
        this.priority = priority;
    }

    Map<String, String> getProperties() {
        return properties;
    }

    /**
     * WARNING calculate once only
     */
    int calculatePropertiesHash(){
        if (propertiesHash == null) {
            propertiesHash = String.valueOf(properties).hashCode();
        }
        return propertiesHash;
    }

    @Override
    public String toString() {
        return "PropertiesDefinition{" +
                "bindType='" + bindType + '\'' +
                ", priority=" + priority +
                ", properties=" + properties +
                '}';
    }
}
