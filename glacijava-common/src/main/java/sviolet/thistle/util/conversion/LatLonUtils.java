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

package sviolet.thistle.util.conversion;

import sviolet.thistle.util.math.MathUtils;

/**
 * 经纬度工具
 * @author S.Violet
 */

public class LatLonUtils {

    private static final double EARTH_RADIUS = 6378.137;

    /**
     * 根据两点经纬度计算距离
     *
     * @param lat1 纬度1
     * @param lng1 经度1
     * @param lat2 纬度2
     * @param lng2 经度2
     * @return 距离(米)
     */
    public static double getDistance(double lat1, double lng1, double lat2,
                                     double lng2) {
        double lat1Rad = MathUtils.rad(lat1);
        double lat2Rad = MathUtils.rad(lat2);
        double latRadOffset = lat1Rad - lat2Rad;
        double lngRadOffset = MathUtils.rad(lng1) - MathUtils.rad(lng2);
        double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(latRadOffset / 2), 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.pow(Math.sin(lngRadOffset / 2), 2)));
        distance = distance * EARTH_RADIUS;
        distance = Math.round(distance * 10000d) / 10000d;
        return distance * 1000;
    }

}
