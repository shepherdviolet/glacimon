package com.github.shepherdviolet.glacimon.spring.expr;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.*;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <pre>
 * SpEL表达式解析器
 *
 * 本解析器采用`模板模式`, 即表达式需用<code>#{</code>与<code>}</code>包裹, 格式为<code>#{SpEL表达式}</code>, 未被包裹部分视为普通字符串:
 * - <code>#name</code>：用于引用传入的 <code>variables</code> 中的值。
 * - <code>@name</code>：用于引用Spring上下文中的Bean，例如获取环境参数可以使用 <code>@environment.getProperty('key')</code>。
 * - <code>methodName(...)</code>：可以直接调用根对象的方法.
 * </pre>
 *
 * 使用示例：
 * <pre>
 * // 创建一个SpelExpressionResolver实例, rootObject有一个方法名为'toJson'实现将Map转为JSON String
 * SpelExpressionResolver resolver = new SpelExpressionResolver(rootObject, applicationContext);
 *
 * // 变量
 * Map&lt;String, Object&gt; variables = new HashMap&lt;&gt;();
 * variables.put("name", "John");
 *
 * // 从变量取值
 * Object result = resolver.resolveExpression("Result: #{#name + ' Doe'}", variables);
 * System.out.println(result); // 输出: Result: John Doe
 *
 * // 调用bean方法, 假设getMessage方法返回"Hello"
 * result = resolver.resolveExpression("Result: #{<code></code>@myService.getMessage() + ' world'}", variables);
 * System.out.println(result); // 输出: Result: Hello world
 *
 * // 调用bean方法, 假设getMap方法返回一个Map
 * result = resolver.resolveExpression("Result: #{toJson(@myService.getMap())}", variables);
 * System.out.println(result); // 输出: Result: Map的JSONString
 * </pre>
 *
 * @author shepherdviolet
 */
public class SpelExpressionResolver {

    // SpEL表达式的前缀
    private static final String PREFIX = "#{";
    // SpEL表达式的后缀
    private static final String SUFFIX = "}";

    // 用于解析SpEL表达式的解析器
    protected final ExpressionParser expressionParser = new SpelExpressionParser();

    // 解析器上下文，用于定义表达式的模板信息
    protected final ParserContext parserContext = new ParserContext() {

        /**
         * 判断是否为模板表达式，这里返回true表示是模板表达式。
         * @return 总是返回true
         */
        @Override
        public boolean isTemplate() {
            return true;
        }

        /**
         * 获取表达式的前缀。
         * @return 表达式前缀
         */
        @Override
        public String getExpressionPrefix() {
            return PREFIX;
        }

        /**
         * 获取表达式的后缀。
         * @return 表达式后缀
         */
        @Override
        public String getExpressionSuffix() {
            return SUFFIX;
        }

    };

    // 表达式缓存，用于存储已经解析过的表达式，避免重复解析
    protected final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    // SpEL表达式的根对象，可在表达式中调用该对象方法或变量
    private final Object spelRootObject;
    // Spring的Bean工厂，用于解析 @name 形式的表达式引用Spring Bean
    private final BeanFactory spelBeanFactory;

    /**
     * 默认构造函数，根对象和Bean工厂都为null。
     */
    public SpelExpressionResolver() {
        this(null, null);
    }

    /**
     * 构造函数，指定根对象，Bean工厂为null。
     * @param spelRootObject SpEL表达式的根对象, SpEL表达式能够直接调用该对象的方法和变量
     */
    public SpelExpressionResolver(Object spelRootObject) {
        this(spelRootObject, null);
    }

    /**
     * 构造函数，指定Bean工厂，根对象为null。
     * @param spelBeanFactory Spring的Bean工厂, SpEL表达式能够通过#{<code>@</code>beanname}引用其上下文中的bean对象
     */
    public SpelExpressionResolver(BeanFactory spelBeanFactory) {
        this(null, spelBeanFactory);
    }

    /**
     * 构造函数，指定根对象和Bean工厂。
     * @param spelRootObject SpEL表达式的根对象, SpEL表达式能够直接调用该对象的方法和变量
     * @param spelBeanFactory Spring的Bean工厂, SpEL表达式能够通过#{<code>@</code>beanname}引用其上下文中的bean对象
     */
    public SpelExpressionResolver(Object spelRootObject, BeanFactory spelBeanFactory) {
        this.spelRootObject = spelRootObject;
        this.spelBeanFactory = spelBeanFactory;
    }

    /**
     * 检查给定的表达式是否可以正确解析。
     * @param expression 要检查的表达式
     * @throws ParseException 如果表达式解析失败抛出此异常
     */
    public void checkExpression(String expression) throws ParseException {
        parseExpression(expression);
    }

    /**
     * 解析并计算给定的SpEL表达式。
     * @param expression 要解析的表达式
     * @param variables 表达式中使用的变量, 用#{#name}取值
     * @return 表达式计算的结果
     * @throws ParseException 如果表达式解析失败抛出此异常
     * @throws EvaluationException 如果表达式计算失败抛出此异常
     */
    public Object resolveExpression(String expression, Map<String, Object> variables) throws ParseException, EvaluationException {
        // 解析表达式
        Expression exp = parseExpression(expression);
        // 创建评估上下文
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext(spelRootObject);
        if (variables != null) {
            // 设置表达式中的变量, 用#{#name}取值
            evaluationContext.setVariables(variables);
        }
        if (spelBeanFactory != null) {
            // 设置Bean解析器，用于解析 #{@beanname} 形式的表达式
            evaluationContext.setBeanResolver(new BeanFactoryResolver(spelBeanFactory));
        }
        // 计算表达式的值
        return exp.getValue(evaluationContext);
    }

    /**
     * 解析并计算给定的SpEL表达式，并将结果转换为布尔类型。
     * @param expression 要解析的表达式
     * @param variables 表达式中使用的变量, 用#{#name}取值
     * @return 表达式计算结果的布尔值
     * @throws ParseException 如果表达式解析失败抛出此异常
     * @throws EvaluationException 如果表达式计算失败抛出此异常
     */
    public boolean resolveExpressionToBoolean(String expression, Map<String, Object> variables) throws ParseException, EvaluationException {
        // 解析并计算表达式
        Object result = resolveExpression(expression, variables);
        // 将结果转换为布尔类型
        return Boolean.TRUE.equals(result);
    }

    protected Expression parseExpression(String expression) throws ParseException {
        // 从缓存中获取表达式
        Expression exp = expressionCache.get(expression);
        if (exp == null) {
            // 如果缓存中不存在，则解析表达式
            exp = expressionParser.parseExpression(expression, parserContext);
            // 将解析后的表达式放入缓存
            expressionCache.put(expression, exp);
        }
        return exp;
    }

}