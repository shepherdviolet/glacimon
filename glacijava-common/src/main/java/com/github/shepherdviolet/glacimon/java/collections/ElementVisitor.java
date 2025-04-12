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

    public static ElementVisitor of(Map root) {
        return new ElementVisitor(root);
    }

    public static ElementVisitor of(Collection root) {
        return new ElementVisitor(root);
    }


    // Public ////////////////////////////////////////////////////////////////////////////////////////////////////


    private static final Object DELETE_FLAG = new Object();

    private final Object root;
    private final List<Path> paths = new ArrayList<>();
    private Consumer<ElementVisitException> exceptionHandler;
    private Supplier<Object> supplyIfElementAbsent;

    private final BasicVisitor basicVisitor = new BasicVisitor();
    private final OnewayVisitor onewayVisitor = new OnewayVisitor();
    private final MultiwayVisitor multiwayVisitor = new MultiwayVisitor();
    private final OnewayPathPlanner onewayPathPlanner = new OnewayPathPlanner();
    private final MultiwayPathPlanner multiwayPathPlanner = new MultiwayPathPlanner();

    private ElementVisitor(Object root) {
        this.root = root;
    }

    public OnewayPathPlanner child(String key) {
        _addPath(key);
        return onewayPathPlanner;
    }

    public MultiwayPathPlanner children() {
        _addPath();
        return multiwayPathPlanner;
    }

    public ElementVisitor exceptionHandler(Consumer<ElementVisitException> exceptionHandler) {
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
            throw _buildArgumentException(ErrorCode.ELEMENT_CONSUMER_IS_NULL, "elementConsumer cannot be null");
        }
    }

    private void _checkElementReplacer(Function elementReplacer) {
        if (elementReplacer == null) {
            throw _buildArgumentException(ErrorCode.ELEMENT_REPLACER_IS_NULL, "elementReplacer cannot be null");
        }
    }

    private ElementVisitException _buildArgumentException(ErrorCode errorCode, String message) {
        ElementVisitException exception = new ElementVisitException(errorCode.toString() + ": " + message);
        exception.setErrorCode(errorCode);
        exception.setPathErrorOccurred("");
        exception.setPathYouExpected("");
        return exception;
    }

    private ElementVisitException _buildVisitException(ErrorCode errorCode, Throwable cause, int level, int collectionIndex, String message, String annotation) {
        StringBuilder pathYouExpectedBuilder = new StringBuilder("root");
        StringBuilder pathErrorOccurredBuilder = new StringBuilder("root");
        // paths cannot be empty
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

        StringBuilder msgBuilder = new StringBuilder(errorCode.toString())
                .append(": ")
                .append(message)
                .append(" [Occurred at: ")
                .append(pathErrorOccurredBuilder)
                .append(" <- ")
                .append(annotation != null ? annotation : "")
                .append("] [Expected element (path): ")
                .append(pathYouExpectedBuilder)
                .append("]");

        ElementVisitException exception = new ElementVisitException(msgBuilder.toString(), cause);
        exception.setErrorCode(errorCode);
        exception.setPathErrorOccurred(pathErrorOccurredBuilder.toString());
        exception.setPathYouExpected(pathYouExpectedBuilder.toString());
        return exception;
    }

    private void _handleVisitException(ErrorCode errorCode, Throwable cause, int level, int collectionIndex, String message, String annotation) {
        ElementVisitException exception = _buildVisitException(errorCode, cause, level, collectionIndex, message, annotation);
        if (exceptionHandler != null) {
            exceptionHandler.accept(exception);
        } else {
            throw exception;
        }
    }

    private Object _tryCreateExpectedElementIfAbsent(Object element) {
        if (element != null) {
            return element;
        }
        if (supplyIfElementAbsent != null) {
            element = supplyIfElementAbsent.get();
            if (element == null) {
                throw new RuntimeException("The expected element returned by Supplier 'supplyIfElementAbsent' is empty, " +
                        "the Supplier is set by method 'createIfAbsent'");
            }
        }
        return element;
    }

    private Object _tryCreateParentElementIfAbsent(Object element, Path nextPath) {
        if (element != null) {
            return element;
        }
        // Determine whether to create the parent element based on whether 'supplyIfElementAbsent' exists.
        if (supplyIfElementAbsent != null) {
            if (nextPath.parentType == ParentType.MAP) {
                element = new LinkedHashMap<>();
            } else {
                element = new ArrayList<>();
            }
        }
        return element;
    }

    private void _visit(Class expectedElementType, Function elementHandler) {
        if (root == null) {
            _handleVisitException(ErrorCode.MISSING_ROOT_ELEMENT, null, -1, -1, "Missing root element", "It's Null");
            return;
        }
        if (CheckUtils.isEmpty(paths)) {
            // Imposable! The reason is that if the ElementVisitor does not call the child or children method, it cannot "retrieve" elements—and without retrieving elements, it is impossible to reach this part of the code.
            throw new IllegalStateException("paths is empty");
        }
        _visit(root, 0, expectedElementType, elementHandler);
    }

    /**
     *
     * @param parentElement nonnull
     * @param level >= 0
     * @param expectedElementType nullable
     * @param elementHandler nonnull
     */
    private void _visit(Object parentElement, int level, Class expectedElementType, Function elementHandler) {
        List<Object> elements = new ArrayList<>();
        Path path = paths.get(level);

        if (path.parentType == ParentType.MAP) {
            if (!(parentElement instanceof Map)) {
                _handleVisitException(ErrorCode.PARENT_ELEMENT_TYPE_MISMATCH, null, level - 1, -1,
                        "Failed to resolve parent element: expected type 'java.util.Map', found '" +
                                parentElement.getClass().getName() + "'", "Not Map");
                return;
            }
            elements.add(((Map) parentElement).get(path.key));
        } else {
            if (!(parentElement instanceof Collection)) {
                _handleVisitException(ErrorCode.PARENT_ELEMENT_TYPE_MISMATCH, null, level - 1, -1,
                        "Failed to resolve parent element, expected a Collection(List/Set...) but found '" +
                                parentElement.getClass().getName(), "Not Collection");
                return;
            }
            elements.addAll(((Collection) parentElement));
        }

        for (int i = 0; i < elements.size() ; i++) {
            Object element = elements.get(i);

            if (level >= paths.size() - 1) {

                // expected element
                try {
                    element = _tryCreateExpectedElementIfAbsent(element);
                } catch (Throwable t) {
                    _handleVisitException(ErrorCode.CREATE_EXPECTED_ELEMENT_FAILED, t, level, i,
                            "Failed to create expected element, an exception occurred when invoking Supplier " +
                                    "'supplyIfElementAbsent' which is set by method 'createIfAbsent'", "Create Failed");
                    continue;
                }
                if (element == null) {
                    _handleVisitException(ErrorCode.MISSING_EXPECTED_ELEMENT, null, level, i,
                            "Missing expected element", "It's Null");
                    continue;
                }
                if (expectedElementType != null) {
                    if (!expectedElementType.isAssignableFrom(element.getClass())) {
                        _handleVisitException(ErrorCode.EXPECTED_ELEMENT_TYPE_MISMATCH, null, level, i,
                                "Failed to resolve parent element: expected type '" + expectedElementType.getName() +
                                        "', found '" + element.getClass().getName(), "Not " + expectedElementType.getSimpleName());
                        continue;
                    }
                }

                Object returnedElement = elementHandler.apply(element);
                // delete/remove
                if (returnedElement == DELETE_FLAG) {
                    ((Map) parentElement).remove(path.key);
                    continue;
                }
                // replace
                if (returnedElement != element) {
                    ((Map) parentElement).put(path.key, returnedElement);
                    continue;
                }

            } else {

                // parent element
                element = _tryCreateParentElementIfAbsent(element, paths.get(level + 1));
                if (element == null) {
                    _handleVisitException(ErrorCode.MISSING_PARENT_ELEMENT, null, level, i,
                            "Missing parent element", "It's Null");
                    continue;
                }

                // visit next path
                _visit(element, level + 1, expectedElementType, elementHandler);

            }

        }
    }

    private void _forEach_consumeAs(Class expectedElementType, Consumer elementConsumer) {
        _checkElementConsumer(elementConsumer);
        _visit(expectedElementType, e -> {
            elementConsumer.accept(e);
            return e;
        });
    }

    private void _forEach_replaceAs(Class expectedElementType, Function elementReplacer) {
        _checkElementReplacer(elementReplacer);
        _visit(expectedElementType, elementReplacer);
    }

    private void _forEach_delete() {
        _visit(null, e -> DELETE_FLAG);
    }

    private Collection _getAllAs(Class expectedElementType) {
        ArrayList result = new ArrayList();
        _visit(expectedElementType, e -> {
            result.add(e);
            return e;
        });
        return result;
    }

    private Collection _removeAllAs(Class expectedElementType) {
        ArrayList result = new ArrayList();
        _visit(expectedElementType, e -> {
            result.add(e);
            return DELETE_FLAG;
        });
        return result;
    }


    // Visitors /////////////////////////////////////////////////////////////////////////////////////////////////////


    public class BasicVisitor {

        private final ForEachVisitor forEachVisitor = new ForEachVisitor();

        private BasicVisitor() {
        }

        public BasicVisitor exceptionHandler(Consumer<ElementVisitException> exceptionHandler) {
            ElementVisitor.this.exceptionHandler = exceptionHandler;
            return basicVisitor;
        }

        public ForEachVisitor forEach() {
            return forEachVisitor;
        }

        public class ForEachVisitor {

            public <K, V> void consumeAsMap(Consumer<Map<K, V>> elementConsumer) throws ElementVisitException {
                _forEach_consumeAs(Map.class, elementConsumer);
            }

            public <E> void consumeAsList(Consumer<List<E>> elementConsumer) throws ElementVisitException {
                _forEach_consumeAs(List.class, elementConsumer);
            }

            public <E> void consumeAsSet(Consumer<Set<E>> elementConsumer) throws ElementVisitException {
                _forEach_consumeAs(Set.class, elementConsumer);
            }

            public <E> void consumeAs(Class<E> expectedElementType, Consumer<E> elementConsumer) throws ElementVisitException {
                _forEach_consumeAs(expectedElementType, elementConsumer);
            }

            public <K, V> void replaceAsMap(Function<Map<K, V>, Object> elementReplacer) throws ElementVisitException {
                _forEach_replaceAs(Map.class, elementReplacer);
            }

            public <E> void replaceAsList(Function<List<E>, Object> elementReplacer) throws ElementVisitException {
                _forEach_replaceAs(List.class, elementReplacer);
            }

            public <E> void replaceAsSet(Function<Set<E>, Object> elementReplacer) throws ElementVisitException {
                _forEach_replaceAs(Set.class, elementReplacer);
            }

            public <E> void replaceAs(Class<E> expectedElementType, Function<E, Object> elementReplacer) throws ElementVisitException {
                _forEach_replaceAs(expectedElementType, elementReplacer);
            }

            public void delete() throws ElementVisitException {
                _forEach_delete();
            }

        }

    }

    public class OnewayVisitor extends BasicVisitor {

        private OnewayVisitor() {
        }

        public OnewayVisitor exceptionHandler(Consumer<ElementVisitException> exceptionHandler) {
            ElementVisitor.this.exceptionHandler = exceptionHandler;
            return onewayVisitor;
        }

        public OnewayVisitor createIfAbsent(Supplier<Object> supplyIfElementAbsent) {
            ElementVisitor.this.supplyIfElementAbsent = supplyIfElementAbsent;
            return onewayVisitor;
        }

        public <K, V> Map<K, V> getAsMap() throws ElementVisitException {
            Collection<Map<K, V>> elements = _getAllAs(Map.class);
            for (Map<K, V> element : elements) {
                return element;
            }
            return null;
        }

        public <E> List<E> getAsList() throws ElementVisitException {
            Collection<List<E>> elements = _getAllAs(List.class);
            for (List<E> element : elements) {
                return element;
            }
            return null;        }

        public <E> Set<E> getAsSet() throws ElementVisitException {
            Collection<Set<E>> elements = _getAllAs(Set.class);
            for (Set<E> element : elements) {
                return element;
            }
            return null;
        }

        public <E> E getAs(Class<E> expectedElementType) throws ElementVisitException {
            Collection<E> elements = _getAllAs(expectedElementType);
            for (E element : elements) {
                return element;
            }
            return null;
        }

        public <K, V> Map<K, V> removeAsMap() throws ElementVisitException {
            Collection<Map<K, V>> elements = _removeAllAs(Map.class);
            for (Map<K, V> element : elements) {
                return element;
            }
            return null;
        }

        public <E> List<E> removeAsList() throws ElementVisitException {
            Collection<List<E>> elements = _removeAllAs(List.class);
            for (List<E> element : elements) {
                return element;
            }
            return null;
        }

        public <E> Set<E> removeAsSet() throws ElementVisitException {
            Collection<Set<E>> elements = _removeAllAs(Set.class);
            for (Set<E> element : elements) {
                return element;
            }
            return null;
        }

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

        public OnewayPathPlanner child(String key) {
            _addPath(key);
            return onewayPathPlanner;
        }

        public MultiwayPathPlanner children() {
            _addPath();
            return multiwayPathPlanner;
        }

        public OnewayPathPlanner exceptionHandler(Consumer<ElementVisitException> exceptionHandler) {
            ElementVisitor.this.exceptionHandler = exceptionHandler;
            return onewayPathPlanner;
        }

    }

    public class MultiwayVisitor extends BasicVisitor {

        private MultiwayVisitor() {
        }

        public MultiwayVisitor exceptionHandler(Consumer<ElementVisitException> exceptionHandler) {
            ElementVisitor.this.exceptionHandler = exceptionHandler;
            return multiwayVisitor;
        }

        public <K, V> Collection<Map<K, V>> getAllAsMap() throws ElementVisitException {
            return _getAllAs(Map.class);
        }

        public <E> Collection<List<E>> getAllAsList() throws ElementVisitException {
            return _getAllAs(List.class);
        }

        public <E> Collection<Set<E>> getAllAsSet() throws ElementVisitException {
            return _getAllAs(Set.class);
        }

        public <E> Collection<E> getAllAs(Class<E> expectedElementType) throws ElementVisitException {
            return _getAllAs(expectedElementType);
        }

        public <K, V> Collection<Map<K, V>> removeAllAsMap() throws ElementVisitException {
            return _removeAllAs(Map.class);
        }

        public <E> Collection<List<E>> removeAllAsList() throws ElementVisitException {
            return _removeAllAs(List.class);
        }

        public <E> Collection<Set<E>> removeAllAsSet() throws ElementVisitException {
            return _removeAllAs(Set.class);
        }

        public <E> Collection<E> removeAllAs(Class<E> expectedElementType) throws ElementVisitException {
            return _removeAllAs(expectedElementType);
        }

    }

    public class MultiwayPathPlanner extends MultiwayVisitor {

        private MultiwayPathPlanner() {
        }

        public MultiwayPathPlanner child(String key) {
            // 注意: 这里返回multiwayPathPlanner不是写错了
            // 访问路径中只要出现过children(), 后续就一直是multiwayPathPlanner了
            _addPath(key);
            return multiwayPathPlanner;
        }

        public MultiwayPathPlanner children() {
            _addPath();
            return multiwayPathPlanner;
        }

        public MultiwayPathPlanner exceptionHandler(Consumer<ElementVisitException> exceptionHandler) {
            ElementVisitor.this.exceptionHandler = exceptionHandler;
            return multiwayPathPlanner;
        }

    }


    // Path ////////////////////////////////////////////////////////////////////////////////////////////


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

    public enum ErrorCategory {

        /**
         * 数据缺失
         */
        DATA_MISSING,

        /**
         * 数据无效:
         * 路径中间的元素(parent_element)不是所需的Map或Collection类型.
         * 你想获取的元素(expected_element)不是指定的类型(你需要的类型).
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

    public enum ErrorCode {

        /**
         * 根元素(root_element)为空
         */
        MISSING_ROOT_ELEMENT(ErrorCategory.DATA_MISSING),
        /**
         * 路径中间的元素(parent_element)为空
         */
        MISSING_PARENT_ELEMENT(ErrorCategory.DATA_MISSING),
        /**
         * 你想获取的元素(expected_element)为空
         */
        MISSING_EXPECTED_ELEMENT(ErrorCategory.DATA_MISSING),

        /**
         * 路径中间的元素(parent_element)不是所需的Map或Collection类型.
         */
        PARENT_ELEMENT_TYPE_MISMATCH(ErrorCategory.DATA_INVALID),
        /**
         * 你想获取的元素(expected_element)不是指定的类型(你需要的类型).
         */
        EXPECTED_ELEMENT_TYPE_MISMATCH(ErrorCategory.DATA_INVALID),

        /**
         * [createIfAbsent] 创建路径中间的元素(parent_element)失败 (创建LinkedHashMap/ArrayList)
         */
        CREATE_PARENT_ELEMENT_FAILED(ErrorCategory.PROGRAMMING_ERROR),
        /**
         * [createIfAbsent] 创建你想获取的元素(expected_element)失败 (由createIfAbsent方法传入的表达式创建)
         */
        CREATE_EXPECTED_ELEMENT_FAILED(ErrorCategory.PROGRAMMING_ERROR),

        /**
         * [forEach#consumeAs...] forEach#consumeAs...方法传入的Consumer表达式为空
         */
        ELEMENT_CONSUMER_IS_NULL(ErrorCategory.PROGRAMMING_ERROR),
        /**
         * [forEach#replaceAs...] forEach#replaceAs...方法传入的Function表达式为空
         */
        ELEMENT_REPLACER_IS_NULL(ErrorCategory.PROGRAMMING_ERROR),

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

    public static class ElementVisitException extends RuntimeException {

        private static final long serialVersionUID = -9064567333501718644L;

        private ErrorCode errorCode;
        private String pathErrorOccurred;
        private String pathYouExpected;

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

    }

}
