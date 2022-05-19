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

import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.basic.FieldScreeningMode;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.filters.FooFilter;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.filters.MapValuesToList;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.filters.StringCheckLength;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.filters.ToString;
import com.github.shepherdviolet.glacimon.spring.x.conversion.mapio.rule.*;

/**
 * 文档中的规则配置示例
 *
 * @author shepherdviolet
 */
public class SampleDicts {

    private enum SubDict {
    }

    public enum SampleDict {

        /**
         * 标记为输入字段, 字段必输. (fromKey=Aaa, toKey=Aaa, required=true)
         * 等效于输入时: contextMap.put("Aaa", inputMap.get("Aaa"))
         */
        @Input
        Aaa,

        /**
         * 标记为输入字段, 字段必输, 字段名映射222->Bbb. (fromKey=222, toKey=Bbb, required=true)
         * 等效于输入时: contextMap.put("Bbb", inputMap.get("222"))
         */
        @Input(fromKey = "222")
        Bbb,

        /**
         * 标记为输入字段, 字段非必输. (fromKey=Ccc, toKey=Ccc, required=false)
         * 等效于输入时: contextMap.put("Ccc", inputMap.get("Ccc"))
         */
        @Input(required = false)
        Ccc,

        /**
         * 标记为输出字段, 字段必输. (fromKey=Ddd, toKey=Ddd, required=true)
         * 等效于输出时: outputMap.put("Ddd", contextMap.get("Ddd"))
         */
        @Output
        Ddd,

        /**
         * 标记为输出字段, 字段必输, 字段名映射555->Eee. (fromKey=555, toKey=Eee, required=true)
         * 等效于输出时: outputMap.put("Eee", contextMap.get("555"))
         */
        @Output
        Eee,

        /**
         * 标记为输出字段, 字段非必输. (fromKey=Fff, toKey=Fff, required=false)
         * 等效于输出时: outputMap.put("Fff", contextMap.get("Fff"))
         */
        @Output
        Fff,

        /**
         * 补充说明:
         * 一个字段可以同时被标记为输入/输出字段, 可以同时有输入/输出过滤规则, 输入输出规则互不干扰(各自在对应的IoMode工作模式中生效)
         */
        @Input
        //@InputFilter(type = FooFilter.class, args = {"aaa", "bbb"})
        //@InputElementFilter(type = FooFilter.class, args = {"aaa", "bbb"}, keepOrder = true)
        //@InputMapper(dictionary = SubDict.class, fieldScreeningMode = FieldScreeningMode.DISCARD_BY_DEFAULT)
        @Output
        //@OutputFilter(type = FooFilter.class, args = {"aaa", "bbb"})
                //@OutputElementFilter(type = FooFilter.class, args = {"aaa", "bbb"}, keepOrder = true)
                //@OutputMapper(fieldScreeningMode = FieldScreeningMode.DISCARD_BY_DEFAULT, dictionary = SubDict.class)
                Ggg,

        /**
         * [特殊情况][不建议使用] 覆盖字典中的字段名, 对于@Input来说相当于toKey.
         *
         * 字典类的字段名有字符限制, 只能用大小字母下划线和数字. 一般情况下, 建议系统内部字段名使用枚举, 这样比较整洁,
         * 但这样就不能用特殊字符了, 如果内部也一定要用特殊字符, 那就用这个覆盖字典中的字段名. 对外的字段名没有限制, 因为可以用@Input的fromKey指定.
         *
         * 未设置fromKey时, 等效于输入时: contextMap.put("-hhh-", inputMap.get("-hhh-"))
         * 设置了fromKey后, 等效于输入时: contextMap.put("-hhh-", inputMap.get("888"))
         */
        @Input
        //@Input(fromKey = "888")
        @FieldName("-hhh-")
        Hhh,

        /**
         * [特殊情况][不建议使用] 覆盖字典中的字段名, 对于@Output来说相当于fromKey.
         *
         * 字典类的字段名有字符限制, 只能用大小字母下划线和数字. 一般情况下, 建议系统内部字段名使用枚举, 这样比较整洁,
         * 但这样就不能用特殊字符了, 如果内部也一定要用特殊字符, 那就用这个覆盖字典中的字段名. 对外的字段名没有限制, 因为可以用@Output的toKey指定.
         *
         * 未设置toKey时, 等效于输出时: outputMap.put("-iii-", contextMap.get("-iii-"))
         * 设置了toKey后, 等效于输出时: outputMap.put("999", contextMap.get("-iii-"))
         */
        @Output
        //@Output(toKey = "999")
        @FieldName("-iii-")
        Iii,

    }

    public enum SampleDict2 {

        /**
         * [InputFilter/OutputFilter] 字段过滤器. 字段通过该过滤器过滤, 可以检查字段值, 也可以改变字段值.
         *
         * type: 指定过滤器类型(type和name至少设置一个)
         * name: 指定过滤器名称(type和name至少设置一个)
         * args: 过滤参数
         *
         * 示例: 检查输入字段(String)长度, 最小长度6, 最大长度8, 否则报错
         */
        @Input
        @InputFilter(type = StringCheckLength.class, args = {"6", "8"})
        Aaa,

        /**
         * [InputElementFilter/OutputElementFilter] 字段元素过滤器. 字段内的元素通过该过滤器过滤, 可以检查元素值, 也可以改变元素值.
         * 使用这个过滤规则时, 输入字段必须是Collection或者Map类型 (可以在前面加一个过滤器转换类型), 否则会报错.
         * 如果输入字段是Collection类型, 过滤器会过滤它的每一个元素.
         * 如果输入字段是Map类型, 过滤器会过滤它每一个元素的value. 另外, 过滤后字段默认是HashMap类型, 如果要保持顺序, 请设置keepOrder=true, 字段会变成LinkedHashMap.
         *
         * type: 指定过滤器类型(type和name至少设置一个)
         * name: 指定过滤器名称(type和name至少设置一个)
         * args: 过滤参数
         * keepOrder: 集合是Map时, 是否需要保持顺序. true:  过滤结果为LinkedHashMap. false: 过滤结果为HashMap.
         *
         * 示例: 检查输入字段(Collection或Map)的每一个元素, 检查元素(String)长度 (最小长度6, 最大长度8, 否则报错)
         */
        @Input
        @InputElementFilter(type = StringCheckLength.class, args = {"6", "8"})
        Bbb,

        /**
         * [InputMapper/OutputMapper] 字段(Map)映射器. 按照指定字典中的规则, 对源Map中的子Map字段进行映射 (相当于对子Map做MapIo#doMap).
         * 使用这个过滤规则时, 输入字段必须是Map类型 (可以在前面加一个过滤器转换类型), 否则会报错.
         *
         * dictionary: 映射字典, 根据其中用注解定义的规则映射元素.
         * fieldScreeningMode: Map字段筛选模式
         *
         * 示例: 将Ccc这个Map类型的字段, 按照指定字典中的规则进行字段映射.
         */
        @Input
        @InputMapper(dictionary = SubDict.class, fieldScreeningMode = FieldScreeningMode.DISCARD_BY_DEFAULT)
        Ccc,


        /**
         * 过滤规则可以配置无数个, 默认按代码书写顺序执行过滤规则. (特殊情况需要手动设置执行顺序, 见后文"关于过滤规则执行顺序"说明)
         *
         * 示例: 将输入字段类型转为String, 然后检查字段(String)长度 (最小长度6, 最大长度8, 否则报错)
         */
        @Input
        @InputFilter(type = ToString.class)
        @InputFilter(type = StringCheckLength.class, args = {"6", "8"})
        Xxx,

        /**
         * 过滤规则可以配置无数个, 默认按代码书写顺序执行过滤规则. (特殊情况需要手动设置执行顺序, 见后文"关于过滤规则执行顺序"说明)
         *
         * 示例: 将输入字段由Map转为List (取values), 然后检查每个元素的长度 (最小长度6, 最大长度8, 否则报错), 最终输出一个List
         */
        @Input
        @InputFilter(type = MapValuesToList.class)
        @InputElementFilter(type = StringCheckLength.class, args = {"6", "8"})
        Yyy,

    }

    /**
     * 关于过滤规则执行顺序(clauseOrder):
     * 1.一般情况下, MapIo会根据注解的书写顺序执行规则, 不需要手动设置执行顺序.
     * 2.如果一个字段存在多种'输入过滤规则'(@Input...), 且同一种'输入过滤规则'存在多条时, 程序将无法分辨注解的书写顺序, 这种情况,
     * 需要手动设置每个'输入过滤规则'的执行顺序.
     * 3.如果一个字段存在多种'输出过滤规则'(@Output...), 且同一种'输出过滤规则'存在多条时, 程序将无法分辨注解的书写顺序, 这种情况,
     * 需要手动设置每个'输出过滤规则'的执行顺序.
     * 4.输入规则和输出规则互不影响.
     *
     * 执行顺序(clauseOrder)设置说明:
     * 1.执行顺序的数值越小优先级越高(越先执行). 小于0表示未设置顺序(按代码书写顺序执行). 建议设置为: 0 1 2 3 ...
     * 2.一个字段的'输入过滤规则'(@Input...)执行顺序要么全都不设置, 要么全都设置, 不可以只设置部分.
     * 3.一个字段的'输出过滤规则'(@Output...)执行顺序要么全都不设置, 要么全都设置, 不可以只设置部分.
     * 4.输入规则和输出规则互不影响.
     */
    public enum SampleDict3 {

        /**
         * [不需要手动设置执行顺序的情况]
         * 虽然有三种输入规则, 但是每种规则只有一条, 不需要手动设置执行顺序.
         * 虽然有三种输出规则, 但是每种规则只有一条, 不需要手动设置执行顺序.
         * 输入规则和输出规则互不影响.
         */
        @Input
        @InputFilter(type = FooFilter.class)
        @InputElementFilter(type = FooFilter.class)
        @InputMapper(dictionary = SubDict.class, fieldScreeningMode = FieldScreeningMode.DISCARD_BY_DEFAULT)
        @Output
        @OutputFilter(type = FooFilter.class)
        @OutputElementFilter(type = FooFilter.class)
        @OutputMapper(dictionary = SubDict.class, fieldScreeningMode = FieldScreeningMode.DISCARD_BY_DEFAULT)
        Aaa,

        /**
         * [不需要手动设置执行顺序的情况]
         * 虽然有两条InputFilter规则, 但是输入规则只有这一种, 不需要手动设置执行顺序.
         * 虽然有两条OutputFilter规则, 但是输出规则只有这一种, 不需要手动设置执行顺序.
         * 输入规则和输出规则互不影响.
         */
        @Input
        @InputFilter(type = FooFilter.class)
        @InputFilter(type = FooFilter.class)
        @Output
        @OutputFilter(type = FooFilter.class)
        @OutputFilter(type = FooFilter.class)
        Bbb,

        /**
         * [需要手动设置执行顺序的情况]
         * 有两种输入规则, 且InputElementFilter规则有多条, 需要手动设置执行顺序.
         * 虽然有两条OutputFilter规则, 但是输出规则只有这一种, 不需要手动设置执行顺序.
         * 输入规则和输出规则互不影响.
         */
        @Input
        @InputFilter(clauseOrder = 0, type = FooFilter.class)
        @InputElementFilter(clauseOrder = 1, type = FooFilter.class)
        @InputElementFilter(clauseOrder = 2, type = FooFilter.class)
        @Output
        @OutputFilter(type = FooFilter.class)
        @OutputFilter(type = FooFilter.class)
        Ccc,

        /**
         * [需要手动设置执行顺序的情况]
         * 虽然有两条InputFilter规则, 但是输入规则只有这一种, 不需要手动设置执行顺序.
         * 有两种输出规则, 且OutputElementFilter规则有多条, 需要手动设置执行顺序.
         * 输入规则和输出规则互不影响.
         */
        @Input
        @InputFilter(type = FooFilter.class)
        @InputFilter(type = FooFilter.class)
        @Output
        @OutputMapper(clauseOrder = 0, dictionary = SubDict.class, fieldScreeningMode = FieldScreeningMode.DISCARD_BY_DEFAULT)
        @OutputElementFilter(clauseOrder = 1, type = FooFilter.class)
        @OutputElementFilter(clauseOrder = 2, type = FooFilter.class)
        Ddd,

        /**
         * [需要手动设置执行顺序的情况]
         * 有两种输入规则, 且InputElementFilter规则有多条, 需要手动设置执行顺序.
         * 有两种输出规则, 且OutputElementFilter规则有多条, 需要手动设置执行顺序.
         * 输入规则和输出规则互不影响.
         */
        @Input
        @InputFilter(clauseOrder = 0, type = FooFilter.class)
        @InputElementFilter(clauseOrder = 1, type = FooFilter.class)
        @InputElementFilter(clauseOrder = 2, type = FooFilter.class)
        @Output
        @OutputMapper(clauseOrder = 0, dictionary = SubDict.class, fieldScreeningMode = FieldScreeningMode.DISCARD_BY_DEFAULT)
        @OutputElementFilter(clauseOrder = 1, type = FooFilter.class)
        @OutputElementFilter(clauseOrder = 2, type = FooFilter.class)
        Eee,

    }

    public enum SampleDict4 {

        /**
         * [错误示范] 缺少@Input注解
         *
         * 必须先标记字段为输入/输出字段(@Input/@output), 才能为它设置对应的过滤规则
         */
        @InputFilter(type = FooFilter.class, args = {"aaa", "bbb"})
        //@InputElementFilter(type = FooFilter.class, args = {"aaa", "bbb"}, keepOrder = true)
                //@InputMapper(dictionary = SubDict.class, fieldScreeningMode = FieldScreeningMode.DISCARD_BY_DEFAULT)
                Aaa,

    }

}
