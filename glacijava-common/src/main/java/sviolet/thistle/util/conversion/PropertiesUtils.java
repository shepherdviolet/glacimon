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

import sviolet.thistle.entity.set.KeyValue;
import sviolet.thistle.entity.set.StringKeyValue;
import sviolet.thistle.util.common.CloseableUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>properties文件处理工具</p>
 *
 * <p>为保持解析标准与JDK一致, 核心解析逻辑从JDK8的java.util.Properties类中复制</p>
 *
 * @author S.Violet
 */
public class PropertiesUtils {

    /**
     * 将properties文件解析为Map, (若存在重复的key, 仅保留最后一个)
     * @param reader Reader
     * @return Map
     * @throws IOException IO错误
     */
    public static Map<String, String> loadAsMap(Reader reader) throws IOException {
        LineWritableLinkedHashMap map = new LineWritableLinkedHashMap();
        load(reader, map);
        return map;
    }

    /**
     * 将properties文件解析为Map, (若存在重复的key, 仅保留最后一个)
     * @param inStream InputStream
     * @return Map
     * @throws IOException IO错误
     */
    public static Map<String, String> loadAsMap(InputStream inStream) throws IOException {
        LineWritableLinkedHashMap map = new LineWritableLinkedHashMap();
        load(inStream, map);
        return map;
    }

    /**
     * 将properties文件解析为List, (若存在重复的key, 保留全部)
     * @param reader Reader
     * @return Map
     * @throws IOException IO错误
     */
    public static List<KeyValue<String, String>> loadAsList(Reader reader) throws IOException {
        LineWritableLinkedList list = new LineWritableLinkedList();
        load(reader, list);
        return list;
    }

    /**
     * 将properties文件解析为List, (若存在重复的key, 保留全部)
     * @param inStream InputStream
     * @return Map
     * @throws IOException IO错误
     */
    public static List<KeyValue<String, String>> loadAsList(InputStream inStream) throws IOException {
        LineWritableLinkedList list = new LineWritableLinkedList();
        load(inStream, list);
        return list;
    }

    /**
     * 解析properties文件, 将所有键值对传给LineWriter, (若存在重复的key, 保留全部)
     * @param reader Reader
     * @throws IOException IO错误
     */
    public static void load(Reader reader, LineWriter lineWriter) throws IOException {
        LineReader lineReader = null;
        try {
            lineReader = new LineReader(reader);
            load0(lineReader, lineWriter);
        } finally {
            CloseableUtils.closeQuiet(lineReader);
        }
    }

    /**
     * 解析properties文件, 将所有键值对传给LineWriter, (若存在重复的key, 保留全部)
     * @param inStream InputStream
     * @throws IOException IO错误
     */
    public static void load(InputStream inStream, LineWriter lineWriter) throws IOException {
        LineReader lineReader = null;
        try {
            lineReader = new LineReader(inStream);
            load0(lineReader, lineWriter);
        } finally {
            CloseableUtils.closeQuiet(lineReader);
        }
    }

    public interface LineWriter {

        /**
         * properties文件解析过程中, 会将所有的键值对送给该方法
         * @param key key
         * @param value value
         */
        void writeKeyValue(String key, String value);

    }

    private static class LineWritableLinkedHashMap extends LinkedHashMap<String, String> implements LineWriter {

        @Override
        public void writeKeyValue(String key, String value) {
            put(key, value);
        }

    }

    private static class LineWritableLinkedList extends LinkedList<KeyValue<String, String>> implements LineWriter {

        @Override
        public void writeKeyValue(String key, String value) {
            add(new StringKeyValue(key, value));
        }

    }

    /* ************************************************************************************************************* */
    /* The following code is copied from the JDK8: java.util.Properties */
    /* ************************************************************************************************************* */

    private static void load0(LineReader lr, LineWriter lw) throws IOException {
        char[] convtBuf = new char[1024];
        int limit;
        int keyLen;
        int valueStart;
        char c;
        boolean hasSep;
        boolean precedingBackslash;

        while ((limit = lr.readLine()) >= 0) {
            c = 0;
            keyLen = 0;
            valueStart = limit;
            hasSep = false;

            //System.out.println("line=<" + new String(lineBuf, 0, limit) + ">");
            precedingBackslash = false;
            while (keyLen < limit) {
                c = lr.lineBuf[keyLen];
                //need check if escaped.
                if ((c == '=' || c == ':') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    hasSep = true;
                    break;
                } else if ((c == ' ' || c == '\t' || c == '\f') && !precedingBackslash) {
                    valueStart = keyLen + 1;
                    break;
                }
                if (c == '\\') {
                    precedingBackslash = !precedingBackslash;
                } else {
                    precedingBackslash = false;
                }
                keyLen++;
            }
            while (valueStart < limit) {
                c = lr.lineBuf[valueStart];
                if (c != ' ' && c != '\t' && c != '\f') {
                    if (!hasSep && (c == '=' || c == ':')) {
                        hasSep = true;
                    } else {
                        break;
                    }
                }
                valueStart++;
            }
            String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
            String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf);
            lw.writeKeyValue(key, value);
        }
    }

    /* Read in a "logical line" from an InputStream/Reader, skip all comment
     * and blank lines and filter out those leading whitespace characters
     * (\u0020, \u0009 and \u000c) from the beginning of a "natural line".
     * Method returns the char length of the "logical line" and stores
     * the line in "lineBuf".
     */
    private static class LineReader implements Closeable {

        public LineReader(InputStream inStream) {
            this.inStream = inStream;
            inByteBuf = new byte[8192];
        }

        public LineReader(Reader reader) {
            this.reader = reader;
            inCharBuf = new char[8192];
        }

        /**
         * Add a close method to release resource
         */
        @Override
        public void close() throws IOException {
            CloseableUtils.closeQuiet(inStream);
            CloseableUtils.closeQuiet(reader);
        }

        byte[] inByteBuf;
        char[] inCharBuf;
        char[] lineBuf = new char[1024];
        int inLimit = 0;
        int inOff = 0;
        InputStream inStream;
        Reader reader;

        int readLine() throws IOException {
            int len = 0;
            char c = 0;

            boolean skipWhiteSpace = true;
            boolean isCommentLine = false;
            boolean isNewLine = true;
            boolean appendedLineBegin = false;
            boolean precedingBackslash = false;
            boolean skipLF = false;

            while (true) {
                if (inOff >= inLimit) {
                    inLimit = (inStream == null) ? reader.read(inCharBuf)
                            : inStream.read(inByteBuf);
                    inOff = 0;
                    if (inLimit <= 0) {
                        if (len == 0 || isCommentLine) {
                            return -1;
                        }
                        if (precedingBackslash) {
                            len--;
                        }
                        return len;
                    }
                }
                if (inStream != null) {
                    //The line below is equivalent to calling a
                    //ISO8859-1 decoder.
                    c = (char) (0xff & inByteBuf[inOff++]);
                } else {
                    c = inCharBuf[inOff++];
                }
                if (skipLF) {
                    skipLF = false;
                    if (c == '\n') {
                        continue;
                    }
                }
                if (skipWhiteSpace) {
                    if (c == ' ' || c == '\t' || c == '\f') {
                        continue;
                    }
                    if (!appendedLineBegin && (c == '\r' || c == '\n')) {
                        continue;
                    }
                    skipWhiteSpace = false;
                    appendedLineBegin = false;
                }
                if (isNewLine) {
                    isNewLine = false;
                    if (c == '#' || c == '!') {
                        isCommentLine = true;
                        continue;
                    }
                }

                if (c != '\n' && c != '\r') {
                    lineBuf[len++] = c;
                    if (len == lineBuf.length) {
                        int newLength = lineBuf.length * 2;
                        if (newLength < 0) {
                            newLength = Integer.MAX_VALUE;
                        }
                        char[] buf = new char[newLength];
                        System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
                        lineBuf = buf;
                    }
                    //flip the preceding backslash flag
                    if (c == '\\') {
                        precedingBackslash = !precedingBackslash;
                    } else {
                        precedingBackslash = false;
                    }
                } else {
                    // reached EOL
                    if (isCommentLine || len == 0) {
                        isCommentLine = false;
                        isNewLine = true;
                        skipWhiteSpace = true;
                        len = 0;
                        continue;
                    }
                    if (inOff >= inLimit) {
                        inLimit = (inStream == null)
                                ? reader.read(inCharBuf)
                                : inStream.read(inByteBuf);
                        inOff = 0;
                        if (inLimit <= 0) {
                            if (precedingBackslash) {
                                len--;
                            }
                            return len;
                        }
                    }
                    if (precedingBackslash) {
                        len -= 1;
                        //skip the leading whitespace characters in following line
                        skipWhiteSpace = true;
                        appendedLineBegin = true;
                        precedingBackslash = false;
                        if (c == '\r') {
                            skipLF = true;
                        }
                    } else {
                        return len;
                    }
                }
            }
        }
    }

    /*
     * Converts encoded &#92;uxxxx to unicode chars
     * and changes special saved chars to their original forms
     */
    private static String loadConvert(char[] in, int off, int len, char[] convtBuf) {
        if (convtBuf.length < len) {
            int newLen = len * 2;
            if (newLen < 0) {
                newLen = Integer.MAX_VALUE;
            }
            convtBuf = new char[newLen];
        }
        char aChar;
        char[] out = convtBuf;
        int outLen = 0;
        int end = off + len;

        while (off < end) {
            aChar = in[off++];
            if (aChar == '\\') {
                aChar = in[off++];
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = in[off++];
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException(
                                        "Malformed \\uxxxx encoding.");
                        }
                    }
                    out[outLen++] = (char) value;
                } else {
                    if (aChar == 't') { aChar = '\t'; }
                    else if (aChar == 'r') { aChar = '\r'; }
                    else if (aChar == 'n') { aChar = '\n'; }
                    else if (aChar == 'f') { aChar = '\f'; }
                    out[outLen++] = aChar;
                }
            } else {
                out[outLen++] = aChar;
            }
        }
        return new String(out, 0, outLen);
    }

}
