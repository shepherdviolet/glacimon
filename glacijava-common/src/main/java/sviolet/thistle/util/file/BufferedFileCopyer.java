/*
 * Copyright (C) 2015-2017 S.Violet
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

package sviolet.thistle.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 文件复制器Buffer版
 *
 * @author S.Violet
 */
public class BufferedFileCopyer {

    /**
     * <p>
     * 较快的复制文件方法（可监控复制进度）<br/>
     * 目标文件的修改时间与原文件保持基本一致<br/>
     * 目标文件修改时间可能会偏大,偏差<2000ms, 例如:<br/>
     * 原始文件修改时间:****2356<br/>
     * 目标文件修改时间:****4000<br/>
     * </p>
     *
     * @param source 源文件
     * @param target 目标文件
     */
    public static void copy(File source, File target) throws IOException {
        copy(source, target, 4096);
    }

    /**
     * <p>
     * 较快的复制文件方法（可监控复制进度）<br/>
     * 目标文件的修改时间与原文件保持基本一致<br/>
     * 目标文件修改时间可能会偏大,偏差<2000ms, 例如:<br/>
     * 原始文件修改时间:****2356<br/>
     * 目标文件修改时间:****4000<br/>
     * </p>
     *
     * @param source 源文件
     * @param target 目标文件
     * @param watcher 进度回调
     */
    public static void copy(File source, File target, ProgressWatcher watcher) throws IOException {
        copy(source, target, 64 * 1024, watcher);
    }

    private static void copy(File source, File target, int buffSize) throws IOException {
        try (FileInputStream inStream = new FileInputStream(source); FileOutputStream outStream = new FileOutputStream(target)) {
            try (FileChannel in = inStream.getChannel(); FileChannel out = outStream.getChannel()){
                ByteBuffer buffer = ByteBuffer.allocate(buffSize);
                while (in.read(buffer) != -1) {
                    buffer.flip();
                    out.write(buffer);
                    buffer.clear();
                }
            }
            outStream.flush();
        }
        //使目标文件修改时间与源文件保持一致
        long lastModified = source.lastModified();
        //解决有时候取出负数, 无法设置时间的问题
        if(lastModified < 0L) {
            lastModified = 0L;
        }
        target.setLastModified(lastModified);
    }

    private static void copy(File source, File target, int buffSize, ProgressWatcher watcher) throws IOException {
        try (FileInputStream inStream = new FileInputStream(source); FileOutputStream outStream = new FileOutputStream(target)) {
            try (FileChannel in = inStream.getChannel(); FileChannel out = outStream.getChannel()){
                ByteBuffer buffer = ByteBuffer.allocate(buffSize);
                long total = source.length();
                long current = 0;
                while (in.read(buffer) != -1) {
                    buffer.flip();
                    current += buffer.limit();
                    out.write(buffer);
                    buffer.clear();
                    watcher.onUpdate(total, current);
                }
            }
            outStream.flush();
        }
        //使目标文件修改时间与源文件保持一致
        long lastModified = source.lastModified();
        //解决有时候取出负数, 无法设置时间的问题
        if(lastModified < 0L) {
            lastModified = 0L;
        }
        target.setLastModified(lastModified);
    }

    public interface ProgressWatcher {
        void onUpdate(long total, long current);
    }

}
