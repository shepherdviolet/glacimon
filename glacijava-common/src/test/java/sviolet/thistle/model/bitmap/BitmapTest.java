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

package sviolet.thistle.model.bitmap;

import org.junit.Assert;
import org.junit.Test;
import sviolet.thistle.util.conversion.ByteUtils;
import sviolet.thistle.util.crypto.DigestCipher;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class BitmapTest {

    @Test
    public void test(){
        test0(new HeapBitmap(1024));
        test0(new DirectBitmap(1024));
        test0(new ConcurrentHeapBitmap(1024));
        test0(new SyncHeapBitmap(1024));
    }

    private void test0(Bitmap bitmap) {
        bitmap.put(999, true);
        bitmap.put(1000, true);
        bitmap.put(1001, true);
        bitmap.put(1002, true);
        bitmap.put(1003, true);
        bitmap.put(1004, true);
        bitmap.put(1005, true);
        bitmap.put(1006, true);
        bitmap.put(1007, true);
        bitmap.put(1008, true);
        Assert.assertTrue(bitmap.get(999));
        Assert.assertTrue(bitmap.get(1000));
        Assert.assertTrue(bitmap.get(1001));
        Assert.assertTrue(bitmap.get(1002));
        Assert.assertTrue(bitmap.get(1003));
        Assert.assertTrue(bitmap.get(1004));
        Assert.assertTrue(bitmap.get(1005));
        Assert.assertTrue(bitmap.get(1006));
        Assert.assertTrue(bitmap.get(1007));
        Assert.assertTrue(bitmap.get(1008));

        bitmap.put(998, true);
        bitmap.put(999, false);
        bitmap.put(1000, true);
        Assert.assertFalse(bitmap.get(999));

        bitmap.put(0, true);
        Assert.assertTrue(bitmap.get(0));
        bitmap.put(1023, true);
        Assert.assertTrue(bitmap.get(1023));

        byte[] data = bitmap.extractAll();
        String dataString = ByteUtils.bytesToHex(data);

        bitmap = new HeapBitmap(data);
        Assert.assertFalse(bitmap.get(999));
        Assert.assertTrue(bitmap.get(0));
        Assert.assertTrue(bitmap.get(1023));
        Assert.assertTrue(bitmap.get(1001));
        Assert.assertTrue(bitmap.get(1002));
        Assert.assertTrue(bitmap.get(1003));
        Assert.assertTrue(bitmap.get(1004));
        Assert.assertTrue(bitmap.get(1005));
        Assert.assertTrue(bitmap.get(1006));
        Assert.assertTrue(bitmap.get(1007));
        Assert.assertTrue(bitmap.get(1008));

        String dataString2 = ByteUtils.bytesToHex(bitmap.extractAll());
        Assert.assertEquals(dataString, dataString2);
//        System.out.println(dataString2);
    }

    @Test
    public void computeTest(){
        byte[] data1 = ByteUtils.hexToBytes("0123456789abcdef6573543543548adcefaadccdef");
        byte[] data2 = ByteUtils.hexToBytes("3216549870fedcbaadcedcdbcdef54685465354354");
        byte[] expected = new byte[data1.length];
        for (int i = 0 ; i < data1.length ; i++) {
            expected[i] = (byte) (data1[i] ^ data2[i]);
        }
//        System.out.println(ByteUtils.bytesToHex(expected));
        computeTest0(new HeapBitmap(data1), new HeapBitmap(data2), expected);
        computeTest0(new DirectBitmap(data1), new DirectBitmap(data2), expected);
        computeTest0(new SyncHeapBitmap(data1), new SyncHeapBitmap(data2), expected);
        computeTest0(new ConcurrentHeapBitmap(data1), new ConcurrentHeapBitmap(data2), expected);
    }

    private void computeTest0(Bitmap bitmap1, Bitmap bitmap2, byte[] expected) {
        Bitmap result = new HeapBitmap(bitmap1.size());
        bitmap1.computeWith(bitmap2, result, Bitmap.ComputeFunction.XOR);
//        System.out.println(ByteUtils.bytesToHex(result.extractAll()));
        Assert.assertArrayEquals(expected, result.extractAll());
    }

    /* *********************************************************************************************************** */


    public static void main(String[] args) {
//        baseline1();//性能
//        baseline2();//误判率
        consistency();//一致性
    }

    private static final int BITMAP_SIZE = 1000000000;
    private static final int TIMES = 10000000;
    private static final Random random = new Random();

    /**
     * 性能
     * HeapBitmap 1799
     * DirectBitmap 1900
     * ConcurrentHeapBitmap 2583
     * SyncHeapBitmap 3182
     */
    private static void baseline1() {
        Bitmap bitmap = new HeapBitmap(BITMAP_SIZE);
//        Bitmap bitmap = new DirectBitmap(BITMAP_SIZE);
//        Bitmap bitmap = new ConcurrentHeapBitmap(BITMAP_SIZE);
//        Bitmap bitmap = new SyncHeapBitmap(BITMAP_SIZE);

        long time = System.currentTimeMillis();

        for (int i = 0 ; i < TIMES ; i++) {
            bitmap.put(random.nextInt(BITMAP_SIZE), i % 2 == 0);
        }

        boolean result = false;
        for (int i = 0 ; i < TIMES ; i++) {
            result = bitmap.get(random.nextInt(BITMAP_SIZE));
        }

        System.out.println(System.currentTimeMillis() - time);
        System.out.println(result);
    }

    private static final int BITMAP_SIZE_2 = 1000000000;//10亿
//    private static final int BITMAP_SIZE_2 = 100000000;//1亿
//    private static final int BITMAP_SIZE_2 = 10000000;//1000万

    private static final int TIMES_2 = 10000000;//1000万
//    private static final int TIMES_2 = 1000000;//100万

    /**
     * 误判率
     *
     * 10亿 1000万 72次 86次 约0.00001
     * 1亿 1000万 48200次 48111次 约0.005
     * 1亿 100万 7次 2次 约0.00001
     * 1000万 100万 4673次 4862次 约0.005
     */
    private static void baseline2(){
        BloomBitmap bitmap = new HeapBitmap(BITMAP_SIZE_2);
//        BloomBitmap bitmap = new DirectBitmap(BITMAP_SIZE_2);
//        BloomBitmap bitmap = new ConcurrentHeapBitmap(BITMAP_SIZE_2);
//        BloomBitmap bitmap = new SyncHeapBitmap(BITMAP_SIZE_2);
        int collisions = 0;

        for (int i = 0 ; i < TIMES_2 ; i++) {
            byte[] uuid = UUID.randomUUID().toString().getBytes();
            if (bitmap.bloomContains(uuid)) {
                collisions++;
            }
            bitmap.bloomAdd(uuid);
        }

        System.out.println(collisions);
    }

    private static final long THREADS = 16;

    /**
     * 一致性测试
     */
    private static void consistency(){
        // 单线程给HeapBitmap赋值
        byte[] correct = consistencySync(new HeapBitmap(1000000));
        String correctData = ByteUtils.bytesToHex(correct);
        String correctHash = ByteUtils.bytesToHex(DigestCipher.digest(correct, DigestCipher.TYPE_SHA1));
        System.out.println(correctData);
        System.out.println("标准值:" + correctHash);

        // 多线程给HeapBitmap赋值, 这个大概率会和标准值不同
        byte[] data1 = consistencyAsync(new HeapBitmap(1000000));
        String hash1 = ByteUtils.bytesToHex(DigestCipher.digest(data1, DigestCipher.TYPE_SHA1));
        System.out.println("可能不同" + hash1);

        // 多线程给ConcurrentHeapBitmap赋值
        byte[] data2 = consistencyAsync(new ConcurrentHeapBitmap(1000000));
        String hash2 = ByteUtils.bytesToHex(DigestCipher.digest(data2, DigestCipher.TYPE_SHA1));
        System.out.println("必须相同" + hash2);

        // 多线程给SyncHeapBitmap赋值
        byte[] data3 = consistencyAsync(new SyncHeapBitmap(1000000));
        String hash3 = ByteUtils.bytesToHex(DigestCipher.digest(data3, DigestCipher.TYPE_SHA1));
        System.out.println("必须相同" + hash3);

        // 单线程给DirectBitmap赋值
        byte[] data4 = consistencySync(new DirectBitmap(1000000));
        String hash4 = ByteUtils.bytesToHex(DigestCipher.digest(data4, DigestCipher.TYPE_SHA1));
        System.out.println("必须相同" + hash4);
    }

    private static byte[] consistencySync(final BloomBitmap bitmap){
        for (int i = 0 ; i < THREADS ; i++) {
            consistencyTask(bitmap, i);
        }
        return bitmap.extractAll();
    }

    private static byte[] consistencyAsync(final BloomBitmap bitmap){
        final AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0 ; i < THREADS ; i++) {
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException ignored) {
                    }
                    consistencyTask(bitmap, finalI);
                    counter.incrementAndGet();
                }
            }).start();
        }
        while (counter.get() < THREADS) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {
            }
        }
        return bitmap.extractAll();
    }

    private static void consistencyTask(BloomBitmap bitmap, int offset) {
        for (int i = 0 ; i < 10000 ; i++) {
            bitmap.bloomAdd(String.valueOf(i * THREADS + offset).getBytes());
        }
    }

}
