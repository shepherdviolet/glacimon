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

package sviolet.thistle.util.common;

import sviolet.thistle.entity.common.Destroyable;
import sviolet.thistle.model.concurrent.lock.HashReentrantLocks;
import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>寄生变量</p>
 *
 * <p>警告: 慎用寄生变量, 不当的使用可能造成内存泄漏.</p>
 *
 * <p>
 * 生命周期依附宿主的变量, 不建议存放过大的对象.<br/>
 * 每一个变量需声明其宿主(host)和变量名(key), 同一个宿主名下不可存在同名变量. 变量会被强引用, 直到生命周期结束,
 * 变量的生命周期取决于宿主的生命周期, 即宿主被系统GC时, 其名下所有变量生命周期结束, 也可以主动调用remove()方法移除变量.
 * 变量生命周期结束时, 会解除对其的强引用, 若变量实现了{@link Destroyable}接口, 会被调用{@link Destroyable#onDestroy()}
 * 方法用于销毁实例, 对象可能在下一次系统GC时被销毁, 若变量被其他对象强引用, 则不会被GC.
 * </p>
 *
 * <p>*************************************************************</p>
 *
 * <p>
 * host 宿主 : <br/>
 * 不可为空,宿主被系统GC时,其名下所有变量会被释放引用,实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁
 * </p>
 *
 * <p>
 * key 变量名 : <br/>
 * 不可为空,用于区分相同宿主名下的不同变量,相同宿主名下,若变量名重复,原变量实例会被销毁({@link Destroyable#onDestroy()})
 * </p>
 *
 * <p>
 * param 变量 : <br/>
 * 生命周期依附于宿主的变量,当宿主被系统GC时,变量也会被释放引用,实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁.
 * 变量实例可能在下一次系统GC时销毁, 若变量被其他对象强引用, 则不会被GC
 * </p>
 *
 * @author S.Violet
 */
public class ParasiticVars {

    /**
     * gc事件监听器
     */
    private static WeakReference<GcHandler> gcHandler;

    /**
     * 变量宿主
     */
    private volatile static Map<String, HostHolder> hosts;

    /**
     * gc任务执行线程池
     */
    private volatile static ExecutorService gcTaskPool;

    private static AtomicBoolean initialized = new AtomicBoolean(false);
    private static AtomicBoolean gcTaskPoolInited = new AtomicBoolean(false);
    private static HashReentrantLocks locks = new HashReentrantLocks(16);

    /**
     * 设置寄生变量
     *
     * @param host 宿主 不为空,宿主被系统GC时,其名下所有变量会被GC,实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁
     * @param key 变量名 不为空,相同宿主名下,若变量名重复,原变量实例会被销毁({@link Destroyable#onDestroy()})
     * @param param 变量 不为空,生命周期依附于宿主
     */
    public static void set(Object host, String key, Object param){

        if (host == null || param == null) {
            return;
        }
        if (key == null) {
            throw new NullPointerException("[ParasiticVars] key == null");
        }

        //初始化
        init();

        //计算宿主Key
        String hostKey = calculateHostKey(host);

        HostHolder hostHolder = hosts.get(hostKey);
        if (hostHolder == null) {
            ReentrantLock lock = locks.getLock(hostKey);
            try {
                lock.lock();
                //获取已存在的宿主
                hostHolder = hosts.get(hostKey);
                //宿主不存在则加入
                if (hostHolder == null) {
                    hostHolder = new HostHolder(host);
                    hosts.put(hostKey, hostHolder);
                }
            } finally {
                lock.unlock();
            }
        }

        //变量存入宿主
        hostHolder.set(key, param);

    }

    /**
     * 获取寄生变量
     *
     * @param host 宿主 不为空,宿主被系统GC时,其名下所有变量会被GC,实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁
     * @param key 变量名 不为空,相同宿主名下,若变量名重复,原变量实例会被销毁({@link Destroyable#onDestroy()})
     * @return 变量 可为空,生命周期依附于宿主
     */
    public static Object get(Object host, String key){

        if (host == null) {
            return null;
        }
        if (key == null) {
            throw new NullPointerException("[ParasiticVars] key == null");
        }

        //初始化
        init();

        //计算宿主Key
        String hostKey = calculateHostKey(host);

        //获取已存在的宿主
        HostHolder hostHolder = hosts.get(hostKey);

        //宿主存在则获取变量
        if (hostHolder != null){
            return hostHolder.get(key);
        }

        //宿主不存在返回null
        return null;
    }

    /**
     * 移除寄生变量, 实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁
     *
     * @param host 宿主 不为空,宿主被系统GC时,其名下所有变量会被GC,实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁
     * @param key 变量名 不为空
     */
    public static void remove(Object host, String key){

        if (host == null) {
            return;
        }
        if (key == null) {
            throw new NullPointerException("[ParasiticVars] key == null");
        }

        //初始化
        init();

        //计算宿主Key
        String hostKey = calculateHostKey(host);
        HostHolder gcHostHolder = hosts.get(hostKey);

        //宿主存在则从中移除变量
        if (gcHostHolder != null) {
            gcHostHolder.remove(key);
        }

    }

    /**
     * 移除指定宿主名下所有寄生变量, 实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁
     *
     * @param host 宿主 不为空,宿主被系统GC时,其名下所有变量会被GC,实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁
     */
    public static void removeAll(Object host){

        if (host == null) {
            return;
        }

        //初始化
        init();

        //计算宿主Key
        String hostKey = calculateHostKey(host);
        HostHolder gcHostHolder = hosts.remove(hostKey);

        //宿主存在则清空其中的变量
        if (gcHostHolder != null) {
            gcHostHolder.removeAll();
        }

    }

    /**
     * 移除所有寄生变量, 实现{@link Destroyable}接口的变量会调用onDestroy()方法销毁
     */
    public static void removeAll(){

        Map<String, HostHolder> gcHosts = hosts;
        if (gcHosts != null) {
            for (Map.Entry<String, HostHolder> entry : gcHosts.entrySet()) {
                entry.getValue().removeAll();
            }
            gcHosts.clear();
        }

    }

    /**
     * 初始化GlobalVars
     */
    private static void init(){
        //宿主Map不存在则新建
        while (hosts == null){
            if (!initialized.get() && initialized.compareAndSet(false, true)) {
                //新建gc事件监听
                gcHandler = new WeakReference<>(new GcHandler());
                //新建宿主Map
                hosts = new ConcurrentHashMap<>();
            } else {
                Thread.yield();
            }
        }
    }

    private static ExecutorService getGcTaskPool(){
        while (gcTaskPool == null){
            if (!gcTaskPoolInited.get() && gcTaskPoolInited.compareAndSet(false, true)) {
                //新建gc任务执行线程池
                gcTaskPool = ThreadPoolExecutorUtils.createLazy(60L, "Thistle-ParasiticVars-gc-%d");
            } else {
                Thread.yield();
            }
        }
        return gcTaskPool;
    }

    /**
     * 系统gc时, 清理无宿主变量
     */
    private static void gc(){

        //引用当前值
        final Map<String, HostHolder> hosts = ParasiticVars.hosts;
        if (hosts == null){
            return;
        }

        //被清理的宿主镜像
        Map<String, HostHolder> gcHosts = new HashMap<>();

        //搜索无宿主的宿主镜像
        for (Map.Entry<String, HostHolder> entry : hosts.entrySet()) {
            if (!entry.getValue().isHostExists()) {
                gcHosts.put(entry.getKey(), entry.getValue());
            }
        }
        //清除无宿主的宿主镜像
        for (Map.Entry<String, HostHolder> entry : gcHosts.entrySet()) {
            hosts.remove(entry.getKey());
        }

        //清理无宿主的变量
        for (Map.Entry<String, HostHolder> entry : gcHosts.entrySet()){
            entry.getValue().removeAll();
        }

        gcHosts.clear();

        //新建gc事件监听
        gcHandler = new WeakReference<>(new GcHandler());
    }

    /**
     * 计算宿主key
     * @param host 宿主
     */
    private static String calculateHostKey(Object host){
        return host.getClass().getSimpleName() + "#" + host.hashCode();
    }

    /**
     * 系统gc事件监听器
     */
    private static class GcHandler{
        /**
         * 利用WeakPreference引用的对象会被系统gc时清理,
         * 在finalize()方法中执行GlobalVars无宿主变量的清理
         */
        @Override
        protected void finalize() throws Throwable {
            try {
                getGcTaskPool().execute(gcRunnable);
            } finally {
                super.finalize();
            }
        }
    }

    private static Runnable gcRunnable = new Runnable() {
        @Override
        public void run() {
            ParasiticVars.gc();
        }
    };

    /**
     * 宿主镜像<p/>
     *
     * 包含WeakReference引用的宿主本身, 和宿主变量
     */
    private static class HostHolder{

        /**
         * 宿主
         */
        private WeakReference<Object> host;

        /**
         * 宿主变量
         */
        private volatile Map<String, Object> params = new ConcurrentHashMap<>();

        HostHolder(Object host){
            //弱引用宿主
            this.host = new WeakReference<>(host);
        }

        void set(String key, Object param){

            if (key == null || param == null) {
                throw new NullPointerException("[ParasiticVars] key == null || param == null");
            }

            Object previous = params.put(key, param);

            if (previous != null && previous != param) {
                CloseableUtils.closeIfCloseable(previous);
            }

        }

        Object get(String key){

            if (key == null) {
                throw new NullPointerException("[ParasiticVars] key == null");
            }

            return params.get(key);

        }

        void remove(String key){

            if (key == null) {
                throw new NullPointerException("[ParasiticVars] key == null");
            }

            Object destroyable = params.remove(key);

            //销毁原变量
            CloseableUtils.closeIfCloseable(destroyable);

        }

        void removeAll(){

            Map<String, Object> destroyables = new HashMap<>();

            for (Map.Entry<String, Object> entry : params.entrySet()) {
                destroyables.put(entry.getKey(), entry.getValue());
            }

            for (Map.Entry<String, Object> entry : destroyables.entrySet()) {
                params.remove(entry.getKey());
            }

            //销毁所有变量
            for (Map.Entry<String, Object> entry : destroyables.entrySet()){
                CloseableUtils.closeIfCloseable(entry.getValue());
            }

            destroyables.clear();

        }

        Object getHost(){
            if (host != null) {
                return host.get();
            }
            return null;
        }

        /**
         * 宿主是否还存在
         * @return true 宿主存在
         */
        boolean isHostExists(){
            return getHost() != null;
        }

    }

}
