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

package com.github.shepherdviolet.glacimon.java.math;

import com.github.shepherdviolet.glacimon.java.math.MathUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MathUtilsTest {

    @Test
    public void atan2(){
        Assertions.assertEquals(0d, MathUtils.atan2(0, 0, 0) * 180d / Math.PI, 1d);
        Assertions.assertEquals(0d, MathUtils.atan2(1, 0, 0) * 180d / Math.PI, 1d);
        Assertions.assertEquals(45d, MathUtils.atan2(1, 1, 0) * 180d / Math.PI, 1d);
        Assertions.assertEquals(90d, MathUtils.atan2(0, 1, 0) * 180d / Math.PI, 1d);
        Assertions.assertEquals(135d, MathUtils.atan2(-1, 1, 0) * 180d / Math.PI, 1d);
        Assertions.assertEquals(180d, MathUtils.atan2(-1, 0, 0) * 180d / Math.PI, 1d);
        Assertions.assertEquals(-135d, MathUtils.atan2(-1, -1, 0) * 180d / Math.PI, 1d);
        Assertions.assertEquals(-90d, MathUtils.atan2(0, -1, 0) * 180d / Math.PI, 1d);
        Assertions.assertEquals(-45d, MathUtils.atan2(1, -1, 0) * 180d / Math.PI, 1d);
    }

}
