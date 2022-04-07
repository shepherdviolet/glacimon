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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.x.monitor.txtimer.def;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sviolet.thistle.util.file.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * [非线程安全 | Not Thread Safe] TxTimer报告扫描解析器
 *
 * <p>建议TxTimer设置slate.txtimer.report.printpermin=true, 可以打印更精确的数据</p>
 *
 * <p>本类是设计用来进行简单地离线地统计数据分析的, 如果写成线程安全的, 会增加代码的复杂度, 同时降低单线程执行时的效率.
 * 所以在使用时, 请在单线程中创建该类, 然后读取, 最后获取结果. 本类提供了一个shutdown方法, 这是唯一一个可以在其他线程
 * 调用的方法, 但是这个方法并不会马上让读取停止, 读取是否停止以读取线程是否执行完或抛异常为准. </p>
 *
 * <p>简单示例:</p>
 *
 * <pre>
 *  Map<String, Map<String, TreeMap<Long, Map<String, DefaultTxTimerReportRepository.DefaultData>>>> data = new DefaultTxTimerReportScanner()
 *      .read("./out/test-case/txtimer/", ".*\\.stat$", true, StandardCharsets.UTF_8)
 *      .getResultData();
 * </pre>
 *
 * @author S.Violet
 */
public class DefaultTxTimerReportScanner {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String currentPath;
    private int currentLineNum;
    private volatile boolean reading = false;
    private boolean shutdown = false;

    private DefaultTxTimerReportParser reportParser = new DefaultTxTimerReportParser.DefaultParser();
    private DefaultTxTimerReportRepository reportRepository = new DefaultTxTimerReportRepository.DefaultRepository();
    private ProgressListener progressListener;

    /**
     * 自定义解析器
     */
    public DefaultTxTimerReportScanner setReportParser(DefaultTxTimerReportParser reportParser) {
        this.reportParser = reportParser;
        return this;
    }

    /**
     * 自定义数据仓库
     */
    public DefaultTxTimerReportScanner setReportRepository(DefaultTxTimerReportRepository reportRepository) {
        this.reportRepository = reportRepository;
        return this;
    }

    /**
     * 设置进度监听器
     */
    public DefaultTxTimerReportScanner setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    /**
     * 获取仓库实例
     */
    @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
    public <T extends DefaultTxTimerReportRepository> T getReportRepository() {
        T result = (T) this.reportRepository;
        return result;
    }

    /**
     * 直接获取仓库中的数据
     *
     * @return 返回类型取决于DefaultTxTimerReportRepository, 默认类型是: Map<String, Map<String, TreeMap<Long, Map<String, DefaultData>>>>
     */
    @SuppressWarnings({"unchecked", "UnnecessaryLocalVariable"})
    public <T> T getResultData(){
        T result = (T) this.reportRepository.getData();
        return result;
    }

    /**
     * 终止读取, 调用该方法后, 正在进行的读取进程会在一定时间后结束, 另外, 当前实例将无法重新读取新的文件(请重新创建实例).
     * 这是唯一一个可以在其他线程调用的方法. 正确的用法是, 在读取线程中, 创建实例, 读取文件, 获取结果, 程序应保证实例不被
     * 其他线程操作(除了这个方法外), 当需要中止读取时, 可以在其他线程中调用该方法, 最终读取是否结束, 要以读取线程那边是否
     * 执行完为准.
     */
    public DefaultTxTimerReportScanner shutdown(){
        shutdown = true;
        return this;
    }

    /**
     * 是否被终止, 该状态不能体现读取是否结束, 只能体现shutdown()是否被调用过! 读取是否停止以读取线程是否执行完或抛异常为准!
     */
    public boolean isShutdown(){
        return shutdown;
    }

    /**
     * [非线程安全 | Not Thread Safe] 从一个文件读取数据
     * @param file 数据源(文件)
     * @param charset 字符集
     */
    public DefaultTxTimerReportScanner read(File file, Charset charset) throws IOException {
        //简单的防多线程操作检查
        if (reading) {
            throw new RuntimeException("This class is not thread safe, Do not invoke read in multiple threads at the same time");
        }
        reading = true;
        try {
            read0(file, charset);
        } finally {
            reading = false;
        }
        return this;
    }

    /**
     * [非线程安全 | Not Thread Safe] 从一个目录读取数据
     * @param directory 目录
     * @param namePattern 文件名的匹配正则表达式
     * @param recursive 是否递归处理子目录
     * @param charset 字符集
     */
    public DefaultTxTimerReportScanner read(String directory, String namePattern, boolean recursive, Charset charset) {
        return read(directory, Pattern.compile(namePattern), recursive, charset);
    }

    /**
     * [非线程安全 | Not Thread Safe] 从一个目录读取数据
     * @param directory 目录
     * @param namePattern 文件名的匹配正则表达式
     * @param recursive 是否递归处理子目录
     * @param charset 字符集
     */
    public DefaultTxTimerReportScanner read(String directory, Pattern namePattern, boolean recursive, Charset charset) {
        //简单的防多线程操作检查
        if (reading) {
            throw new RuntimeException("This class is not thread safe, Do not invoke read in multiple threads at the same time");
        }
        reading = true;
        try {
            read0(directory, namePattern, recursive, charset);
        } finally {
            reading = false;
        }
        return this;
    }

    private void read0(File file, Charset charset) throws IOException {
        if (shutdown) {
            return;
        }
        if (file == null || !file.exists() || !file.isFile()) {
            logger.warn("File '" + file + "' is null / not exists / not a file");
            return;
        }
        if (charset == null) {
            logger.warn("Charset is null");
            return;
        }

        //当前处理的文件路径
        currentPath = file.getAbsolutePath();
        currentLineNum = 0;

        if (logger.isDebugEnabled()) {
            logger.debug("Reading file " + currentPath);
        }

        final ProgressListener progressListener = this.progressListener;
        if (progressListener != null) {
            progressListener.enterFile(currentPath);
        }

        FileUtils.readLines(file, 1024 * 4, 1024 * 4, 1024 * 64, (data, outOfLimit) -> {
            currentLineNum++;

            //忽略过长的行
            if (outOfLimit) {
                return !shutdown;
            }

            String line = new String(data, charset);

            //解析
            DefaultTxTimerReportParser.RawData rawData = null;
            try {
                rawData = reportParser.parseLine(line);
            } catch (Exception e) {
                logger.warn("Error while parsing line, lineNum:" + currentLineNum + ", path:" + currentPath + ", string:" + line, e);
            }

            if (rawData == null) {
                return !shutdown;
            }

            //入库
            try {
                reportRepository.add(rawData);
            } catch (Exception e) {
                logger.warn("Error while add data to repository, lineNum:" + currentLineNum + ", path:" + currentPath + ", rawData:" + rawData, e);
            }

            if (progressListener != null) {
                progressListener.exitLine(currentPath, currentLineNum, line);
            }

            //如果被终止, 就停止读取
            return !shutdown;
        });

        if (progressListener != null) {
            progressListener.exitFile(currentPath);
        }
    }

    private void read0(String directory, Pattern namePattern, boolean recursive, Charset charset) {
        if (shutdown) {
            return;
        }
        if (directory == null) {
            logger.warn("Directory is null");
            return;
        }
        if (namePattern == null) {
            logger.warn("NamePattern is null");
            return;
        }
        if (charset == null) {
            logger.warn("Charset is null");
            return;
        }

        File directoryFile = new File(directory);
        if (!directoryFile.exists() || !directoryFile.isDirectory()) {
            logger.warn("Directory '" + directoryFile + "' is not exists / not a directory");
            return;
        }

        File[] files = directoryFile.listFiles();
        if (files == null || files.length == 0) {
            logger.debug("Empty directory " + directory);
            return;
        }

        for (File file : files) {
            if (file.isFile() && namePattern.matcher(file.getName()).matches()) {
                try {
                    read0(file, charset);
                } catch (IOException e) {
                    logger.warn("Error while reading file " + file.getAbsolutePath(), e);
                }
            } else if (file.isDirectory() && recursive) {
                read0(file.getAbsolutePath(), namePattern, recursive, charset);
            }
            if (shutdown) {
                return;
            }
        }
    }

    /**
     * 进度监听器
     */
    public interface ProgressListener {

        /**
         * 读取一个文件开始
         */
        void enterFile(String filePath);

        /**
         * 读取一行结束(有效行才会回调)
         */
        void exitLine(String filePath, int lineNum, String line);

        /**
         * 读取一个文件结束
         */
        void exitFile(String filePath);

    }

}
