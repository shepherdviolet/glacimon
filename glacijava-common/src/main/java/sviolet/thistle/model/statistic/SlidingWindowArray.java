/*
 * Copyright (C) 2015-2019 S.Violet
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

package sviolet.thistle.model.statistic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>An array implements sliding window algorithm, For statistical</p>
 *
 * @author S.Violet
 */
public class SlidingWindowArray<E> {

    public static final int DEFAULT_TIME_REVERSE_THRESHOLD = 64;

    private final int bucketSize;
    private final long durationPerBucket;
    private final float floatDurationPerBucket;
    private final int timeReverseThreshold;

    private final AtomicReferenceArray<Bucket<E>> buckets;
    private final ElementOperator<E> operator;
    private final Lock lock;

    private final AtomicInteger timeReverseCount = new AtomicInteger(0);

    /**
     * @param bucketSize bucket size, > 0
     * @param durationPerBucket duration per bucket, milliseconds, >= 10 ms
     * @param operator create element when bucket creating, reset element when the bucket is expired
     */
    public SlidingWindowArray(int bucketSize, long durationPerBucket, ElementOperator<E> operator) {
        this(bucketSize, durationPerBucket, DEFAULT_TIME_REVERSE_THRESHOLD, operator);
    }

    /**
     * @param bucketSize bucket size, > 0
     * @param durationPerBucket duration per bucket, milliseconds, >= 10 ms
     * @param timeReverseThreshold reset all statistical data if time reverse exceeds the specified number of times, Set Integer.MAX_VALUE to disable reset
     * @param operator create element when bucket creating, reset element when the bucket is expired
     */
    public SlidingWindowArray(int bucketSize, long durationPerBucket, int timeReverseThreshold, ElementOperator<E> operator) {
        if (bucketSize <= 0) {
            throw new IllegalArgumentException("bucketSize must > 0");
        }
        if (durationPerBucket < 10) {
            throw new IllegalArgumentException("durationPerBucket must >= 10 ms");
        }
        if (operator == null) {
            throw new IllegalArgumentException("ElementOperator is null");
        }

        this.bucketSize = bucketSize;
        this.durationPerBucket = durationPerBucket;
        this.floatDurationPerBucket = durationPerBucket;
        this.timeReverseThreshold = timeReverseThreshold;

        this.buckets = new AtomicReferenceArray<>(bucketSize);
        this.operator = operator;
        this.lock = buildLock();
    }

    /**
     * Get the element to record current statistics
     * @param currentTime current timestamp
     * @return the statistical element of current time
     */
    public E getElement(long currentTime){
        return getByGeneration(generation(currentTime), true).element;
    }

    /**
     * <p>Get the elements of recent period for data calculation.</p>
     *
     * <p>Rough Mode: The smaller the ratio of statisticDuration and durationPerBucket (statisticDuration / durationPerBucket),
     * the greater the deviation. Therefore, we recommend using a larger ratio (statisticDuration / durationPerBucket > 10).
     * If statisticDuration / durationPerBucket = 1, it will return data of two buckets, the deviation can be up to 100%. </p>
     *
     * <p>For Example, if you want to get statistics for the last 10 seconds:
     * slidingWindowArray.getElementsRoughly(System.currentTimeMillis(), 10000L);</p>
     *
     * @param currentTime current timestamp
     * @param statisticDuration statistic duration, milliseconds, statisticDuration <= bucketSize * durationPerBucket
     * @return elements of recent period
     */
    public List<E> getElementsRoughly(long currentTime, long statisticDuration) {
        long statisticStartTime = currentTime - Math.max(statisticDuration, 0);
        int endGeneration = (int) (currentTime / durationPerBucket);
        int startGeneration = Math.max((int) (statisticStartTime / durationPerBucket), endGeneration - bucketSize + 1);

        List<E> result = new ArrayList<>(endGeneration - startGeneration + 1);
        for (int generation = startGeneration ; generation <= endGeneration ; generation++) {
            //put all
            result.add(getByGeneration(generation, false).element);
        }
        return result;
    }

    /**
     * <p>Get the elements of recent period for data calculation.</p>
     *
     * <p>Accurate Mode: Each element will have a weight value, you can multiply the statistical value by the weight,
     * to get more accurate result.</p>
     *
     * <p>For Example, if you want to get statistics for the last 10 seconds:
     * slidingWindowArray.getElementsRoughly(System.currentTimeMillis(), 10000L);</p>
     *
     * @param currentTime current timestamp
     * @param statisticDuration statistic duration, milliseconds, statisticDuration <= bucketSize * durationPerBucket
     * @return elements of recent period
     */
    public List<Element<E>> getElementsAccurately(long currentTime, long statisticDuration) {
        long statisticStartTime = currentTime - Math.max(statisticDuration, 0);
        int endGeneration = (int) (currentTime / durationPerBucket);
        int startGeneration = Math.max((int) (statisticStartTime / durationPerBucket), endGeneration - bucketSize + 1);

        List<Element<E>> result = new ArrayList<>(endGeneration - startGeneration + 1);
        for (int generation = startGeneration ; generation <= endGeneration ; generation++) {
            Bucket<E> bucket = getByGeneration(generation, false);
            //bucket info
            long bucketStartTime = bucket.startTime;
            long bucketEndTime = bucketStartTime + durationPerBucket;
            //weight calculation
            long validDuration = durationPerBucket;
            if (bucketStartTime < statisticStartTime) {
                validDuration -= statisticStartTime - bucketStartTime;
            }
            if (bucketEndTime > currentTime) {
                validDuration -= bucketEndTime - currentTime;
            }
            //element with infos
            result.add(new Element<>(
                    bucketStartTime,
                    bucketEndTime,
                    validDuration == durationPerBucket ? 1.0f : Math.max((float)validDuration / floatDurationPerBucket, 0.0f),
                    bucket.element));
        }
        return result;
    }

    /**
     * bucket size
     * @return bucket size
     */
    public int size(){
        return bucketSize;
    }

    /**
     * ReentrantLock by default
     */
    protected Lock buildLock(){
        return new ReentrantLock();
    }

    /**
     * @param generation currentTime / durationPerBucket
     */
    private Bucket<E> getByGeneration(long generation, boolean resetEnabled){
        //index of bucket
        int index = (int) (generation % bucketSize);
        if (index < 0) {
            index += bucketSize;
        }
        //bucket start time
        long startTime = generation * durationPerBucket;
        //get bucket
        Bucket<E> bucket = buckets.get(index);

        //create bucket
        if (bucket == null) {
            lock.lock();
            try {
                bucket = buckets.get(index);
                if (bucket == null) {
                    //new bucket
                    bucket = new Bucket<>(startTime, operator.reset(null));
                    buckets.set(index, bucket);
                }
            } finally {
                lock.unlock();
            }
        }

        //get element
        while (true) {
            //return new element if time reverse, Please avoid this situation !
            if (startTime < bucket.startTime) {
                //reset disabled
                if (!resetEnabled) {
                    //return dummy
                    return new Bucket<>(startTime, operator.reset(null));
                }
                //reset enabled
                if (timeReverseCount.incrementAndGet() <= timeReverseThreshold) {
                    //return dummy
                    return new Bucket<>(startTime, operator.reset(null));
                } else {
                    //reset all if time reverse exceeded threshold
                    lock.lock();
                    try {
                        for (int i = 0 ; i < bucketSize ; i++) {
                            Bucket<E> b = buckets.get(i);
                            b.element = operator.reset(b.element);
                            b.startTime = Integer.MIN_VALUE;
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            }
            //return current bucket
            if (startTime == bucket.startTime) {
                return bucket;
            }
            //clear time reverse count
            if (resetEnabled) {
                timeReverseCount.set(0);
            }
            //reset bucket and re-judge
            lock.lock();
            try {
                if (startTime > bucket.startTime) {
                    bucket.element = operator.reset(bucket.element);
                    bucket.startTime = startTime;
                }
            } finally {
                lock.unlock();
            }
        }

    }

    private long generation(long currentTime) {
        return currentTime / durationPerBucket;
    }

    private static class Bucket<E> {

        private volatile long startTime;

        private volatile E element;

        private Bucket(long startTime, E element) {
            this.startTime = startTime;
            this.element = element;
        }
    }

    /**
     * Statistical data / start time / end time
     * @param <E> Element type
     */
    public static class Element<E> {

        private final long startTime;
        private final long endTime;
        private final float weight;
        private E value;

        public Element(long startTime, long endTime, float weight, E value) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.weight = weight;
            this.value = value;
        }

        /**
         * Start time of the bucket
         */
        public long getStartTime() {
            return startTime;
        }

        /**
         * End time of the bucket
         */
        public long getEndTime() {
            return endTime;
        }

        /**
         * You can multiply the statistical value by the weight to get more accurate result
         */
        public float getWeight() {
            return weight;
        }

        /**
         * Statistical data
         */
        public E getValue() {
            return value;
        }

        public void setValue(E value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "Element{" +
                    "startTime=" + startTime +
                    ", endTime=" + endTime +
                    ", weight=" + weight +
                    ", value=" + value +
                    '}';
        }
    }

    /**
     * Create element when bucket creating, reset element when the bucket is expired
     * @param <E>
     */
    public interface ElementOperator<E> {

        /**
         * Create element when bucket creating, reset element when the bucket is expired
         *
         * @param element The old element, null if creating bucket
         * @return return new element, Or you can reuse the old one
         */
        E reset(E element);

    }

}
