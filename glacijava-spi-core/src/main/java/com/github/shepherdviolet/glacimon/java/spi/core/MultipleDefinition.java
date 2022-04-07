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
 * Multiple service definition loaded from file
 *
 * @author S.Violet
 */
class MultipleDefinition {

    private final boolean isDisable;
    private final String interfaceType;
    private final String implementationType;
    private final int rank;
    private final String url;
    private Integer implementationHash;

    MultipleDefinition(boolean isDisable, String interfaceType, String implementationType, int rank, String url) {
        this.isDisable = isDisable;
        this.interfaceType = interfaceType;
        this.implementationType = implementationType;
        this.rank = rank;
        this.url = url;
    }

    boolean isDisable() {
        return isDisable;
    }

    String getInterfaceType() {
        return interfaceType;
    }

    String getImplementationType() {
        return implementationType;
    }

    int getRank() {
        return rank;
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
        return "MultipleDefinition{" +
                "isDisable=" + isDisable +
                ", interfaceType='" + interfaceType + '\'' +
                ", implementationType='" + implementationType + '\'' +
                ", rank=" + rank +
                '}';
    }
}
