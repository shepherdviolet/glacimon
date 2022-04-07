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

import sviolet.thistle.util.common.CloseableUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 文件工具
 *
 * @author S.Violet
 */
public class FileUtils {

    /**
     * 向文件写入字节数据(仅限少量数据)
     * @param file 目标文件
     * @param bytes 数据
     * @param append true:追加 false:覆盖
     */
    public static void writeBytes(File file, byte[] bytes, boolean append) throws IOException {
        File dirFile = file.getParentFile();
        if (dirFile != null && !dirFile.exists()){
            if (!dirFile.mkdirs()){
                throw new IOException("Can not make directory before write bytes to file, path:" + dirFile.getAbsolutePath());
            }
        }
        // About suppressed warnings: FileOutputStream will be closed by BufferedWriter
        try (OutputStream outputStream = new FileOutputStream(file, append)) {
            outputStream.write(bytes);
            outputStream.flush();
        }
    }

    /**
     * 向文件写入字符串
     * @param file 文件
     * @param msg 字符串
     * @param append true:追加 false:覆盖
     */
    public static void writeString(File file, String msg, boolean append) throws IOException {
        writeString(file, msg, "utf-8", append);
    }

    /**
     * 向文件写入字符串
     * @param file 文件
     * @param msg 字符串
     * @param charset 字符编码
     * @param append true:追加 false:覆盖
     */
    @SuppressWarnings({"lgtm[java/output-resource-leak]"})
    public static void writeString(File file, String msg, String charset, boolean append) throws IOException {
        File dirFile = file.getParentFile();
        if (dirFile != null && !dirFile.exists()){
            if (!dirFile.mkdirs()){
                throw new IOException("Can not make directory before write string to file, path:" + dirFile.getAbsolutePath());
            }
        }
        // About suppressed warnings: FileOutputStream will be closed by BufferedWriter
        try (OutputStream outputStream = new FileOutputStream(file, append);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset))) {
            writer.write(msg);
            writer.flush();
        }
    }

    /**
     * 读取整个文件(字符串形式), 仅限于读取小文件, 小心OOM
     * @param file 文件
     * @param maxLength 最大长度, 如果文件大小超过该设定值, 会抛出异常
     * @return 字符串
     */
    public static String readString(File file, int maxLength) throws LengthOutOfLimitException, IOException {
        return readString(file, "utf-8", maxLength);
    }

    /**
     * 读取整个文件(字符串形式), 仅限于读取小文件, 小心OOM
     * @param file 文件
     * @param charset 字符编码
     * @param maxLength 最大长度, 如果文件大小超过该设定值, 会抛出异常
     * @return 字符串
     */
    @SuppressWarnings({"lgtm[java/input-resource-leak]"})
    public static String readString(File file, String charset, int maxLength) throws LengthOutOfLimitException, IOException {

        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("File not found, path:" + file.getAbsolutePath());
        }

        if (file.length() > maxLength){
            throw new LengthOutOfLimitException("File length out of limit, file:" + file.getAbsolutePath() + ", length:" + file.length());
        }

        // About suppressed warnings: FileInputStream will be closed by BufferedReader
        try (InputStream inputStream = new FileInputStream(file);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, charset))) {
            StringBuilder stringBuilder = new StringBuilder();
            char[] buff = new char[1024];
            int length;
            while ((length = bufferedReader.read(buff)) >= 0) {
                stringBuilder.append(buff, 0, length);
            }
            return stringBuilder.toString();
        }
    }

    /**
     * 读取文件的头部数据
     * @param file 文件
     * @param start 起始位置
     * @param buffer 获取的数据
     * @throws IOException exception
     */
    public static long readHead(File file, int start, byte[] buffer) throws IOException {
        if (file == null || !file.exists()){
            return 0;
        }
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            randomAccessFile.seek(start);
            return randomAccessFile.read(buffer);
        }
    }


    /**
     * NIO方式遍历文件的每一行数据, 内容不含换行符, 而且会忽略空行
     *
     * @param file 文件, 不存在会抛出异常
     * @param limitPerLine 一行的长度限制(字节), 超出时这行只有前半部分数据(不完整), 设置0不限长度(小心OOM), 建议设置较大值, 例如: 1024 * 1024
     * @param expectedLineLength 预计每行的长度(字节), 例如: 1024
     * @param bufferSize 读取数据用的缓存大小(字节), 例如: 4096
     * @param consumer 行处理器, 如果有一行数据长度超过限制, 数据截取前半部分, 不完整
     */
    public static void readLines(File file, int limitPerLine, int expectedLineLength, int bufferSize, LineConsumer consumer) throws IOException {
        if (!file.exists() || !file.isFile()) {
            throw new FileNotFoundException("File not found, path:" + file.getAbsolutePath());
        }

        byte[] buff = new byte[bufferSize];
        int buffLength;
        int buffStartIndex;
        boolean outOfLengthFlag = false;

        ByteArrayOutputStream lineData = new ByteArrayOutputStream(expectedLineLength);

        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r")) {
            while ((buffLength = randomAccessFile.read(buff)) > -1) {
                buffStartIndex = 0;

                //handle every byte in the buff
                for (int buffCurrentIndex = 0 ; buffCurrentIndex < buffLength ; buffCurrentIndex++) {
                    byte b = buff[buffCurrentIndex];

                    //handle the end of the line
                    if (b == '\n' || b == '\r') {

                        //reset outOfLengthFlag flag if reach the end of the line
                        if (outOfLengthFlag) {
                            //skip bytes which has out of limit
                            buffStartIndex = buffCurrentIndex + 1;
                            //mark outOfLengthFlag to false
                            outOfLengthFlag = false;
                            //skip write & consume
                            continue;
                        }

                        //write to line data
                        boolean outOfLimit = readLines_writeLine(buff, lineData, buffStartIndex, buffCurrentIndex, limitPerLine);
                        //consume
                        if (!readLines_consume(consumer, lineData, outOfLimit)) {return;}
                        //set start index to next byte
                        buffStartIndex = buffCurrentIndex + 1;

                    }
                }

                //reach the end of the buff
                //write to line data
                boolean outOfLimit = readLines_writeLine(buff, lineData, buffStartIndex, buffLength, limitPerLine);
                //out of limit
                if (outOfLimit) {
                    //consume
                    if (!readLines_consume(consumer, lineData, true)) {return;}
                    //mark outOfLengthFlag to true
                    outOfLengthFlag = true;
                }
            }

            //handle last line
            readLines_consume(consumer, lineData, false);
        }
    }

    private static boolean readLines_writeLine(byte[] buff, ByteArrayOutputStream lineData, int buffStartIndex, int buffCurrentIndex, int limitPerLine) {
        //buff -> lineData
        int writeLength = buffCurrentIndex - buffStartIndex;
        boolean outOfLimit = false;
        if (limitPerLine > 0 && writeLength > 0 &&
                writeLength + lineData.size() > limitPerLine) {
            writeLength = limitPerLine - lineData.size();
            outOfLimit = true;
        }
        if (writeLength > 0) {
            lineData.write(buff, buffStartIndex, writeLength);
        }
        return outOfLimit;
    }

    private static boolean readLines_consume(LineConsumer consumer, ByteArrayOutputStream lineData, boolean outOfLimit) {
        if (lineData.size() <= 0) {
            return true;
        }
        boolean isContinue = consumer.consume(lineData.toByteArray(), outOfLimit);
        lineData.reset();
        return isContinue;
    }

    /**
     * 判断当前运行环境是否能手动回收MappedByteBuffer, 无法手动回收的运行时建议不要随便使用MappedByteBuffer, 改用传统的IO.
     * @return true:支持手动回收MappedByteBuffer
     */
    public static boolean isMappedByteBufferCanClean(){
        return CloseableUtils.isMappedByteBufferCanClean();
    }

    /**
     * 手动回收MappedByteBuffer.
     * @return true:回收成功
     */
    public static boolean cleanMappedByteBuffer(ByteBuffer byteBuffer) {
        return CloseableUtils.cleanMappedByteBuffer(byteBuffer);
    }

    /**
     * 递归列出目录下所有的文件(不含目录, 包含子目录下的文件)
     * @param directory 目录 (如果是文件就返回该文件)
     * @return 目录下所有的文件(不含目录, 包含子目录下的文件)
     */
    public static List<File> listAllFilesRecursively(File directory) {
        if (directory == null || !directory.exists()) {
            return Collections.emptyList();
        }
        if (!directory.isDirectory()) {
            return Collections.singletonList(directory);
        }
        List<File> list = new ArrayList<>();
        listAllFilesRecursively_inner(directory, list);
        return list;
    }

    private static void listAllFilesRecursively_inner(File directory, List<File> list) {
        File[] subFiles = directory.listFiles();
        if (subFiles == null) {
            return;
        }
        for (File subFile : subFiles) {
            if (subFile.isFile()) {
                list.add(subFile);
            } else if (subFile.isDirectory()) {
                listAllFilesRecursively_inner(subFile, list);
            }
        }

    }

    /****************************************************************************************************
     * classes
     */

    /**
     * 文件长度超过限制
     */
    public static class LengthOutOfLimitException extends Exception {

        private static final long serialVersionUID = -28200503942475448L;

        public LengthOutOfLimitException(String message) {
            super(message);
        }

    }

    /**
     * 行处理器
     */
    public interface LineConsumer {
        /**
         * 处理每一行数据, 例如: 转成String
         * @param line 一行数据, 不含换行符
         * @param outOfLimit false: 正常 true: 当前行长度超过限制, 数据截取前半部分, 不完整
         * @return true: 继续处理 false: 终止处理
         */
        boolean consume(byte[] line, boolean outOfLimit);
    }

}
