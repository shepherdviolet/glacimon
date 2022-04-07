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

package sviolet.thistle.util.judge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sviolet.thistle.util.conversion.ByteUtils;

/**
 * 数据类型判断工具
 *
 * @author S.Violet
 */
public class DataTypeUtils {

    /**
     * 最大截取文件头长度
     */
    private static final int MAX_HEADER_LENGTH = 20;

    /**
     * 映射表
     */
    private static final Map<String, Type> TYPES_MAPPING = new HashMap<>();

    /**
     * 数据类型
     */
    public enum Type {

        /**
         * JPG
         */
        JPG,

        /**
         * PNG
         */
        PNG,

        /**
         * GIF
         */
        GIF,

        /**
         * BMP
         */
        BMP,

        /**
         * TIF
         */
        TIF,

        /**
         * DWG
         */
        DWG,

        /**
         * PSD
         */
        PSD,

        /**
         * RTF
         */
        RTF,

        /**
         * XML
         */
        XML,

        /**
         * HTML
         */
        HTML,

        /**
         * EMAIL
         */
        EMAIL,

        /**
         * DOC
         */
        DOC,

        /**
         * MDB
         */
        MDB,

        /**
         * PS
         */
        PS,

        /**
         * PDF
         */
        PDF,

        /**
         * ZIP
         */
        ZIP,

        /**
         * RAR
         */
        RAR,

        /**
         * WAV
         */
        WAV,

        /**
         * AVI
         */
        AVI,

        /**
         * RM
         */
        RM,

        /**
         * MPG
         */
        MPG,

        /**
         * MOV
         */
        MOV,

        /**
         * ASF
         */
        ASF,

        /**
         * MID
         */
        MID,

        /**
         * GZ
         */
        GZ,

        /**
         * 未知类型
         */
        UNKNOWN,

        /**
         * 文件或字节流为空
         */
        NULL
    }

    /*
     * 填充文件头-文件类型映射表
     */
    static {
        TYPES_MAPPING.put("ffd8ff", Type.JPG);
        TYPES_MAPPING.put("89504e47", Type.PNG);
        TYPES_MAPPING.put("47494638", Type.GIF);
        TYPES_MAPPING.put("49492a00", Type.TIF);
        TYPES_MAPPING.put("424d", Type.BMP);
        TYPES_MAPPING.put("41433130", Type.DWG);
        TYPES_MAPPING.put("38425053", Type.PSD);
        TYPES_MAPPING.put("7b5c727466", Type.RTF);
        TYPES_MAPPING.put("3c3f786d6c", Type.XML);
        TYPES_MAPPING.put("68746d6c3e", Type.HTML);
        TYPES_MAPPING.put("44656c69766572792d646174653a", Type.EMAIL);
        TYPES_MAPPING.put("d0cf11e0", Type.DOC);
        TYPES_MAPPING.put("5374616e64617264204a", Type.MDB);
        TYPES_MAPPING.put("252150532d41646f6265", Type.PS);
        TYPES_MAPPING.put("255044462d312e", Type.PDF);
        TYPES_MAPPING.put("504b0304", Type.ZIP);
        TYPES_MAPPING.put("52617221", Type.RAR);
        TYPES_MAPPING.put("57415645", Type.WAV);
        TYPES_MAPPING.put("41564920", Type.AVI);
        TYPES_MAPPING.put("2e524d46", Type.RM);
        TYPES_MAPPING.put("000001ba", Type.MPG);
        TYPES_MAPPING.put("000001b3", Type.MPG);
        TYPES_MAPPING.put("6d6f6f76", Type.MOV);
        TYPES_MAPPING.put("3026b2758e66cf11", Type.ASF);
        TYPES_MAPPING.put("4d546864", Type.MID);
        TYPES_MAPPING.put("1f8b08", Type.GZ);
    }

    /**
     * 判断文件数据类型
     * @param file 文件
     * @return 类型
     */
    public static Type getFileType(File file){
        if (file == null || !file.exists()){
            return Type.NULL;
        }
        String fileHeader = getFileHeader(file);
        if (fileHeader == null){
            return Type.NULL;
        }
        for (Map.Entry<String, Type> entry : TYPES_MAPPING.entrySet()){
            if (fileHeader.startsWith(entry.getKey())){
                return entry.getValue();
            }
        }
        return Type.UNKNOWN;
    }

    private static String getFileHeader(File file){
        FileInputStream inputStream = null;
        try{
            long fileLength = file.length();
            if (fileLength <= 0){
                return null;
            }
            int headerLength;
            if (fileLength > MAX_HEADER_LENGTH){
                headerLength = MAX_HEADER_LENGTH;
            }else{
                headerLength = (int) fileLength;
            }
            byte[] buffer = new byte[headerLength];
            inputStream = new FileInputStream(file);
            inputStream.read(buffer);
            return ByteUtils.bytesToHex(buffer);
        }catch(Exception ignored){
        }finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    /**
     * 判断bytes数据类型
     * @param bytes bytes
     * @return 类型
     */
    public static Type getBytesType(byte[] bytes){
        String header = getBytesHeader(bytes);
        if (header == null){
            return Type.NULL;
        }
        for (Map.Entry<String, Type> entry : TYPES_MAPPING.entrySet()){
            if (header.startsWith(entry.getKey())){
                return entry.getValue();
            }
        }
        return Type.UNKNOWN;
    }

    private static String getBytesHeader(byte[] bytes){
        if (bytes == null || bytes.length <= 0){
            return null;
        }
        int headerLength;
        if (bytes.length > MAX_HEADER_LENGTH){
            headerLength = MAX_HEADER_LENGTH;
        }else{
            headerLength = bytes.length;
        }
        byte[] buffer = new byte[headerLength];
        System.arraycopy(bytes, 0, buffer, 0, headerLength);
        return ByteUtils.bytesToHex(buffer);
    }

}
