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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.helper.hessianlite;

import com.alibaba.com.caucho.hessian.io.Hessian2Input;
import com.alibaba.com.caucho.hessian.io.Hessian2Output;
import com.alibaba.com.caucho.hessian.io.SerializerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * hessian-lite序列化工具, 服务端场合建议自定义, 调整参数优化性能增加扩展性, 依赖: com.alibaba:hessian-lite
 *
 * @author S.Violet
 */
public class HessianLiteSerializeUtils {

    private static final SerializerFactory serializerFactory;
    private static final int BUFF_SIZE = 1024;

    static {
        serializerFactory = new SlateHessianLiteSerializerFactory();
        serializerFactory.setAllowNonSerializable(true);
    }

    /**
     * 反序列化
     * @param data 数据
     * @param type 类型
     * @param <T> 类型
     * @return 对象
     */
    public static <T> T deserialize(byte[] data, Class<T> type) throws Exception {
        if (data == null) {
            return null;
        }
        InputStream inputStream = new ByteArrayInputStream(data);
        Hessian2Input hessian2Input = null;
        Object result = null;
        try {
            hessian2Input = new Hessian2Input(inputStream);
            hessian2Input.setSerializerFactory(serializerFactory);
            result = hessian2Input.readObject();
        } finally {
            if (hessian2Input != null) {
                try {
                    hessian2Input.close();
                } catch (Exception ignore){
                }
            }
        }
        if (type != null && result != null && !type.isAssignableFrom(result.getClass())) {
            throw new RuntimeException("Deserialize error, result is not an instance of " + type.getName());
        }
        return (T) result;
    }

    /**
     * 序列化
     * @param obj 对象
     * @return 数据
     */
    public static byte[] serialize(Object obj) throws Exception {
        if (obj == null) {
            return new byte[0];
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(BUFF_SIZE);
        Hessian2Output hessian2Output = null;
        try {
            hessian2Output = new Hessian2Output(outputStream);
            hessian2Output.setSerializerFactory(serializerFactory);
            hessian2Output.writeObject(obj);
        } finally {
            if (hessian2Output != null) {
                try {
                    hessian2Output.close();
                } catch (Exception ignore){
                }
            }
        }
        return outputStream.toByteArray();
    }

}
