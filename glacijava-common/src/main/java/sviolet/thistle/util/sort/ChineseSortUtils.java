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

package sviolet.thistle.util.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * 
 * 中文排序工具
 * 
 * @author S.Violet ()
 *
 */
public class ChineseSortUtils {

    public static void sort(String[] list){
        Arrays.sort(list, java.text.Collator.getInstance(java.util.Locale.CHINA));
    }

    public static void sort(List<String> list){
        Collections.sort(list, java.text.Collator.getInstance(java.util.Locale.CHINA));
    }

    /**
     *    List<Item> list = ...
     *    list = ChineseSortUtils.keySort(list, new KeyGetter<Item>(){
     *        //根据Item对象的getName()方法得到关键字
     *        public String getKey(Item obj) {
     *            return obj.getName();
     *        }
     *    });
     */
    public static <T> List<T> keySort(List<T> list, KeyGetter<T> keyGetter){
        //取得对象们的关键字
        String[] keyArray = new String[list.size()];
        for(int i = 0 ; i < list.size() ; i++){
            keyArray[i] = keyGetter.getKey(list.get(i));
        }

        //关键字排序
        sort(keyArray);

        //根据关键字顺序对对象进行排序
        List<T> result = new ArrayList<>();
        for(int i = 0 ; i < keyArray.length ; i++){
            for(int j = 0 ; j < list.size() ; j++){
                if(keyGetter.getKey(list.get(j)).equals(keyArray[i])){
                    result.add(list.get(j));
                    list.remove(j);
                    break;
                }
            }
        }
        return result;
    }

    public interface KeyGetter<T>{
        /**
         * 实现从对象中获取key, 用于排序
         * @param obj 待排序对象
         * @return key
         */
        String getKey(T obj);
    }

}
