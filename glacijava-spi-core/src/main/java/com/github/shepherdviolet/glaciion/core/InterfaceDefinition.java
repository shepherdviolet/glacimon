/*
 * Copyright (C) 2019-2019 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/glaciion
 * Email: shepherdviolet@163.com
 */

package com.github.shepherdviolet.glaciion.core;

/**
 * Interface definition loaded from file
 *
 * @author S.Violet
 */
class InterfaceDefinition {

    private final String interfaceType;
    private final String url;
    private Integer interfaceHash;

    InterfaceDefinition(String interfaceType, String url) {
        this.interfaceType = interfaceType;
        this.url = url;
    }

    String getInterfaceType() {
        return interfaceType;
    }

    String getUrl() {
        return url;
    }

    /**
     * WARNING calculate once only
     */
    int calculateInterfaceHash(){
        if (interfaceType == null) {
            return 0;
        }
        if (interfaceHash == null) {
            interfaceHash = interfaceType.hashCode();
        }
        return interfaceHash;
    }

    @Override
    public String toString() {
        return "InterfaceDefinition{" +
                "interfaceType='" + interfaceType + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return calculateInterfaceHash();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InterfaceDefinition) {
            if (interfaceType != null) {
                return interfaceType.equals(((InterfaceDefinition) obj).getInterfaceType());
            } else {
                return super.equals(obj);
            }
        } else {
            return false;
        }
    }
}
