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

/**
 * Single service definition loaded from file
 *
 * @author S.Violet
 */
class SingleDefinition {

    private final String interfaceType;
    private final String implementationType;
    private final int priority;
    private final String url;
    private Integer implementationHash;

    SingleDefinition(String interfaceType, String implementationType, int priority, String url) {
        this.interfaceType = interfaceType;
        this.implementationType = implementationType;
        this.priority = priority;
        this.url = url;
    }

    String getInterfaceType() {
        return interfaceType;
    }

    String getImplementationType() {
        return implementationType;
    }

    int getPriority() {
        return priority;
    }

    String getUrl() {
        return url;
    }

    /**
     * WARNING calculate once only
     */
    int calculateImplementationHash(){
        if (implementationType == null) {
            return 0;
        }
        if (implementationHash == null) {
            implementationHash = implementationType.hashCode();
        }
        return implementationHash;
    }

    @Override
    public String toString() {
        return "SingleDefinition{" +
                "interfaceType='" + interfaceType + '\'' +
                ", implementationType='" + implementationType + '\'' +
                ", priority=" + priority +
                '}';
    }
}
