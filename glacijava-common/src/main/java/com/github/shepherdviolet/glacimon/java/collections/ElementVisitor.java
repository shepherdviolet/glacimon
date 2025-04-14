/*
 * Copyright (C) 2022-2025 S.Violet
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

package com.github.shepherdviolet.glacimon.java.collections;

import com.github.shepherdviolet.glacimon.java.misc.CheckUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * `便捷安全`的跨层级集合元素访问工具 ElementVisitor
 *
 * TODO 文档
 *
 * @author shepherdviolet
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class ElementVisitor {

    /**
     * 开始访问指定Map
     * @param root 根元素
     */
    public static ElementVisitor of(Map root) {
        return new ElementVisitor(root);
    }

    /**
     * 开始访问指定Collection
     * @param root 根元素
     */
    public static ElementVisitor of(Collection root) {
        return new ElementVisitor(root);
    }


    // Public ////////////////////////////////////////////////////////////////////////////////////////////////////


    private final Object root;
    private final List<Path> paths = new ArrayList<>();
    private Set<ErrorCategory> suppressedErrorCategories = Collections.emptySet();
    private Set<ErrorCode> suppressedErrorCodes = Collections.emptySet();
    private Consumer<ElementVisitException> exceptionHandler = e -> {throw e;};
    private Supplier<Object> supplyIfElementAbsent;

    private final BasicVisitor basicVisitor = new BasicVisitor();
    private final OnewayVisitor onewayVisitor = new OnewayVisitor();
    private final MultiwayVisitor multiwayVisitor = new MultiwayVisitor();
    private final OnewayPathPlanner onewayPathPlanner = new OnewayPathPlanner();
    private final MultiwayPathPlanner multiwayPathPlanner = new MultiwayPathPlanner();

    private ElementVisitor(Object root) {
        this.root = root;
    }

    /**
     * 路径配置, 访问Map的子元素
     * @param key key
     */
    public OnewayPathPlanner child(String key) {
        _addPath(key);
        return onewayPathPlanner;
    }

    /**
     * 路径配置, 访问Collection的子元素 (遍历)
     */
    public MultiwayPathPlanner children() {
        _addPath();
        return multiwayPathPlanner;
    }

    /**
     * 压制(忽略)指定的错误类别.
     * 注意, ExceptionHandler将无法接收到被压制(忽略)的异常. 
     * 重复调用此方法设置会覆盖原来设置的ErrorCategory. 
     * @param errorCategory 需要忽略的错误类别, 可配置多个
     */
    public ElementVisitor suppressErrorCategories(ErrorCategory... errorCategory) {
        // 注意, 会覆盖原配置
        this.suppressedErrorCategories = new HashSet<>(Arrays.asList(errorCategory));
        return this;
    }

    /**
     * 压制(忽略)指定的错误码.
     * 注意, ExceptionHandler将无法接收到被压制(忽略)的异常. 
     * 重复调用此方法设置会覆盖原来设置的ErrorCode.
     * @param errorCodes 需要忽略的错误码, 可配置多个
     */
    public ElementVisitor suppressErrorCodes(ErrorCode... errorCodes) {
        // 注意, 会覆盖原配置
        this.suppressedErrorCodes = new HashSet<>(Arrays.asList(errorCodes));
        return this;
    }

    /**
     * <p>设置异常处理器, 默认为: e -> {throw e;} 即一律抛出异常.</p>
     * <p>注意, 如果exceptionHandler中不抛出异常, get/remove/forEach方法就不会抛出异常了, 返回的元素可能为null或空集合.</p>
     * <p>注意, 使用suppressErrorCategories和suppressErrorCodes压制(忽略)的异常将不会被exceptionHandler接收.</p>
     * <p>示范一: 只打印日志, 不抛出异常 (返回的元素可能为null或空集合)</p>
     * <pre>
     * e -> {
     *      logger.error("Error occurred when visiting element", e);
     * }
     * </pre>
     * <p>示范二: 数据缺失类的错误只打印日志, 不抛出异常(返回的元素可能为null或空集合); 其他错误抛出异常</p>
     * <pre>
     * e -> {
     *      switch (e.getErrorCategory()) {
     *          case DATA_MISSING:
     *              logger.error("Error occurred when visiting element", e);
     *              break;
     *          default:
     *              throw e;
     *      }
     * }
     * </pre>
     * <p>示范三: 封装成其他异常抛出</p>
     * <pre>
     * e -> {
     *      throw new ServiceException("Error occurred when visiting element", e);
     * }
     * </pre>
     *
     * @param exceptionHandler 异常处理器, 默认为: e -> {throw e;}
     * @throws IllegalArgumentException exceptionHandler为空
     */
    public ElementVisitor exceptionHandler(Consumer<ElementVisitException> exceptionHandler) throws IllegalArgumentException {
        _checkExceptionHandler(exceptionHandler);
        this.exceptionHandler = exceptionHandler;
        return this;
    }


    // Private //////////////////////////////////////////////////////////////////////////////////////////////////


    private void _addPath(String key) {
        paths.add(new Path(ParentType.MAP, key));
    }

    private void _addPath() {
        paths.add(new Path(ParentType.COLLECTION, null));
    }

    private void _checkElementConsumer(Consumer elementConsumer) {
        if (elementConsumer == null) {
            throw new IllegalArgumentException("elementConsumer cannot be null");
        }
    }

    private void _checkElementReplacer(Function elementReplacer) {
        if (elementReplacer == null) {
            throw new IllegalArgumentException("elementReplacer cannot be null");
        }
    }

    private void _checkExceptionHandler(Consumer<ElementVisitException> exceptionHandler) {
        if (exceptionHandler == null) {
            throw new IllegalArgumentException("exceptionHandler cannot be null");
        }
    }

    private ElementVisitException _buildVisitException(ErrorCode errorCode, Throwable cause, 
                                                       int level, int collectionIndex, String messagePrefix, String messageSuffix, String indicateMessage) {
        StringBuilder pathYouExpectedBuilder = new StringBuilder("root");
        StringBuilder pathErrorOccurredBuilder = new StringBuilder("root");
        if (CheckUtils.notEmpty(paths)) {
            for (int i = 0 ; i < paths.size() ; i++) {
                Path p = paths.get(i);
                if (p.parentType == ParentType.MAP) {
                    pathYouExpectedBuilder.append('.').append(p.key);
                    if (i <= level) {
                        pathErrorOccurredBuilder.append('.').append(p.key);
                    }
                } else {
                    pathYouExpectedBuilder.append("[*]");
                    if (i <= level) {
                        pathErrorOccurredBuilder.append("[").append(collectionIndex >= 0 ? collectionIndex + "" : "*").append("]");
                    }
                }
            }
        }

        List<String> prettyErrorIndicatorPrefix = new ArrayList<>();
        List<String> prettyErrorIndicatorSuffix = new ArrayList<>();
        if (CheckUtils.notEmpty(paths)) {
            for (int i = 0 ; i < paths.size() ; i++) {
                Path p = paths.get(i);
                if (p.parentType == ParentType.MAP) {
                    prettyErrorIndicatorPrefix.add("{");
                    if (i == level + 1) {
                        prettyErrorIndicatorPrefix.add("    <-- " + indicateMessage);
                    }
                    prettyErrorIndicatorPrefix.add("\n");
                    prettyErrorIndicatorSuffix.add("}");
                    for (int blanks = 1; blanks <= i + 1; blanks++) {
                        prettyErrorIndicatorPrefix.add("  ");
                        if (blanks > 1) {
                            prettyErrorIndicatorSuffix.add("  ");
                        }
                    }
                    prettyErrorIndicatorSuffix.add("\n");
                    prettyErrorIndicatorPrefix.add(p.key + ": ");
                } else {
                    prettyErrorIndicatorPrefix.add("[");
                    if (i == level + 1) {
                        prettyErrorIndicatorPrefix.add("    <-- " + indicateMessage);
                    }
                    prettyErrorIndicatorPrefix.add("\n");
                    prettyErrorIndicatorSuffix.add("]");
                    for (int blanks = 1; blanks <= i + 1; blanks++) {
                        prettyErrorIndicatorPrefix.add("  ");
                        if (blanks > 1) {
                            prettyErrorIndicatorSuffix.add("  ");
                        }
                    }
                    prettyErrorIndicatorSuffix.add("\n");
                }
            }
        }
        prettyErrorIndicatorPrefix.add("<Expected>");
        if (level == paths.size() - 1) {
            prettyErrorIndicatorPrefix.add("    <-- " + indicateMessage);
        }
        StringBuilder prettyErrorIndicatorBuilder = new StringBuilder();
        for (String prefix : prettyErrorIndicatorPrefix) {
            prettyErrorIndicatorBuilder.append(prefix);
        }
        for (int i = prettyErrorIndicatorSuffix.size() - 1; i >= 0; i--) {
            prettyErrorIndicatorBuilder.append(prettyErrorIndicatorSuffix.get(i));
        }

        StringBuilder errorMessageBuildler = new StringBuilder(errorCode.toString())
                .append(": ")
                .append(messagePrefix != null ? messagePrefix : "")
                .append(pathErrorOccurredBuilder)
                .append(messageSuffix != null ? messageSuffix : "")
                .append('\n')
                .append(prettyErrorIndicatorBuilder);

        ElementVisitException exception = new ElementVisitException(errorMessageBuildler.toString(), cause);
        exception.setErrorCode(errorCode);
        exception.setPathErrorOccurred(pathErrorOccurredBuilder.toString());
        exception.setPathYouExpected(pathYouExpectedBuilder.toString());
        exception.setPrettyErrorIndicator(prettyErrorIndicatorBuilder.toString());
        return exception;
    }

    private void _handleVisitException(ErrorCode errorCode, Throwable cause, boolean suppressAll, 
                                       int level, int collectionIndex, String messagePrefix, String messageSuffix, String indicateMessage) {
        if (suppressAll || suppressedErrorCategories.contains(errorCode.getErrorCategory()) || suppressedErrorCodes.contains(errorCode)) {
            // suppress error
            return;
        }
        ElementVisitException exception = _buildVisitException(errorCode, cause, level, collectionIndex, messagePrefix, messageSuffix, indicateMessage);
        exceptionHandler.accept(exception);
    }

    private Object _tryCreateExpectedElementIfAbsent(Object element, Map parent, String key) {
        if (element != null) {
            return element;
        }
        if (supplyIfElementAbsent != null) {
            element = supplyIfElementAbsent.get();
            if (element == null) {
                throw new RuntimeException("The 'Supplier' returns null.");
            }
            parent.put(key, element);
        }
        return element;
    }

    private Object _tryCreateParentElementIfAbsent(Object element, Map parent, String key) {
        if (element != null) {
            return element;
        }
        // Determine whether to create the parent element based on whether 'supplyIfElementAbsent' exists.
        if (supplyIfElementAbsent != null) {
            // Automatic element creation only supports Map (Oneway visit)
            element = new LinkedHashMap<>();
            parent.put(key, element);
        }
        return element;
    }

    private void _visitRoot(Class expectedElementType, Function elementHandler, boolean replaceMode, boolean deleteMode, boolean suppressAll) {
        if (root == null) {
            _handleVisitException(ErrorCode.MISSING_ROOT_ELEMENT, null, suppressAll, -1, -1, "The '", "' element is null", "Null");
            return;
        }
        if (CheckUtils.isEmpty(paths)) {
            // Imposable! The reason is that if the ElementVisitor does not call the child or children method, it cannot "retrieve" elements—and without retrieving elements, it is impossible to reach this part of the code.
            throw new IllegalStateException("paths is empty");
        }
        _visitNonRoot(root, 0, expectedElementType, elementHandler, replaceMode, deleteMode, suppressAll);
    }

    private void _visitNonRoot(Object parentElement, int level, Class expectedElementType, Function elementHandler, boolean replaceMode, boolean deleteMode, boolean suppressAll) {
        if (paths.get(level).parentType == ParentType.MAP) {
            _visitNonRoot_map(parentElement, level, expectedElementType, elementHandler, replaceMode, deleteMode, suppressAll);
        } else {
            _visitNonRoot_collection(parentElement, level, expectedElementType, elementHandler, replaceMode, deleteMode, suppressAll);
        }
    }

    private void _visitNonRoot_map(Object parentElement, int level, Class expectedElementType, Function elementHandler, boolean replaceMode, boolean deleteMode, boolean suppressAll) {
        Path path = paths.get(level);
        if (!(parentElement instanceof Map)) {
            if (level == 0) {
                _handleVisitException(ErrorCode.ROOT_ELEMENT_TYPE_MISMATCH, null, suppressAll, level - 1, -1,
                        "Root element '", "' is not an instance of Map (it's " + parentElement.getClass().getName() +
                                "), unable to get child '" + path.key + "' from it", "Not Map");
            } else {
                _handleVisitException(ErrorCode.PARENT_ELEMENT_TYPE_MISMATCH, null, suppressAll, level - 1, -1,
                        "Parent element '", "' is not an instance of Map (it's " + parentElement.getClass().getName() +
                                "), unable to get child '" + path.key + "' from it", "Not Map");
            }
            return;
        }

        Object element = ((Map) parentElement).get(path.key);

        if (level >= paths.size() - 1) {
            // expected element

            try {
                element = _tryCreateExpectedElementIfAbsent(element, (Map) parentElement, path.key);
            } catch (Throwable t) {
                _handleVisitException(ErrorCode.CREATE_EXPECTED_ELEMENT_FAILED, t, suppressAll, level, -1,
                        "Failed to create expected element '",
                        "' from 'Supplier'. The 'Supplier' was set via the createIfAbsent(Supplier) method", "Create Failed");
                return;
            }
            if (element == null) {
                _handleVisitException(ErrorCode.MISSING_EXPECTED_ELEMENT, null, suppressAll, level, -1,
                        "Expected element '", "' does not exist", "Not Exist");
                return;
            }
            if (expectedElementType != null) {
                if (!expectedElementType.isAssignableFrom(element.getClass())) {
                    _handleVisitException(ErrorCode.EXPECTED_ELEMENT_TYPE_MISMATCH, null, suppressAll, level, -1,
                            "Expected element '", "' does not match the type you expected '" +
                                    expectedElementType.getName() + "', it's " + element.getClass().getName(), "Not " + expectedElementType.getSimpleName());
                    return;
                }
            }

            // handle
            Object returnedElement = elementHandler.apply(element);

            // delete/remove
            if (deleteMode) {
                ((Map) parentElement).remove(path.key);
                return;
            }
            // replace
            if (replaceMode) {
                ((Map) parentElement).put(path.key, returnedElement);
                return;
            }

        } else {
            // parent element

            element = _tryCreateParentElementIfAbsent(element, (Map) parentElement, path.key);
            if (element == null) {
                _handleVisitException(ErrorCode.MISSING_PARENT_ELEMENT, null, suppressAll, level, -1,
                        "Parent element '", "' does not exist, can not get child or children from it", "Not Exist");
                return;
            }

            // visit next path
            _visitNonRoot(element, level + 1, expectedElementType, elementHandler, replaceMode, deleteMode, suppressAll);

        }
    }

    private void _visitNonRoot_collection(Object parentElement, int level, Class expectedElementType, Function elementHandler, boolean replaceMode, boolean deleteMode, boolean suppressAll) {
        Path path = paths.get(level);
        if (!(parentElement instanceof Collection)) {
            if (level == 0) {
                _handleVisitException(ErrorCode.ROOT_ELEMENT_TYPE_MISMATCH, null, suppressAll, level - 1, -1,
                        "Root element '", "' is not an instance of Collection (it's " + parentElement.getClass().getName() +
                                "), unable to get children from it", "Not Collection");
            } else {
                _handleVisitException(ErrorCode.PARENT_ELEMENT_TYPE_MISMATCH, null, suppressAll, level - 1, -1,
                        "Parent element '", "' is not an instance of Collection (it's " + parentElement.getClass().getName() +
                                "), unable to get children from it", "Not Collection");
            }
            return;
        }

        Collection<Object> elements = (Collection) parentElement;

        if (level >= paths.size() - 1) {
            // expected element

            if (elements.isEmpty()) {
                _handleVisitException(ErrorCode.MISSING_EXPECTED_ELEMENT, null, suppressAll, level, -1,
                        "Expected element '", "' does not exist", "Not Exist");
                return;
            }

            List<Object> replacedElements = null;

            int i = -1;
            for (Object element : elements) {
                i++;

                if (expectedElementType != null && element != null) {
                    if (!expectedElementType.isAssignableFrom(element.getClass())) {
                        _handleVisitException(ErrorCode.EXPECTED_ELEMENT_TYPE_MISMATCH, null, suppressAll, level, i,
                                "Expected element '", "' does not match the type you expected '" +
                                        expectedElementType.getName() + "', it's " + element.getClass(), "Not " + expectedElementType.getSimpleName());
                        continue;
                    }
                }

                // handle
                Object returnedElement = elementHandler.apply(element);

                // delete/remove
                if (deleteMode) {
                    continue;
                }
                // replace
                if (replaceMode) {
                    if (replacedElements == null) {
                        replacedElements = new ArrayList(elements.size());
                    }
                    replacedElements.add(returnedElement);
                    continue;
                }
            }

            // delete mode
            if (deleteMode) {
                ((Collection) parentElement).clear();
                return;
            }

            // replace mode
            if (replacedElements != null) {
                ((Collection) parentElement).clear();
                ((Collection) parentElement).addAll(replacedElements);
                return;
            }

        } else {
            // parent element

            if (elements.isEmpty()) {
                _handleVisitException(ErrorCode.MISSING_PARENT_ELEMENT, null, suppressAll, level, -1,
                        "Parent element '", "' does not exist, can not get child or children from it", "Not Exist (Parent Collection is empty)");
                return;
            }

            int i = -1;
            for (Object element : elements) {
                i++;

                if (element == null) {
                    _handleVisitException(ErrorCode.MISSING_PARENT_ELEMENT, null, suppressAll, level, i,
                            "Parent element '", "' is null, can not get child or children from it", "Null");
                    continue;
                }

                // visit next path
                _visitNonRoot(element, level + 1, expectedElementType, elementHandler, replaceMode, deleteMode, suppressAll);

            }

        }
    }

    private void _forEach_consumeAs(Class expectedElementType, Consumer elementConsumer) {
        _checkElementConsumer(elementConsumer);
        _visitRoot(expectedElementType, e -> {
            elementConsumer.accept(e);
            return e;
        }, false, false, false);
    }

    private void _forEach_replaceAs(Class expectedElementType, Function elementReplacer) {
        _checkElementReplacer(elementReplacer);
        _visitRoot(expectedElementType, elementReplacer, true, false, false);
    }

    private void _forEach_delete() {
        _visitRoot(null, e -> e, false, true, true);
    }

    private List _getAllAs(Class expectedElementType) {
        ArrayList result = new ArrayList();
        _visitRoot(expectedElementType, e -> {
            result.add(e);
            return e;
        }, false, false, false);
        return result;
    }

    private List _removeAllAs(Class expectedElementType) {
        ArrayList result = new ArrayList();
        _visitRoot(expectedElementType, e -> {
            result.add(e);
            return e;
        }, false, true, false);
        return result;
    }


    // Visitors /////////////////////////////////////////////////////////////////////////////////////////////////////


    public class BasicVisitor {

        private final ForEachVisitor forEachVisitor = new ForEachVisitor();

        private BasicVisitor() {
        }

         /**
          * 压制(忽略)指定的错误类别.
          * 注意, ExceptionHandler将无法接收到被压制(忽略)的异常. 
          * 重复调用此方法设置会覆盖原来设置的ErrorCategory. 
          * @param errorCategory 需要忽略的错误类别, 可配置多个
          */
        public BasicVisitor suppressErrorCategories(ErrorCategory... errorCategory) {
            // 注意, 会覆盖原配置
            ElementVisitor.this.suppressedErrorCategories = new HashSet<>(Arrays.asList(errorCategory));
            return this;
        }

        /**
         * 压制(忽略)指定的错误码.
         * 注意, ExceptionHandler将无法接收到被压制(忽略)的异常.
         * 重复调用此方法设置会覆盖原来设置的ErrorCode.
         * @param errorCodes 需要忽略的错误码, 可配置多个
         */
        public BasicVisitor suppressErrorCodes(ErrorCode... errorCodes) {
            // 注意, 会覆盖原配置
            ElementVisitor.this.suppressedErrorCodes = new HashSet<>(Arrays.asList(errorCodes));
            return this;
        }

        /**
         * <p>设置异常处理器, 默认为: e -> {throw e;} 即一律抛出异常.</p>
         * <p>注意, 如果exceptionHandler中不抛出异常, get/remove/forEach方法就不会抛出异常了, 返回的元素可能为null或空集合.</p>
         * <p>注意, 使用suppressErrorCategories和suppressErrorCodes压制(忽略)的异常将不会被exceptionHandler接收.</p>
         * <p>示范一: 只打印日志, 不抛出异常 (返回的元素可能为null或空集合)</p>
         * <pre>
         * e -> {
         *      logger.error("Error occurred when visiting element", e);
         * }
         * </pre>
         * <p>示范二: 数据缺失类的错误只打印日志, 不抛出异常(返回的元素可能为null或空集合); 其他错误抛出异常</p>
         * <pre>
         * e -> {
         *      switch (e.getErrorCategory()) {
         *          case DATA_MISSING:
         *              logger.error("Error occurred when visiting element", e);
         *              break;
         *          default:
         *              throw e;
         *      }
         * }
         * </pre>
         * <p>示范三: 封装成其他异常抛出</p>
         * <pre>
         * e -> {
         *      throw new ServiceException("Error occurred when visiting element", e);
         * }
         * </pre>
         *
         * @param exceptionHandler 异常处理器, 默认为: e -> {throw e;}
         * @throws IllegalArgumentException exceptionHandler为空
         */
        public BasicVisitor exceptionHandler(Consumer<ElementVisitException> exceptionHandler) throws IllegalArgumentException {
            _checkExceptionHandler(exceptionHandler);
            ElementVisitor.this.exceptionHandler = exceptionHandler;
            return basicVisitor;
        }

        /**
         * 遍历所有你想访问的元素, 使用Lambda表达式处理它们
         */
        public ForEachVisitor forEach() {
            return forEachVisitor;
        }

        public class ForEachVisitor {

            /**
             * 遍历所有你想访问的元素, 使用Lambda表达式(或Consumer)接收并处理它们, 接收元素类型为Map
             * @param elementConsumer 处理每一个你想访问的元素
             * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
             * @throws IllegalArgumentException elementConsumer为空
             */
            public <K, V> void consumeAsMap(Consumer<Map<K, V>> elementConsumer) throws ElementVisitException, IllegalArgumentException {
                _forEach_consumeAs(Map.class, elementConsumer);
            }

            /**
             * 遍历所有你想访问的元素, 使用Lambda表达式(或Consumer)接收并处理它们, 接收元素类型为List
             * @param elementConsumer 处理每一个你想访问的元素
             * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
             * @throws IllegalArgumentException elementConsumer为空
             */
            public <E> void consumeAsList(Consumer<List<E>> elementConsumer) throws ElementVisitException, IllegalArgumentException {
                _forEach_consumeAs(List.class, elementConsumer);
            }

            /**
             * 遍历所有你想访问的元素, 使用Lambda表达式(或Consumer)接收并处理它们, 接收元素类型为Set
             * @param elementConsumer 处理每一个你想访问的元素
             * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
             * @throws IllegalArgumentException elementConsumer为空
             */
            public <E> void consumeAsSet(Consumer<Set<E>> elementConsumer) throws ElementVisitException, IllegalArgumentException {
                _forEach_consumeAs(Set.class, elementConsumer);
            }

            /**
             * 遍历所有你想访问的元素, 使用Lambda表达式(或Consumer)接收并处理它们, 接收元素类型由expectedElementType指定
             * @param expectedElementType 你想访问的元素的类型, 如果是Map/List/Set, 请用consumeAsMap/consumeAsList/consumeAsSet
             * @param elementConsumer 处理每一个你想访问的元素
             * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
             * @throws IllegalArgumentException elementConsumer为空
             */
            public <E> void consumeAs(Class<E> expectedElementType, Consumer<E> elementConsumer) throws ElementVisitException, IllegalArgumentException {
                _forEach_consumeAs(expectedElementType, elementConsumer);
            }

            /**
             * 转换所有你想访问的元素, 使用Lambda表达式(或Function)接收并返回转换后的对象, 接收元素类型为Map(入参类型Map, 出参类型任意)
             * @param elementReplacer 接收每一个你想访问的元素并返回转换后的对象
             * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
             * @throws IllegalArgumentException elementReplacer为空
             */
            public <K, V> void replaceAsMap(Function<Map<K, V>, Object> elementReplacer) throws ElementVisitException, IllegalArgumentException {
                _forEach_replaceAs(Map.class, elementReplacer);
            }

            /**
             * 转换所有你想访问的元素, 使用Lambda表达式(或Function)接收并返回转换后的对象, 接收元素类型为List(入参类型List, 出参类型任意)
             * @param elementReplacer 接收每一个你想访问的元素并返回转换后的对象
             * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
             * @throws IllegalArgumentException elementReplacer为空
             */
            public <E> void replaceAsList(Function<List<E>, Object> elementReplacer) throws ElementVisitException, IllegalArgumentException {
                _forEach_replaceAs(List.class, elementReplacer);
            }

            /**
             * 转换所有你想访问的元素, 使用Lambda表达式(或Function)接收并返回转换后的对象, 接收元素类型为Set(入参类型Set, 出参类型任意)
             * @param elementReplacer 接收每一个你想访问的元素并返回转换后的对象
             * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
             * @throws IllegalArgumentException elementReplacer为空
             */
            public <E> void replaceAsSet(Function<Set<E>, Object> elementReplacer) throws ElementVisitException, IllegalArgumentException {
                _forEach_replaceAs(Set.class, elementReplacer);
            }

            /**
             * 转换所有你想访问的元素, 使用Lambda表达式(或Function)接收并返回转换后的对象, 接收元素类型由expectedElementType指定(入参类型expectedElementType, 出参类型任意)
             * @param expectedElementType 你想访问的元素的类型, 如果是Map/List/Set, 请用replaceAsMap/replaceAsList/replaceAsSet
             * @param elementReplacer 接收每一个你想访问的元素并返回转换后的对象
             * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
             * @throws IllegalArgumentException elementReplacer为空
             */
            public <E> void replaceAs(Class<E> expectedElementType, Function<E, Object> elementReplacer) throws ElementVisitException, IllegalArgumentException {
                _forEach_replaceAs(expectedElementType, elementReplacer);
            }

            /**
             * 删除所有你想获得的元素, 该方法不会抛出ElementVisitException
             */
            public void delete() {
                _forEach_delete();
            }

        }

    }

    public class OnewayVisitor extends BasicVisitor {

        private OnewayVisitor() {
        }

         /**
          * 压制(忽略)指定的错误类别.
          * 注意, ExceptionHandler将无法接收到被压制(忽略)的异常. 
          * 重复调用此方法设置会覆盖原来设置的ErrorCategory. 
          * @param errorCategory 需要忽略的错误类别, 可配置多个
          */
        public OnewayVisitor suppressErrorCategories(ErrorCategory... errorCategory) {
            // 注意, 会覆盖原配置
            ElementVisitor.this.suppressedErrorCategories = new HashSet<>(Arrays.asList(errorCategory));
            return this;
        }

        /**
         * 压制(忽略)指定的错误码.
         * 注意, ExceptionHandler将无法接收到被压制(忽略)的异常.
         * 重复调用此方法设置会覆盖原来设置的ErrorCode.
         * @param errorCodes 需要忽略的错误码, 可配置多个
         */
        public OnewayVisitor suppressErrorCodes(ErrorCode... errorCodes) {
            // 注意, 会覆盖原配置
            ElementVisitor.this.suppressedErrorCodes = new HashSet<>(Arrays.asList(errorCodes));
            return this;
        }

        /**
         * <p>设置异常处理器, 默认为: e -> {throw e;} 即一律抛出异常.</p>
         * <p>注意, 如果exceptionHandler中不抛出异常, get/remove/forEach方法就不会抛出异常了, 返回的元素可能为null或空集合.</p>
         * <p>注意, 使用suppressErrorCategories和suppressErrorCodes压制(忽略)的异常将不会被exceptionHandler接收.</p>
         * <p>示范一: 只打印日志, 不抛出异常 (返回的元素可能为null或空集合)</p>
         * <pre>
         * e -> {
         *      logger.error("Error occurred when visiting element", e);
         * }
         * </pre>
         * <p>示范二: 数据缺失类的错误只打印日志, 不抛出异常(返回的元素可能为null或空集合); 其他错误抛出异常</p>
         * <pre>
         * e -> {
         *      switch (e.getErrorCategory()) {
         *          case DATA_MISSING:
         *              logger.error("Error occurred when visiting element", e);
         *              break;
         *          default:
         *              throw e;
         *      }
         * }
         * </pre>
         * <p>示范三: 封装成其他异常抛出</p>
         * <pre>
         * e -> {
         *      throw new ServiceException("Error occurred when visiting element", e);
         * }
         * </pre>
         *
         * @param exceptionHandler 异常处理器, 默认为: e -> {throw e;}
         * @throws IllegalArgumentException exceptionHandler为空
         */
        public OnewayVisitor exceptionHandler(Consumer<ElementVisitException> exceptionHandler) throws IllegalArgumentException {
            _checkExceptionHandler(exceptionHandler);
            ElementVisitor.this.exceptionHandler = exceptionHandler;
            return onewayVisitor;
        }

        /**
         * [只支持Map嵌套Map的集合, 若访问路径中存在Collection则不支持]
         * 若访问路径中的中间元素不存在, 则自动创建中间元素(LinkedHashMap).
         * 若你想要获取的元素不存在, 则调用本方法入参'supplyIfElementAbsent'创建.
         * @param supplyIfElementAbsent 用于创建你想访问的元素实例 (如果不存在)
         */
        public OnewayVisitor createIfAbsent(Supplier<Object> supplyIfElementAbsent) {
            ElementVisitor.this.supplyIfElementAbsent = supplyIfElementAbsent;
            return onewayVisitor;
        }

        /**
         * 获取你想访问的元素(一个), 元素类型为Map
         * @return 你想访问的元素(一个), 默认不为空; 但如果你压制(忽略)了指定异常, 或者自定义ExceptionHandler中未抛出异常, 则可能返回null;
         * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
         */
        public <K, V> Map<K, V> getAsMap() throws ElementVisitException {
            Collection<Map<K, V>> elements = _getAllAs(Map.class);
            for (Map<K, V> element : elements) {
                return element;
            }
            return null;
        }

        /**
         * 获取你想访问的元素(一个), 元素类型为List
         * @return 你想访问的元素(一个), 默认不为空; 但如果你压制(忽略)了指定异常, 或者自定义ExceptionHandler中未抛出异常, 则可能返回null;
         * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
         */
        public <E> List<E> getAsList() throws ElementVisitException {
            Collection<List<E>> elements = _getAllAs(List.class);
            for (List<E> element : elements) {
                return element;
            }
            return null;
        }

        /**
         * 获取你想访问的元素(一个), 元素类型为Set
         * @return 你想访问的元素(一个), 默认不为空; 但如果你压制(忽略)了指定异常, 或者自定义ExceptionHandler中未抛出异常, 则可能返回null;
         * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
         */
        public <E> Set<E> getAsSet() throws ElementVisitException {
            Collection<Set<E>> elements = _getAllAs(Set.class);
            for (Set<E> element : elements) {
                return element;
            }
            return null;
        }

        /**
         * 获取你想访问的元素(一个), 元素类型由expectedElementType指定
         * @param expectedElementType 你想访问的元素的类型, 如果是Map/List/Set, 请用getAsMap/getAsList/getAsSet
         * @return 你想访问的元素(一个), 默认不为空; 但如果你压制(忽略)了指定异常, 或者自定义ExceptionHandler中未抛出异常, 则可能返回null;
         * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
         */
        public <E> E getAs(Class<E> expectedElementType) throws ElementVisitException {
            Collection<E> elements = _getAllAs(expectedElementType);
            for (E element : elements) {
                return element;
            }
            return null;
        }

        /**
         * 移除你想访问的元素(一个), 元素类型为Map
         * @return 你想访问的元素(一个), 默认不为空; 但如果你压制(忽略)了指定异常, 或者自定义ExceptionHandler中未抛出异常, 则可能返回null;
         * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
         */
        public <K, V> Map<K, V> removeAsMap() throws ElementVisitException {
            Collection<Map<K, V>> elements = _removeAllAs(Map.class);
            for (Map<K, V> element : elements) {
                return element;
            }
            return null;
        }

        /**
         * 移除你想访问的元素(一个), 元素类型为List
         * @return 你想访问的元素(一个), 默认不为空; 但如果你压制(忽略)了指定异常, 或者自定义ExceptionHandler中未抛出异常, 则可能返回null;
         * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
         */
        public <E> List<E> removeAsList() throws ElementVisitException {
            Collection<List<E>> elements = _removeAllAs(List.class);
            for (List<E> element : elements) {
                return element;
            }
            return null;
        }

        /**
         * 移除你想访问的元素(一个), 元素类型为Set
         * @return 你想访问的元素(一个), 默认不为空; 但如果你压制(忽略)了指定异常, 或者自定义ExceptionHandler中未抛出异常, 则可能返回null;
         * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
         */
        public <E> Set<E> removeAsSet() throws ElementVisitException {
            Collection<Set<E>> elements = _removeAllAs(Set.class);
            for (Set<E> element : elements) {
                return element;
            }
            return null;
        }

        /**
         * 移除你想访问的元素(一个), 元素类型由expectedElementType指定
         * @param expectedElementType 你想访问的元素的类型, 如果是Map/List/Set, 请用removeAsMap/removeAsList/removeAsSet
         * @return 你想访问的元素(一个), 默认不为空; 但如果你压制(忽略)了指定异常, 或者自定义ExceptionHandler中未抛出异常, 则可能返回null;
         * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
         */
        public <E> E removeAs(Class<E> expectedElementType) throws ElementVisitException {
            Collection<E> elements = _removeAllAs(expectedElementType);
            for (E element : elements) {
                return element;
            }
            return null;
        }

    }

    public class OnewayPathPlanner extends OnewayVisitor {

        private OnewayPathPlanner() {
        }

        /**
         * 路径配置, 访问Map的子元素
         * @param key key
         */
        public OnewayPathPlanner child(String key) {
            _addPath(key);
            return onewayPathPlanner;
        }

        /**
         * 路径配置, 访问Collection的子元素 (遍历)
         */
        public MultiwayPathPlanner children() {
            _addPath();
            return multiwayPathPlanner;
        }

         /**
          * 压制(忽略)指定的错误类别.
          * 注意, ExceptionHandler将无法接收到被压制(忽略)的异常. 
          * 重复调用此方法设置会覆盖原来设置的ErrorCategory. 
          * @param errorCategory 需要忽略的错误类别, 可配置多个
          */
        public OnewayPathPlanner suppressErrorCategories(ErrorCategory... errorCategory) {
            // 注意, 会覆盖原配置
            ElementVisitor.this.suppressedErrorCategories = new HashSet<>(Arrays.asList(errorCategory));
            return this;
        }

        /**
         * 压制(忽略)指定的错误码.
         * 注意, ExceptionHandler将无法接收到被压制(忽略)的异常.
         * 重复调用此方法设置会覆盖原来设置的ErrorCode.
         * @param errorCodes 需要忽略的错误码, 可配置多个
         */
        public OnewayPathPlanner suppressErrorCodes(ErrorCode... errorCodes) {
            // 注意, 会覆盖原配置
            ElementVisitor.this.suppressedErrorCodes = new HashSet<>(Arrays.asList(errorCodes));
            return this;
        }

        /**
         * <p>设置异常处理器, 默认为: e -> {throw e;} 即一律抛出异常.</p>
         * <p>注意, 如果exceptionHandler中不抛出异常, get/remove/forEach方法就不会抛出异常了, 返回的元素可能为null或空集合.</p>
         * <p>注意, 使用suppressErrorCategories和suppressErrorCodes压制(忽略)的异常将不会被exceptionHandler接收.</p>
         * <p>示范一: 只打印日志, 不抛出异常 (返回的元素可能为null或空集合)</p>
         * <pre>
         * e -> {
         *      logger.error("Error occurred when visiting element", e);
         * }
         * </pre>
         * <p>示范二: 数据缺失类的错误只打印日志, 不抛出异常(返回的元素可能为null或空集合); 其他错误抛出异常</p>
         * <pre>
         * e -> {
         *      switch (e.getErrorCategory()) {
         *          case DATA_MISSING:
         *              logger.error("Error occurred when visiting element", e);
         *              break;
         *          default:
         *              throw e;
         *      }
         * }
         * </pre>
         * <p>示范三: 封装成其他异常抛出</p>
         * <pre>
         * e -> {
         *      throw new ServiceException("Error occurred when visiting element", e);
         * }
         * </pre>
         *
         * @param exceptionHandler 异常处理器, 默认为: e -> {throw e;}
         * @throws IllegalArgumentException exceptionHandler为空
         */
        public OnewayPathPlanner exceptionHandler(Consumer<ElementVisitException> exceptionHandler) throws IllegalArgumentException {
            _checkExceptionHandler(exceptionHandler);
            ElementVisitor.this.exceptionHandler = exceptionHandler;
            return onewayPathPlanner;
        }

    }

    public class MultiwayVisitor extends BasicVisitor {

        private MultiwayVisitor() {
        }

         /**
          * 压制(忽略)指定的错误类别.
          * 注意, ExceptionHandler将无法接收到被压制(忽略)的异常. 
          * 重复调用此方法设置会覆盖原来设置的ErrorCategory. 
          * @param errorCategory 需要忽略的错误类别, 可配置多个
          */
        public MultiwayVisitor suppressErrorCategories(ErrorCategory... errorCategory) {
            // 注意, 会覆盖原配置
            ElementVisitor.this.suppressedErrorCategories = new HashSet<>(Arrays.asList(errorCategory));
            return this;
        }

        /**
         * 压制(忽略)指定的错误码.
         * 注意, ExceptionHandler将无法接收到被压制(忽略)的异常.
         * 重复调用此方法设置会覆盖原来设置的ErrorCode.
         * @param errorCodes 需要忽略的错误码, 可配置多个
         */
        public MultiwayVisitor suppressErrorCodes(ErrorCode... errorCodes) {
            // 注意, 会覆盖原配置
            ElementVisitor.this.suppressedErrorCodes = new HashSet<>(Arrays.asList(errorCodes));
            return this;
        }

        /**
         * <p>设置异常处理器, 默认为: e -> {throw e;} 即一律抛出异常.</p>
         * <p>注意, 如果exceptionHandler中不抛出异常, get/remove/forEach方法就不会抛出异常了, 返回的元素可能为null或空集合.</p>
         * <p>注意, 使用suppressErrorCategories和suppressErrorCodes压制(忽略)的异常将不会被exceptionHandler接收.</p>
         * <p>示范一: 只打印日志, 不抛出异常 (返回的元素可能为null或空集合)</p>
         * <pre>
         * e -> {
         *      logger.error("Error occurred when visiting element", e);
         * }
         * </pre>
         * <p>示范二: 数据缺失类的错误只打印日志, 不抛出异常(返回的元素可能为null或空集合); 其他错误抛出异常</p>
         * <pre>
         * e -> {
         *      switch (e.getErrorCategory()) {
         *          case DATA_MISSING:
         *              logger.error("Error occurred when visiting element", e);
         *              break;
         *          default:
         *              throw e;
         *      }
         * }
         * </pre>
         * <p>示范三: 封装成其他异常抛出</p>
         * <pre>
         * e -> {
         *      throw new ServiceException("Error occurred when visiting element", e);
         * }
         * </pre>
         *
         * @param exceptionHandler 异常处理器, 默认为: e -> {throw e;}
         * @throws IllegalArgumentException exceptionHandler为空
         */
        public MultiwayVisitor exceptionHandler(Consumer<ElementVisitException> exceptionHandler) throws IllegalArgumentException {
            _checkExceptionHandler(exceptionHandler);
            ElementVisitor.this.exceptionHandler = exceptionHandler;
            return multiwayVisitor;
        }

        /**
         * <pre><code>
         * 获取你想访问的元素(多个,List<Map>), 单个元素的类型为Map
         * </code></pre>
         * @return 你想访问的元素(多个,List), 默认不为空; 但如果你压制(忽略)了指定异常, 或者自定义ExceptionHandler中未抛出异常, 则可能返回空List;
         * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
         */
        public <K, V> List<Map<K, V>> getAllAsMap() throws ElementVisitException {
            return _getAllAs(Map.class);
        }

        /**
         * <pre><code>
         * 获取你想访问的元素(多个,List<List>), 单个元素的类型为List
         * </code></pre>
         * @return 你想访问的元素(多个,List), 默认不为空; 但如果你压制(忽略)了指定异常, 或者自定义ExceptionHandler中未抛出异常, 则可能返回空List;
         * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
         */
        public <E> List<List<E>> getAllAsList() throws ElementVisitException {
            return _getAllAs(List.class);
        }

        /**
         * <pre><code>
         * 获取你想访问的元素(多个,List<Set>), 单个元素的类型为Set
         * </code></pre>
         * @return 你想访问的元素(多个,List), 默认不为空; 但如果你压制(忽略)了指定异常, 或者自定义ExceptionHandler中未抛出异常, 则可能返回空List;
         * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
         */
        public <E> List<Set<E>> getAllAsSet() throws ElementVisitException {
            return _getAllAs(Set.class);
        }

        /**
         * <pre><code>
         * 获取你想访问的元素(多个,List<E>), 单个元素的类型由expectedElementType指定
         * </code></pre>
         * @param expectedElementType 你想访问的元素的类型, 如果是Map/List/Set, 请用getAllAsMap/getAllAsList/getAllAsSet
         * @return 你想访问的元素(多个,List), 默认不为空; 但如果你压制(忽略)了指定异常, 或者自定义ExceptionHandler中未抛出异常, 则可能返回空List;
         * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
         */
        public <E> List<E> getAllAs(Class<E> expectedElementType) throws ElementVisitException {
            return _getAllAs(expectedElementType);
        }

        /**
         * <pre><code>
         * 移除你想访问的元素(多个,List<Map>), 单个元素的类型为Map
         * </code></pre>
         * @return 你想访问的元素(多个,List), 默认不为空; 但如果你压制(忽略)了指定异常, 或者自定义ExceptionHandler中未抛出异常, 则可能返回空List;
         * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
         */
        public <K, V> List<Map<K, V>> removeAllAsMap() throws ElementVisitException {
            return _removeAllAs(Map.class);
        }

        /**
         * <pre><code>
         * 移除你想访问的元素(多个,List<List>), 单个元素的类型为List
         * </code></pre>
         * @return 你想访问的元素(多个,List), 默认不为空; 但如果你压制(忽略)了指定异常, 或者自定义ExceptionHandler中未抛出异常, 则可能返回空List;
         * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
         */
        public <E> List<List<E>> removeAllAsList() throws ElementVisitException {
            return _removeAllAs(List.class);
        }

        /**
         * <pre><code>
         * 移除你想访问的元素(多个,List<Set>), 单个元素的类型为Set
         * </code></pre>
         * @return 你想访问的元素(多个,List), 默认不为空; 但如果你压制(忽略)了指定异常, 或者自定义ExceptionHandler中未抛出异常, 则可能返回空List;
         * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
         */
        public <E> List<Set<E>> removeAllAsSet() throws ElementVisitException {
            return _removeAllAs(Set.class);
        }

        /**
         * <pre><code>
         * 移除你想访问的元素(多个,List<E>), 单个元素的类型由expectedElementType指定
         * </code></pre>
         * @param expectedElementType 你想访问的元素的类型, 如果是Map/List/Set, 请用removeAllAsMap/removeAllAsList/removeAllAsSet
         * @return 你想访问的元素(多个,List), 默认不为空; 但如果你压制(忽略)了指定异常, 或者自定义ExceptionHandler中未抛出异常, 则可能返回空List;
         * @throws ElementVisitException 元素访问异常, 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 这里就不会抛出ElementVisitException了
         */
        public <E> List<E> removeAllAs(Class<E> expectedElementType) throws ElementVisitException {
            return _removeAllAs(expectedElementType);
        }

    }

    public class MultiwayPathPlanner extends MultiwayVisitor {

        private MultiwayPathPlanner() {
        }

        /**
         * 路径配置, 访问Map的子元素
         * @param key key
         */
        public MultiwayPathPlanner child(String key) {
            // 注意: 这里返回multiwayPathPlanner不是写错了
            // 访问路径中只要出现过children(), 后续就一直是multiwayPathPlanner了
            _addPath(key);
            return multiwayPathPlanner;
        }

        /**
         * 路径配置, 访问Collection的子元素 (遍历)
         */
        public MultiwayPathPlanner children() {
            _addPath();
            return multiwayPathPlanner;
        }

         /**
          * 压制(忽略)指定的错误类别.
          * 注意, ExceptionHandler将无法接收到被压制(忽略)的异常. 
          * 重复调用此方法设置会覆盖原来设置的ErrorCategory. 
          * @param errorCategory 需要忽略的错误类别, 可配置多个
          */
        public MultiwayPathPlanner suppressErrorCategories(ErrorCategory... errorCategory) {
            // 注意, 会覆盖原配置
            ElementVisitor.this.suppressedErrorCategories = new HashSet<>(Arrays.asList(errorCategory));
            return this;
        }

        /**
         * 压制(忽略)指定的错误码.
         * 注意, ExceptionHandler将无法接收到被压制(忽略)的异常.
         * 重复调用此方法设置会覆盖原来设置的ErrorCode.
         * @param errorCodes 需要忽略的错误码, 可配置多个
         */
        public MultiwayPathPlanner suppressErrorCodes(ErrorCode... errorCodes) {
            // 注意, 会覆盖原配置
            ElementVisitor.this.suppressedErrorCodes = new HashSet<>(Arrays.asList(errorCodes));
            return this;
        }

        /**
         * <p>设置异常处理器, 默认为: e -> {throw e;} 即一律抛出异常.</p>
         * <p>注意, 如果exceptionHandler中不抛出异常, get/remove/forEach方法就不会抛出异常了, 返回的元素可能为null或空集合.</p>
         * <p>注意, 使用suppressErrorCategories和suppressErrorCodes压制(忽略)的异常将不会被exceptionHandler接收.</p>
         * <p>示范一: 只打印日志, 不抛出异常 (返回的元素可能为null或空集合)</p>
         * <pre>
         * e -> {
         *      logger.error("Error occurred when visiting element", e);
         * }
         * </pre>
         * <p>示范二: 数据缺失类的错误只打印日志, 不抛出异常(返回的元素可能为null或空集合); 其他错误抛出异常</p>
         * <pre>
         * e -> {
         *      switch (e.getErrorCategory()) {
         *          case DATA_MISSING:
         *              logger.error("Error occurred when visiting element", e);
         *              break;
         *          default:
         *              throw e;
         *      }
         * }
         * </pre>
         * <p>示范三: 封装成其他异常抛出</p>
         * <pre>
         * e -> {
         *      throw new ServiceException("Error occurred when visiting element", e);
         * }
         * </pre>
         *
         * @param exceptionHandler 异常处理器, 默认为: e -> {throw e;}
         * @throws IllegalArgumentException exceptionHandler为空
         */
        public MultiwayPathPlanner exceptionHandler(Consumer<ElementVisitException> exceptionHandler) throws IllegalArgumentException {
            _checkExceptionHandler(exceptionHandler);
            ElementVisitor.this.exceptionHandler = exceptionHandler;
            return multiwayPathPlanner;
        }

    }


    // private class ////////////////////////////////////////////////////////////////////////////////////////////


    private static class Path {

        private final ParentType parentType;
        private final String key;

        public Path(ParentType parentType, String key) {
            this.parentType = parentType;
            this.key = key;
        }

    }

    private enum ParentType {

        MAP,
        COLLECTION,

    }


    // Error ////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * 错误类别
     */
    public enum ErrorCategory {

        /**
         * 数据缺失
         */
        DATA_MISSING,

        /**
         * 数据无效:
         * 路径中间元素(parent_element)不是所需的Map或Collection类型.
         * 你想访问的元素(expected_element)不是指定的类型(你需要的类型).
         */
        DATA_INVALID,

        /**
         * 编程错误:
         * 工具类用法错误.
         * 必要的方法入参缺失.
         * createIfAbsent的表达式创建对象(expected_element)失败(或为空)
         */
        PROGRAMMING_ERROR,

    }

    /**
     * 错误码
     */
    public enum ErrorCode {

        /**
         * 根元素(root_element)为空
         */
        MISSING_ROOT_ELEMENT(ErrorCategory.DATA_MISSING),
        /**
         * 路径中间元素(parent_element)为空
         */
        MISSING_PARENT_ELEMENT(ErrorCategory.DATA_MISSING),
        /**
         * 你想访问的元素(expected_element)为空
         */
        MISSING_EXPECTED_ELEMENT(ErrorCategory.DATA_MISSING),

        /**
         * 根元素(root_element)不是所需的Map或Collection类型.
         */
        ROOT_ELEMENT_TYPE_MISMATCH(ErrorCategory.DATA_INVALID),
        /**
         * 路径中间元素(parent_element)不是所需的Map或Collection类型.
         */
        PARENT_ELEMENT_TYPE_MISMATCH(ErrorCategory.DATA_INVALID),
        /**
         * 你想访问的元素(expected_element)不是指定的类型(你需要的类型).
         */
        EXPECTED_ELEMENT_TYPE_MISMATCH(ErrorCategory.DATA_INVALID),

        /**
         * [createIfAbsent] 创建你想访问的元素(expected_element)失败 (由createIfAbsent方法传入的表达式创建)
         */
        CREATE_EXPECTED_ELEMENT_FAILED(ErrorCategory.PROGRAMMING_ERROR),

        /**
         * 未定义的错误
         */
        UNDEFINED_ERROR(ErrorCategory.PROGRAMMING_ERROR),

        ;

        private final ErrorCategory errorCategory;

        ErrorCode(ErrorCategory errorCategory) {
            this.errorCategory = errorCategory;
        }

        @Override
        public String toString() {
            return errorCategory.toString() + '/' + super.toString();
        }

        public ErrorCategory getErrorCategory() {
            return errorCategory;
        }

    }

    /**
     * 元素访问异常. 如果异常被压制(忽略), 或者自定义ExceptionHandler中未抛出, 就不会抛出ElementVisitException了
     */
    public static class ElementVisitException extends RuntimeException {

        private static final long serialVersionUID = -9064567333501718644L;

        private ErrorCode errorCode;
        private String pathErrorOccurred;
        private String pathYouExpected;
        private String prettyErrorIndicator;

        public ElementVisitException() {
        }

        public ElementVisitException(String message) {
            super(message);
        }

        public ElementVisitException(String message, Throwable cause) {
            super(message, cause);
        }

        public ErrorCode getErrorCode() {
            return errorCode != null ? errorCode : ErrorCode.UNDEFINED_ERROR;
        }

        public void setErrorCode(ErrorCode errorCode) {
            this.errorCode = errorCode;
        }

        public ErrorCategory getErrorCategory() {
            return getErrorCode().getErrorCategory();
        }

        public String getPathErrorOccurred() {
            return pathErrorOccurred;
        }

        public void setPathErrorOccurred(String pathErrorOccurred) {
            this.pathErrorOccurred = pathErrorOccurred;
        }

        public String getPathYouExpected() {
            return pathYouExpected;
        }

        public void setPathYouExpected(String pathYouExpected) {
            this.pathYouExpected = pathYouExpected;
        }

        public String getPrettyErrorIndicator() {
            return prettyErrorIndicator;
        }

        public void setPrettyErrorIndicator(String prettyErrorIndicator) {
            this.prettyErrorIndicator = prettyErrorIndicator;
        }
        
    }

}
