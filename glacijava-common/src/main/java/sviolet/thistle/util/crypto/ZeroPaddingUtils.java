/*
 * Copyright (C) 2015-2018 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/thistle
 * Email: shepherdviolet@163.com
 */

package sviolet.thistle.util.crypto;

/**
 * 数据0填充
 *
 * @author S.Violet
 */
public class ZeroPaddingUtils {

    /**
     * 数据使用0填充至指定块大小
     * @param data 数据
     * @param blockSize 加密需要的数据块大小, 单位bytes, 例如:DESEDE需要8bytes(64位)
     * @return 填充后的数据
     */
    public static byte[] padding(byte[] data, int blockSize) {
        if (blockSize <= 0) {
            throw new IllegalArgumentException("Invalid blockSize:" + blockSize);
        }
        if (data == null) {
            return new byte[blockSize];
        }
        if (data.length % blockSize == 0) {
            return data;
        }
        byte[] result = new byte[(data.length / blockSize + 1) * blockSize];
        System.arraycopy(data, 0, result, 0, data.length);
        return result;
    }

    /**
     * 逆填充, 即去除尾部的0填充
     * @param data 数据
     * @return 去除尾部0的数据
     */
    public static byte[] trimZero(byte[] data) {
        if (data == null) {
            return null;
        }
        for (int i = data.length - 1 ; i >= 0 ; i--) {
            if (data[i] != 0) {
                byte[] result = new byte[i + 1];
                System.arraycopy(data, 0, result, 0, result.length);
                return result;
            }
        }
        return new byte[0];
    }

}
