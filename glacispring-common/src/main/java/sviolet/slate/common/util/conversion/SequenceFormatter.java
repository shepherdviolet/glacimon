package sviolet.slate.common.util.conversion;

import org.springframework.util.PropertyPlaceholderHelper;
import sviolet.slate.common.util.common.LambdaBuilder;
import sviolet.thistle.util.conversion.StringUtils;
import sviolet.thistle.util.judge.CheckUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;

/**
 * 流水号格式化工具
 *
 * @author S.Violet
 */
public class SequenceFormatter {

    /* **** Default ************************************************************************************************* */

    private static final DateTimeFormatter DATE_TIME_FORMATTER_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Map<String, ValueProvider> DEFAULT_VALUE_PROVIDERS = Collections.unmodifiableMap(LambdaBuilder.hashMap(map -> {
        // {_date_}取当前日期: yyyyMMdd
        map.put("_date_", properties -> LocalDate.now().format(DATE_TIME_FORMATTER_YYYYMMDD));
    }));

    private static final Map<String, ValueProcessor> DEFAULT_VALUE_PROCESSORS = Collections.unmodifiableMap(LambdaBuilder.hashMap(map -> {
        // {1<+>1} -> 2
        map.put("+", (value, parameter) -> String.valueOf(Long.parseLong(value) + Long.parseLong(parameter)));
        // {1<++>} -> 2
        map.put("++", (value, parameter) -> String.valueOf(Long.parseLong(value) + 1));
        // {1<pad-to-len>6} -> 000001   {1234567<pad-to-len>6} -> 234567
        map.put("pad-to-len", (value, parameter) -> {
            int len = Integer.parseInt(parameter);
            return StringUtils.leftPaddingToLength(value, len, len, '0'); });
        // {1000001<trim-to-len>6} -> 1
        map.put("trim-to-len", (value, parameter) -> {
            int len = Integer.parseInt(parameter);
            if (value.length() > len) { value = value.substring(value.length() - len); }
            return StringUtils.leftTrimToLength(value, 1, '0'); });
    }));

    private static final SequenceFormatter DEFAULT_FORMATTER = new SequenceFormatter(DEFAULT_VALUE_PROVIDERS, DEFAULT_VALUE_PROCESSORS);

    /**
     * <p>默认的流水号格式化工具</p>
     *
     * <p>--示例------------------------------------------------------------------------------------------------------</p>
     *
     * <p>方法: format(String format, Map<String, Object> dataMap);//用{name}从dataMap里取值</p>
     * <p>假设: dataMap={"sequence":"10000111"}</p>
     *
     * <p>format("{_date_}99{sequence}", dataMap) -> 202003179910000111 : 直接取值, {_date_}为yyyyMMdd格式的日期</p>
     * <p>format("{_date_}99{sequence&lt;pad-to-len&gt;10}", dataMap) -> 20200317990010000111 : 不足左补0, 太长左边裁掉</p>
     * <p>format("{_date_}99{sequence&lt;pad-to-len&gt;6}", dataMap) -> 2020031799000111 : 不足左补0, 太长左边裁掉</p>
     * <p>format("{sequence&lt;trim-to-len&gt;6}", dataMap) -> 111 : 太长左边裁掉后再去掉左边的0</p>
     * <p>format("{sequence&lt;+&gt;5&lt;trim-to-len&gt;6}", dataMap) -> 116 : 先+5, 太长左边裁掉后再去掉左边的0</p>
     * <p>format("{sequence&lt;++&gt;&lt;++&gt;}", dataMap) -> 10000113 : 自加1两次</p>
     *
     * <p>--示例------------------------------------------------------------------------------------------------------</p>
     *
     * <p>方法: format(String format, Object data);//用{}取data值</p>
     *
     * <p>format("{_date_}99{}", "10000111") -> 202003179910000111 : 直接取值, {_date_}为yyyyMMdd格式的日期</p>
     * <p>format("{_date_}99{&lt;pad-to-len&gt;10}", "10000111") -> 20200317990010000111 : 不足左补0, 太长左边裁掉</p>
     * <p>......</p>
     */
    public static SequenceFormatter defaultFormatter() {
        return DEFAULT_FORMATTER;
    }

    /* ************************************************************************************************************** */

    private static final PropertyPlaceholderHelper PLACEHOLDER_HELPER = new PropertyPlaceholderHelper(
            "{", "}", ":", false);

    private final Map<String, ValueProvider> valueProviders;
    private final Map<String, ValueProcessor> valueProcessors;

    /**
     * @param valueProviders 对特定Key提供Value, Map的Key对应占位符中的变量名 ({name&lt;operator&gt;parameter} 中的name)
     * @param valueProcessors Value变换器, Map的Key对应占位符中的操作名 ({name&lt;operator&gt;parameter} 中的operator)
     */
    public SequenceFormatter(Map<String, ValueProvider> valueProviders, Map<String, ValueProcessor> valueProcessors) {
        this.valueProviders = valueProviders;
        this.valueProcessors = valueProcessors;
    }

    /**
     * <p>格式化流水号, 用{}取data的值</p>
     * <p>示例1: </p>
     * <p>{}</p>
     * <p>处理流程: </p>
     * <p>1.返回data值</p>
     * <p></p>
     * <p>示例2: </p>
     * <p>{&lt;operator1&gt;parameter1&lt;operator2&gt;parameter2:default}</p>
     * <p>处理流程: </p>
     * <p>1.若data不为空, 则继续处理, 若data为空, 则结果为default</p>
     * <p>2.使用名为'operator1'的ValueProcessor变换data值, 参数为'parameter1'</p>
     * <p>3.使用名为'operator2'的ValueProcessor变换结果, 参数为'parameter2'</p>
     * <p>4.返回变换结果</p>
     *
     * @param format 流水号格式
     * @param data 数据
     * @throws SequenceFormatException 格式化异常
     */
    public String format(String format, Object data) throws SequenceFormatException {
        return format(format, data, null);
    }

    /**
     * <p>格式化流水号, 用{name}从dataMap里取值</p>
     * <p>示例1: </p>
     * <p>{name}</p>
     * <p>处理流程: </p>
     * <p>1.从dataMap中取'name'的值, 返回结果</p>
     * <p></p>
     * <p>示例2: </p>
     * <p>{name&lt;operator1&gt;parameter1&lt;operator2&gt;parameter2:default}</p>
     * <p>处理流程: </p>
     * <p>1.从dataMap中取'name'的值, 若不为空, 则继续处理, 若为空, 则结果为default</p>
     * <p>2.使用名为'operator1'的ValueProcessor变换结果, 参数为'parameter1'</p>
     * <p>3.使用名为'operator2'的ValueProcessor变换结果, 参数为'parameter2'</p>
     * <p>4.返回变换结果</p>
     *
     * @param format 流水号格式
     * @param dataMap 数据
     * @throws SequenceFormatException 格式化异常
     */
    public String format(String format, Map<String, Object> dataMap) throws SequenceFormatException {
        return format(format, null, dataMap);
    }

    private String format(String format, Object data, Map<String, Object> dataMap) throws SequenceFormatException {
        try {
            // check
            if (CheckUtils.isEmpty(format)) {
                throw new IllegalArgumentException("format is null or empty");
            }
            // resolve placeholders
            return PLACEHOLDER_HELPER.replacePlaceholders(format, placeholderName -> {
                // parse first operator
                int operatorStart = placeholderName.indexOf('<');
                int operatorEnd = placeholderName.indexOf('>', operatorStart);
                ValueProcessor valueProcessor = getValueProcessor(placeholderName, operatorStart, operatorEnd);

                // parse name
                String name;
                if (valueProcessor == null) {
                    name = placeholderName;
                } else {
                    name = placeholderName.substring(0, operatorStart);
                }

                // get value
                Object value = null;
                if ("".equals(name)) {
                    value = data;
                }
                if (value == null) {
                    ValueProvider valueProvider = valueProviders.get(name);
                    if (valueProvider != null) {
                        value = valueProvider.provide(dataMap);
                    } else if (dataMap != null) {
                        value = dataMap.get(name);
                    }
                }
                if (value == null) {
                    return null;
                }

                String valueStr = String.valueOf(value);

                // operator process
                while(valueProcessor != null) {

                    // get next operator
                    int nextOperatorStart = placeholderName.indexOf('<', operatorEnd);
                    int nextOperatorEnd = placeholderName.indexOf('>', nextOperatorStart);
                    ValueProcessor nextValueProcessor = getValueProcessor(placeholderName, nextOperatorStart, nextOperatorEnd);

                    // get operator parameter
                    String operatorParameter;
                    if (nextValueProcessor == null) {
                        operatorParameter = placeholderName.substring(operatorEnd + 1);
                    } else {
                        operatorParameter = placeholderName.substring(operatorEnd + 1, nextOperatorStart);
                    }

                    // process
                    try {
                        valueStr = valueProcessor.process(valueStr, operatorParameter);
                    } catch (Throwable t) {
                        throw new RuntimeException("Error while processing operator '<" +
                                getOperator(placeholderName, operatorStart, operatorEnd) +
                                ">', parameter '" + operatorParameter + "', value '" + valueStr + "', placeholder: " +
                                placeholderName);
                    }

                    // next
                    operatorStart = nextOperatorStart;
                    operatorEnd = nextOperatorEnd;
                    valueProcessor = nextValueProcessor;

                }

                return valueStr;
            });
        } catch (Throwable t) {
            throw new SequenceFormatException("Error while formatting sequence, format: " + format + ", properties: " + dataMap, t);
        }
    }

    private ValueProcessor getValueProcessor(String placeholderName, int operatorStart, int operatorEnd) {
        String operator = getOperator(placeholderName, operatorStart, operatorEnd);
        if (operator == null) {
            return null;
        }
        ValueProcessor valueProcessor = valueProcessors.get(operator);
        if (valueProcessor == null) {
            throw new IllegalArgumentException("Undefined operator '<" + operator +
                    ">', no corresponding ValueProcessor found, in placeholder: " + placeholderName);
        }
        return valueProcessor;
    }

    private String getOperator(String placeholderName, int operatorStart, int operatorEnd) {
        if (operatorStart < 0 || operatorEnd < 0) {
            return null;
        }
        String operator = placeholderName.substring(operatorStart + 1, operatorEnd);
        if (CheckUtils.isEmpty(operator)) {
            throw new IllegalArgumentException("Operator name is empty '<>', in placeholder: " + placeholderName);
        }
        return operator;
    }

    /**
     * 格式化异常
     */
    public static class SequenceFormatException extends Exception {

        private static final long serialVersionUID = -7980323109875435405L;

        public SequenceFormatException(String message) {
            super(message);
        }

        public SequenceFormatException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    /**
     * 对特定Key提供Value
     */
    public interface ValueProvider {

        String provide(Map<String, Object> properties);

    }

    /**
     * Value变换器
     */
    public interface ValueProcessor {

        String process(String value, String parameter) throws SequenceFormatException;

    }

}
