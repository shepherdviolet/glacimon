/*
 * Copyright (C) 2015-2022 S.Violet
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

import org.junit.Assert;
import org.junit.Test;

public class MathUtilsTest {

    @Test
    public void atan2(){
        Assert.assertEquals(0d, MathUtils.atan2(0, 0, 0) * 180d / Math.PI, 1d);
        Assert.assertEquals(0d, MathUtils.atan2(1, 0, 0) * 180d / Math.PI, 1d);
        Assert.assertEquals(45d, MathUtils.atan2(1, 1, 0) * 180d / Math.PI, 1d);
        Assert.assertEquals(90d, MathUtils.atan2(0, 1, 0) * 180d / Math.PI, 1d);
        Assert.assertEquals(135d, MathUtils.atan2(-1, 1, 0) * 180d / Math.PI, 1d);
        Assert.assertEquals(180d, MathUtils.atan2(-1, 0, 0) * 180d / Math.PI, 1d);
        Assert.assertEquals(-135d, MathUtils.atan2(-1, -1, 0) * 180d / Math.PI, 1d);
        Assert.assertEquals(-90d, MathUtils.atan2(0, -1, 0) * 180d / Math.PI, 1d);
        Assert.assertEquals(-45d, MathUtils.atan2(1, -1, 0) * 180d / Math.PI, 1d);
    }

}
