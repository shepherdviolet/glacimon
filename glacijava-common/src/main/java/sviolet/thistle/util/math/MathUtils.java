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

package sviolet.thistle.util.math;

/**
 * 数学工具
 *
 * @author S.Violet
 */
public class MathUtils {

    /* ********************************************************************
     * trigonometric
     */

    /**
     * <p>
     *    根据角度计算sin值 <br/>
     *    Math.sin()中的参数为弧度值, 弧度值 = 角度 * PI / 180 <br/>
     * </p>
     *
     * @param angle [0, 360]
     * @return sin值 [-1, 1]
     */
    public static double sinAngle(double angle){
        return Math.sin(angle * Math.PI / 180);
    }

    /**
     * <p>
     *    根据sin值计算角度 <br/>
     *    Math.asin()返回值为弧度值, 弧度值 = 角度 * PI / 180 <br/>
     * </p>
     *
     * @param sin sin值 [-1, 1]
     * @return 角度 [0, 360]
     */
    public static double asinAngle(double sin){
        return Math.asin(sin) / (Math.PI / 180);
    }

    /**
     * <p>
     *    根据角度计算tan值 <br/>
     *    Math.tan()中的参数为弧度值, 弧度值 = 角度 * PI / 180 <br/>
     * </p>
     *
     * @param angle [0, 360]
     * @return tan值
     */
    public static double tanAngle(double angle){
        return Math.tan(angle * Math.PI / 180);
    }

    /**
     * <p>
     *    根据tan值计算角度 <br/>
     *    Math.atan()返回值为弧度值, 弧度值 = 角度 * PI / 180 <br/>
     * </p>
     *
     * @param tan tan值
     * @return 角度 [0, 360]
     */
    public static double atanAngle(double tan){
        return Math.atan(tan) / (Math.PI / 180);
    }

    /* ***********************************************************************************
     * other
     */

    /**
     * 角度转为弧度(rad)
     */
    public static double rad(double degrees) {
        return degrees * Math.PI / 180.0;
    }

    /**
     * 角度标准化, 将任意不在0-360范围内的角度转换为0-360范围内的角度, 例如:-90 -> 270
     * @param angle 角度
     * @return 0-360范围的角度
     */
    public static float standardizeAngle(float angle){
        float m = angle % 360;
        return m > 0 ? m : m + 360;
    }

    /**
     * 判断数字是否为2的幂次, 例如1 2 4 8 16 ... 512 1024 ... (0 返回 false)
     * @param value 数字
     * @return true:为2的幂次
     */
    public static boolean isPowerOfTwo(int value){
        return value > 0 && (value & value - 1) == 0;
    }

    /**
     * <p>用atan实现atan2.</p>
     * <p></p>
     * <p>注意!!! 请使用Math.atan2(x, y), 本方法用于给其他不支持atan2的语言提供参考.</p>
     * <p></p>
     * <p><pre>
     *      结果转换为角度: <br>
     *      角度 = atan2(x, y) * 180d / Math.PI
     *      取值范围: (-180, 180]
     *      说明: "原点到指定点(x, y)的连线"与"X轴正方向"的夹角
     * </pre></p>
     *
     * @param x X
     * @param y Y
     * @param undefinedValue 当x=0,y=0时, 返回该值
     * @return 取值范围: (-PI, PI]
     * @deprecated 请使用Math.atan2(x, y), 本方法用于给其他不支持atan2的语言提供参考
     */
    @Deprecated
    public static double atan2(double x, double y, double undefinedValue) {
        if (x > 0d) {
            return Math.atan(y / x);
        } else if (x < 0d && y >= 0d) {
            return Math.atan(y / x) + Math.PI;
        } else if (x < 0d && y < 0d) {
            return Math.atan(y / x) - Math.PI;
        } else if (x == 0d && y > 0d) {
            return Math.PI / 2;
        } else if (x == 0d && y < 0d) {
            return - Math.PI / 2;
        } else {
            // x = 0, y = 0, undefined
            return undefinedValue;
        }
    }

}
