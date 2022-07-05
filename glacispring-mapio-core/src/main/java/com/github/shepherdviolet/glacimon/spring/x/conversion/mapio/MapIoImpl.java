/*
 * Copyright (C) 2022-2022 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/glacimon
 * Email: shepherdviolet@163.com
 */

package com.github.shepherdviolet.glacimon.spring.x.conversion.mapio;

import com.github.shepherdviolet.glacimon.java.misc.CheckUtils;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.FieldScreeningMode;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.FilterRuleException;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.RuleInfo;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.filter.Filter;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.DefaultExceptionFactory;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.ExceptionFactory;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.ExceptionFactoryAware;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.IoMode;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.filter.FilterProvider;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.filter.JavaFilterProvider;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.rule.*;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.rule.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MapIO实现类
 *
 * @author shepherdviolet
 */
public class MapIoImpl implements MapIo {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<Class<?>, Mapper> mappers = new ConcurrentHashMap<>();

    private RuleAnnotationManager ruleAnnotationManager = new DefaultRuleAnnotationManager();

    private FilterProvider filterProvider = new JavaFilterProvider();

    private ExceptionFactory exceptionFactory = new DefaultExceptionFactory();

    /**
     * 根据字典中'用注解定义的规则'映射Map元素
     *
     * @param rawData 原数据Map
     * @param ioMode INPUT / OUTPUT
     * @param fieldScreeningMode Map字段筛选模式
     * @param dictionaries 映射字典, 根据其中用注解定义的规则映射元素.
     *                     配置多个字典时, 执行顺序同数组顺序, 因此最后一个字典拥有最高优先级(对相同字段的处理以最后一个字典为准)
     * @return 映射结果Map
     */
    @Override
    public Map<String, Object> doMap(Map<String, Object> rawData, IoMode ioMode, FieldScreeningMode fieldScreeningMode, Class<?>... dictionaries) {
        return doMap(rawData, ioMode, fieldScreeningMode, new Class[][]{dictionaries});
    }

    @Override
    public Map<String, Object> doMap(Map<String, Object> rawData, IoMode ioMode, FieldScreeningMode fieldScreeningMode, Class<?>[]... dictionaries) {
        try {
            // do map
            return doMapInner(rawData, ioMode, fieldScreeningMode, dictionaries);
        } catch (RuntimeException t) {
            String errorTrace = Debug.getAndRemoveErrorTrace();
            if (Debug.isErrorLogEnabled() && logger.isErrorEnabled()) {
                logger.error("MapIO | doMap | Error while mapping data (" + ioMode + " mode)" + errorTrace, t);
            }
            throw t;
        } finally {
            // force clean trace
            Debug.getAndRemoveErrorTrace();
        }
    }

    /**
     * 预加载字典类,
     * IOMapper默认采用懒加载模式, 字典类用到了才会初始化.
     * 预加载适合线上系统, 在启动时就对所有字典类配置的规则进行检查, 避免出现非法的规则.
     *
     * @param dictionaries 映射字典
     */
    @Override
    public void preloadDictionaries(Class<?>... dictionaries) {
        if (dictionaries == null) {
            return;
        }
        for (Class<?> cls : dictionaries) {
            getMapper(cls);
        }
    }

    /**
     * 规则注解管理器, 实现规则注解与核心逻辑解耦. 可以通过自定义RuleAnnotationManager改变规则注解的类型.
     * @param ruleAnnotationManager ruleAnnotationManager
     */
    public MapIoImpl setRuleAnnotationParser(RuleAnnotationManager ruleAnnotationManager) {
        this.ruleAnnotationManager = ruleAnnotationManager;
        return this;
    }

    /**
     * 过滤器提供者
     * @param filterProvider filterProvider
     */
    public MapIoImpl setFilterProvider(FilterProvider filterProvider) {
        if (filterProvider == null) {
            filterProvider = new JavaFilterProvider();
        }
        this.filterProvider = filterProvider;
        return this;
    }

    /**
     * 异常工厂
     * @param exceptionFactory exceptionFactory
     */
    public MapIoImpl setExceptionFactory(ExceptionFactory exceptionFactory) {
        if (exceptionFactory == null) {
            exceptionFactory = new DefaultExceptionFactory();
        }
        this.exceptionFactory = exceptionFactory;
        return this;
    }

    /**
     * 打印映射器缓存信息
     */
    @Override
    public String printCachedMappers() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<Class<?>, Mapper> entry : mappers.entrySet()) {
            stringBuilder.append("\n").append(entry.getValue().printMapper());
        }
        return stringBuilder.toString();
    }

    /**
     * [inner] do map
     */
    Map<String, Object> doMapInner(Map<String, Object> rawData, IoMode ioMode, FieldScreeningMode fieldScreeningMode, Class<?>[][] dictionaries) {

        // checks

        if (rawData == null) {
            return new HashMap<>();
        }
        if (ioMode == null) {
            throw new IllegalArgumentException("MapIO | ioMode is null");
        }
        if (fieldScreeningMode == null) {
            throw new IllegalArgumentException("MapIO | fieldScreeningMode is null");
        }
        if (dictionaries == null) {
            dictionaries = new Class[0][0];
        }

        if (Debug.isTraceLogEnabled() && logger.isTraceEnabled()) {
            logger.trace("MapIO | doMap | Start mapping, ioMode: " + ioMode + ", fieldScreeningMode: " + fieldScreeningMode +
                    ", dictionaries: " + Arrays.toString(dictionaries) + ", data: " + rawData);
        }

        // results

        Map<String, Object> mappedData = new HashMap<>(rawData.size() * 2);
        Set<String> mappedKeys = new HashSet<>(rawData.size() * 2);

        // map by mappers

        for (Class<?>[] dictionaryArray : dictionaries) {
            if (dictionaryArray == null) {
                continue;
            }
            for (Class<?> dictionary : dictionaryArray) {
                if (dictionary == null) {
                    continue;
                }

                try {
                    Mapper mapper = getMapper(dictionary);
                    mapper.doMap(rawData, ioMode, mappedData, mappedKeys);
                } catch (RuntimeException t) {
                    if (Debug.isErrorLogEnabled()) {
                        Debug.addErrorTrace(", rules in dictionary '" + dictionary.getName() + "'");
                    }
                    throw t;
                }

                if (Debug.isTraceLogEnabled() && logger.isTraceEnabled()) {
                    logger.trace("MapIO | doMap | Mapped by rules in " + dictionary.getName() + " : " + mappedData);
                }
            }
        }

        // put unmapped data if needed

        if (fieldScreeningMode == FieldScreeningMode.PASS_BY_DEFAULT) {
            Map<String, Object> unmappedData = new HashMap<>(rawData);
            for (String mappedKey : mappedKeys) {
                unmappedData.remove(mappedKey);
            }
            mappedData.putAll(unmappedData);

            if (Debug.isTraceLogEnabled() && logger.isTraceEnabled()) {
                logger.trace("MapIO | doMap | Put all the unmapped data : " + mappedData);
            }
        }

        if (Debug.isTraceLogEnabled() && logger.isTraceEnabled()) {
            logger.trace("MapIO | doMap | End mapping, return: " + mappedData);
        }

        return mappedData;
    }

    /**
     * get or create mapper
     */
    private Mapper getMapper(Class<?> dictionary) {
        if (dictionary == null) {
            throw new IllegalArgumentException("MapIO | Can not parse mapper rule from dictionary class: null");
        }
        return mappers.computeIfAbsent(dictionary, this::parseDictionary);
    }

    /**
     * parse rules from dictionary
     */
    private Mapper parseDictionary(Class<?> dictionary) {
        try {
            Map<String, Field> allFields = getAllFields(dictionary, null);
            Map<String, FieldRule> inputRules = new HashMap<>(allFields.size() * 2);
            Map<String, FieldRule> outputRules = new HashMap<>(allFields.size() * 2);

            // parse rules from fields

            for (Map.Entry<String, Field> entry : allFields.entrySet()) {
                try {
                    parseInputRule(entry.getValue(), inputRules, dictionary);
                } catch (RuntimeException t) {
                    throw new FilterRuleException("MapIO | Failed to parse filter rules from '@Input...' series annotation on field: " + entry.getValue().getName(), t);
                }
                try {
                    parseOutputRule(entry.getValue(), outputRules, dictionary);
                } catch (RuntimeException t) {
                    throw new FilterRuleException("MapIO | Failed to parse filter rules from '@Output...' series annotation on field: " + entry.getValue().getName(), t);
                }
            }

            if (Debug.isInfoLogEnabled() && logger.isInfoEnabled()) {
                logger.info("MapIO | Dictionary loaded: " + dictionary.getName());
            }

            return new Mapper(new ArrayList<>(inputRules.values()), new ArrayList<>(outputRules.values()), exceptionFactory,
                    new RuleInfo(dictionary.getName(), null, null, null, null, null));
        } catch (Throwable t) {
            throw new FilterRuleException("MapIO | Failed to parse mapper rule from dictionary class: " + dictionary, t);
        }
    }

    /**
     * 从字典类中获取Field
     */
    private Map<String, Field> getAllFields(Class<?> dictionary, Map<String, Field> result) {
        if (result == null) {
            result = new HashMap<>();
        }

        Class<?> superclass = dictionary.getSuperclass();
        if (superclass != null) {
            getAllFields(superclass, result);
        }

        Field[] fields = dictionary.getDeclaredFields();
        for (Field field : fields) {
            result.put(field.getName(), field);
        }

        return result;
    }

    /**
     * parse @Input... series
     */
    private void parseInputRule(Field field, Map<String, FieldRule> parsedRules, Class<?> dictionary) {
        InputRule inputRule = ruleAnnotationManager.getInputRule(field);
        List<OrderedRule> rules = ruleAnnotationManager.getInputSeriesRules(field);
        rules = checkAndSortRules(rules);

        //clause num
        int clauseNum = rules.size();

        // skip parse if no @Input defined
        if (inputRule == null) {
            if (clauseNum > 0) {
                throw new FilterRuleException("MapIO | Missing annotation '@Input'. if you set '@Input...' series annotation on field, the annotation @Input is required");
            }
            return;
        }

        // The fieldName gets the field name in the dictionary class by default. Not recommended for use @Input(fieldName="...")
        String fieldName = CheckUtils.notEmpty(inputRule.fieldName()) ? inputRule.fieldName() : field.getName();

        String fromKey = CheckUtils.notEmpty(inputRule.fromKey()) ? inputRule.fromKey() : fieldName;
        String toKey = fieldName;

        List<RuleClause> clauses = new ArrayList<>(clauseNum);

        // handle rule clauses
        for (OrderedRule rule : rules) {
            RuleClause clause = null;

            try {

                if (rule instanceof InputFilterRule) {

                    InputFilterRule inputFilter = (InputFilterRule) rule;
                    clause = buildFilterClause(inputFilter.type(), inputFilter.name(), inputFilter.args(),
                            new RuleInfo(dictionary.getName(), fieldName, fromKey, toKey,
                                    printFilterName(inputFilter.type(), inputFilter.name()), null));

                } else if (rule instanceof InputElementFilterRule) {

                    InputElementFilterRule inputElementFilter = (InputElementFilterRule) rule;
                    clause = buildElementFilterClause(inputElementFilter.type(), inputElementFilter.name(), inputElementFilter.args(), inputElementFilter.keepOrder(),
                            new RuleInfo(dictionary.getName(), fieldName, fromKey, toKey,
                                    printFilterName(inputElementFilter.type(), inputElementFilter.name()), null));

                } else if (rule instanceof InputMapperRule) {

                    InputMapperRule inputMapper = (InputMapperRule) rule;
                    try {
                        preloadDictionaries(inputMapper.dictionary());
                    } catch (Throwable t) {
                        throw new FilterRuleException("MapIO | Failed to parse sub mapper rule from sub dictionary class: " + inputMapper.dictionary(), t);
                    }
                    clause = buildMapperClause(inputMapper.fieldScreeningMode(), inputMapper.dictionary(), IoMode.INPUT,
                            new RuleInfo(dictionary.getName(), fieldName, fromKey, toKey,
                                    null, "@InputMapper{dictionary=" + inputMapper.dictionary().getName() + "}"));

                }

            } catch (Throwable t) {
                throw new FilterRuleException("MapIO | Failed to parse filter rule from annotation: " + rule.getAnnotation(), t);
            }

            if (clause == null) {
                continue;
            }
            clauses.add(clause);
        }

        parsedRules.put(toKey, new FieldRule(fromKey, toKey, inputRule.required(), clauses,
                new RuleInfo(dictionary.getName(), fieldName, fromKey, toKey, null, null)));
    }

    /**
     * parse @Output... series
     */
    private void parseOutputRule(Field field, Map<String, FieldRule> parsedRules, Class<?> dictionary) {
        OutputRule outputRule = ruleAnnotationManager.getOutputRule(field);
        List<OrderedRule> rules = ruleAnnotationManager.getOutputSeriesRules(field);
        rules = checkAndSortRules(rules);

        //clause num
        int clauseNum = rules.size();

        // skip parse if no @Input defined
        if (outputRule == null) {
            if (clauseNum > 0) {
                throw new FilterRuleException("MapIO | Missing annotation '@Output'. if you set '@Output...' series annotation on field, the annotation @Output is required");
            }
            return;
        }

        // The fieldName gets the field name in the dictionary class by default. Not recommended for use @Output(fieldName="...")
        String fieldName = CheckUtils.notEmpty(outputRule.fieldName()) ? outputRule.fieldName() : field.getName();

        String fromKey = fieldName;
        String toKey = CheckUtils.notEmpty(outputRule.toKey()) ? outputRule.toKey() : fieldName;

        // @Output的toKey不允许重复, 但是@Input的fromKey允许重复
        if (parsedRules.containsKey(toKey)) {
            throw new FilterRuleException("MapIO | Duplicate toKey=\"" + toKey + "\" in annotation @Output, previous on field: " +
                    parsedRules.get(toKey).getFromKey() + ", current on field: " + fromKey);
        }

        List<RuleClause> clauses = new ArrayList<>(clauseNum);

        // handle rule clauses
        for (OrderedRule rule : rules) {
            RuleClause clause = null;

            try {

                if (rule instanceof OutputFilterRule) {

                    OutputFilterRule outputFilter = (OutputFilterRule) rule;
                    clause = buildFilterClause(outputFilter.type(), outputFilter.name(), outputFilter.args(),
                            new RuleInfo(dictionary.getName(), fieldName, fromKey, toKey,
                                    printFilterName(outputFilter.type(), outputFilter.name()), null));

                } else if (rule instanceof OutputElementFilterRule) {

                    OutputElementFilterRule outputElementFilter = (OutputElementFilterRule) rule;
                    clause = buildElementFilterClause(outputElementFilter.type(), outputElementFilter.name(), outputElementFilter.args(), outputElementFilter.keepOrder(),
                            new RuleInfo(dictionary.getName(), fieldName, fromKey, toKey,
                                    printFilterName(outputElementFilter.type(), outputElementFilter.name()), null));

                } else if (rule instanceof OutputMapperRule) {

                    OutputMapperRule outputMapper = (OutputMapperRule) rule;
                    try {
                        preloadDictionaries(outputMapper.dictionary());
                    } catch (Throwable t) {
                        throw new FilterRuleException("MapIO | Failed to parse sub mapper rule from sub dictionary class: " + outputMapper.dictionary(), t);
                    }
                    clause = buildMapperClause(outputMapper.fieldScreeningMode(), outputMapper.dictionary(), IoMode.OUTPUT,
                            new RuleInfo(dictionary.getName(), fieldName, fromKey, toKey,
                                    null, "@OutputMapper{dictionary=" + outputMapper.dictionary() + "}"));

                }

            } catch (Throwable t) {
                throw new FilterRuleException("MapIO | Failed to parse filter rule from annotation: " + rule.getAnnotation(), t);
            }

            if (clause == null) {
                continue;
            }
            clauses.add(clause);
        }

        parsedRules.put(toKey, new FieldRule(fromKey, toKey, outputRule.required(), clauses,
                new RuleInfo(dictionary.getName(), fieldName, fromKey, toKey, null, null)));
    }

    /**
     * check the rules, and sort them as needed
     */
    private List<OrderedRule> checkAndSortRules(List<OrderedRule> rules) {
        if (rules == null) {
            return new ArrayList<>(0);
        }

        // 规则顺序集合
        Set<Integer> orderSet = new HashSet<>();
        // 每种规则的数量
        Map<Class<? extends OrderedRule>, Integer> ruleCounter = new HashMap<>();

        for (OrderedRule rule : rules) {
            // 规则顺序不可重复
            int order = rule.getOrder();
            if (order >= 0) {
                if (orderSet.contains(order)) {
                    throw new FilterRuleException("MapIO | The 'clauseOrder = " + order + "' is duplicated!");
                }
                orderSet.add(order);
            }

            // 规则计数
            Class<? extends OrderedRule> type = rule.getClass();
            ruleCounter.put(type, ruleCounter.getOrDefault(type, 0) + 1);
        }

        // 规则顺序要么全都设置, 要么全都不设置
        // 判断条件: 当有规则顺序设置, 且规则顺序数量不同于规则数, 报错
        if (orderSet.size() > 0 && orderSet.size() != rules.size()) {
            throw new FilterRuleException("MapIO | If you want to set the 'clauseOrder' of @Input... (or @Output...) series annotation for a field, " +
                    "you must set all of them, not only part of them!");
        }

        // 存在多种过滤规则, 且同一种过滤规则存在多条时, 程序无法判断代码书写顺序, 需要手动设置执行顺序
        // 判断条件: 当没有规则顺序设置, 且规则种类大于1, 且有一种规则数量>1, 报错
        if (orderSet.size() <= 0 && ruleCounter.size() > 1) {
            for (Integer count : ruleCounter.values()) {
                if (count > 1) {
                    throw new FilterRuleException("MapIO | Because the field has multiple rule categories, and one rule " +
                            "category has multiple rule clauses, in this case, the program cannot determine the code writing order. " +
                            "Please set 'clauseOrder' for each rule (@Input... or @Output... series annotation) for this field, " +
                            "to clarify the order of rules' execution. See https://github.com/shepherdviolet/glacimon/blob/master/docs/mapio/guide.md");
                }
            }
        }

        // 代码书写顺序执行
        if (orderSet.size() > 0) {
            rules.sort(Comparator.comparingInt(OrderedRule::getOrder));
        }

        return rules;

    }

    /**
     * build FilterClause
     */
    private FilterClause buildFilterClause(Class<? extends Filter> type, String name, String[] args,
                                           RuleInfo ruleInfo){
        // "" -> null
        name = !"".equals(name) ? name : null;

        // continue
        if (Filter.class.equals(type) && name == null) {
            return null;
        }

        Filter filter = findFilter(type, name, args, ruleInfo);

        return new FilterClause(filter, args, ruleInfo);
    }

    /**
     * build ElementFilterClause
     */
    private ElementFilterClause buildElementFilterClause(Class<? extends Filter> type, String name, String[] args, boolean keepOrder,
                                                         RuleInfo ruleInfo) {
        // "" -> null
        name = !"".equals(name) ? name : null;

        // continue
        if (Filter.class.equals(type) && name == null) {
            return null;
        }

        Filter filter = findFilter(type, name, args, ruleInfo);

        return new ElementFilterClause(filter, args, keepOrder,
                exceptionFactory, ruleInfo);
    }

    /**
     * build MapperClause
     */
    private MapperClause buildMapperClause(FieldScreeningMode fieldScreeningMode, Class<?> dictionary, IoMode ioMode,
                                           RuleInfo ruleInfo) {

        return new MapperClause(fieldScreeningMode, dictionary, this, ioMode, exceptionFactory, ruleInfo);
    }

    /**
     * find Filter from filterProvider
     */
    private Filter findFilter(Class<? extends Filter> type, String name, String[] args, RuleInfo ruleInfo) {
        // check
        FilterProvider filterProvider = this.filterProvider;
        if (filterProvider == null) {
            throw new IllegalStateException("MapIO | Can not find Filter from filterProvider, filterProvider is null");
        }

        // find filter
        Filter filter = filterProvider.findFilter(type, name);
        if (filter == null) {
            throw new IllegalStateException("MapIO | Can not find Filter from filterProvider, filterProvider#findFilter returns null");
        }

        // inject ExceptionFactory
        if (filter instanceof ExceptionFactoryAware) {
            ((ExceptionFactoryAware) filter).setExceptionFactory(exceptionFactory);
        }

        // pre-check args
        try {
            filter.doPreCheckArgs(args);
        } catch (Throwable t) {
            throw new FilterRuleException("MapIO | Illegal filter args: " + Arrays.toString(args) +
                    ", " + ruleInfo, t);
        }

        return filter;
    }

    private String printFilterName(Class<? extends Filter> type, String name) {
        if (type == null) {
            type = Filter.class;
        }
        if (CheckUtils.notEmpty(name)) {
            return type.getName() + "@" + name;
        } else {
            return type.getName();
        }
    }

}
