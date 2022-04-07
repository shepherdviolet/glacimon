/*
 * Copyright (C) 2015-2021 S.Violet
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

import java.util.Arrays;

/**
 * Array Utils
 *
 * @author shepherdviolet
 */
public class ArrayUtils {

    private static final int DEFRAGMENT_BUFFER_SIZE = 1024;

    /**
     * Delete useless elements in the array and place the remaining elements at the left end of the array
     * @param array array
     * @param predicate determine whether the element is retained, nullable
     * @param <E> Element type
     * @return Number of remaining elements, return -1 if array is null
     */
    public static <E> int defragment(E[] array, DefragmentPredicate<E> predicate) {
        if (array == null) {
            return -1;
        }
        if (array.length <= 0) {
            return 0;
        }

        // array offset
        int arrayOffset = 0;

        // buff
        Object[] buff = new Object[Math.min(array.length, DEFRAGMENT_BUFFER_SIZE)];
        int buffOffset = 0;

        // handle array
        for (E element : array) {
            // If the element is null, discard
            if (element == null) {
                continue;
            }

            // If predicate.isRetained returns false, discard
            if (predicate != null && !predicate.isRetained(element)) {
                continue;
            }

            // put element into buff
            buff[buffOffset++] = element;

            // flush to array
            if (buffOffset >= buff.length) {
                System.arraycopy(buff, 0, array, arrayOffset, buff.length);
                arrayOffset += buff.length;
                buffOffset = 0;
            }
        }

        // flush to array
        if (buffOffset > 0) {
            System.arraycopy(buff, 0, array, arrayOffset, buffOffset);
            arrayOffset += buffOffset;
            buffOffset = 0;
        }

        // fill null
        if (arrayOffset < array.length) {
            Arrays.fill(array, arrayOffset, array.length, null);
        }

        // remaining length
        return arrayOffset;
    }

    /**
     * When defragmenting, determine whether the element is retained
     * @param <E>
     */
    public interface DefragmentPredicate<E> {

        /**
         * Determine whether the element is retained
         * @param element element
         * @return true: retain, false: discard
         */
        boolean isRetained(E element);

    }

}
