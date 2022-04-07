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

package sviolet.thistle.model.queue;

import sviolet.thistle.util.concurrent.ThreadPoolExecutorUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>目的性阻塞消息池</p>
 *
 * <p>*******************************************************************************************</p>
 *
 * <p>意外消息:若一个消息在塞入(restock)时, ID未在消息池注册, 则该消息被视为意外消息</p>
 *
 * <p>*******************************************************************************************</p>
 *
 * <p>模式1:意外消息直接抛弃模式</p>
 *
 * <p>用于提前知道ID的情况, 先注册ID, 然后阻塞等待异步操作塞入的消息.</p>
 *
 * <p>使用{@link PurposefulBlockingMessagePool#register(Object)}方法注册指定ID, 开始异步操作, 使用
 * {@link PurposefulBlockingMessagePool#wait(Object, long)} 方法等待目标对象返回, 此时线程阻塞. 当
 * 异步操作中, 通过{@link PurposefulBlockingMessagePool#restock(Object, Object)} 方法塞入目标对象后,
 * 原线程继续执行, 返回目标对象.</p>
 *
 * <p><pre>{@code
 *
 *  PurposefulBlockingMessagePool<String, String> pool = new PurposefulBlockingMessagePool<>();//意外消息直接抛弃模式
 *
 *  //注册需要的消息ID
 *  try {
 *      pool.register(id);
 *  } catch (PurposefulBlockingMessagePool.OutOfLimitException e) {
 *      //注册等待的消息超过限制
 *      //TODO异常处理,终止流程或抛出异常
 *      return;
 *  }
 *  //异步操作
 *  threadPool.execute(new Runnable(){
 *      public void run(){
 *          //TODO异步处理
 *          //将消息塞入消息池
 *          try {
 *              pool.restock(id, item);
 *          } catch (PurposefulBlockingMessagePool.MessageDropException e) {
 *              //因ID未注册或意外消息池满而抛出该异常
 *              //TODO处理被丢弃的消息
 *          }
 *      }
 *  });
 *  //阻塞线程, 等待消息返回
 *  String message = null;
 *  try {
 *      message = pool.wait(id, 5000);
 *  } catch (PurposefulBlockingMessagePool.TimeoutException e) {
 *      //等待超时抛出该异常
 *      //TODO超时处理,终止流程或抛出异常
 *      return;
 *  }
 *  if (message == null){
 *      //TODO返回消息为空,终止流程或抛出异常
 *      return;
 *  }
 *
 * }</pre></p>
 *
 * <p>*******************************************************************************************</p>
 *
 * <p>模式2:意外消息池模式</p>
 *
 * <p>用于无法提前知道ID的情况, 设置一个意外消息有效期, 接收到的意外消息将存入意外消息池, 有效期内可以
 * 从意外消息池获取该消息, 过期的消息将被清理任务清理(自动). 手动调用{@link PurposefulBlockingMessagePool#flush()}
 * 可立即启动清理任务.</p>
 *
 * <pre>{@code
 *
 * PurposefulBlockingMessagePool<String, String> pool = new PurposefulBlockingMessagePool<>(10000);//意外消息池模式, 设定意外消息有效期
 *
 * //异步操作,可能会先于pool.register()执行
 * threadPool.execute(new Runnable(){
 *      public void run(){
 *          //TODO异步处理
 *          //将消息塞入消息池
 *          try {
 *              pool.restock(id, item);
 *          } catch (PurposefulBlockingMessagePool.MessageDropException e) {
 *              //因意外消息池满而抛出该异常
 *              //TODO处理被丢弃的消息
 *          }
 *      }
 * });
 * //注册并阻塞线程, 等待消息返回
 * String message = null;
 * try {
 *      message = pool.registerAndWait(id, 5000);
 * } catch (PurposefulBlockingMessagePool.OutOfLimitException e) {
 *      //注册等待的消息超过限制
 *      //TODO异常处理,终止流程或抛出异常
 *      return;
 * } catch (PurposefulBlockingMessagePool.TimeoutException e) {
 *      //等待超时抛出该异常
 *      //TODO超时处理,终止流程或抛出异常
 *      return;
 * }
 * if (message == null){
 *      //TODO返回消息为空,终止流程或抛出异常
 *      return;
 * }
 *
 * }</pre>
 *
 * @author S.Violet
 */
public class PurposefulBlockingMessagePool <K, I> {

    /**
     * 默认限制
     */
    private static final int DEFAULT_LIMIT = 1000;
    /**
     * 意外消息池清理任务时延
     */
    private static final long UNEXPECTED_ITEM_FLUSH_DELAY = 10 * 1000000L;

    /**
     * 锁
     */
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * 信号池
     */
    private final Map<K, Condition> conditionPool = new HashMap<>();
    /**
     * 消息池
     */
    private final Map<K, I> itemPool = new HashMap<>();

    /**
     * 意外消息池(存放未注册ID的消息)
     */
    private Map<K, UnexpectedItem<I>> unexpectedItemPool = null;
    /**
     * 意外消息有效期(ms)
     */
    private long unexpectedItemValidityPeriod = 0;
    /**
     * 意外消息锁
     */
    private ReentrantLock unexpectedItemLock = null;
    /**
     * 意外消息池清理线程
     */
    private ExecutorService unexpectedItemFlushThreadPool = null;
    /**
     * 消息从意外消息池被丢弃回调
     */
    private MessageDropListener<I> messageDropListener = null;

    /**
     * 注册等待数上限
     */
    private int registerLimit = DEFAULT_LIMIT;
    /**
     * 消息池内消息数上限
     */
    private int messageLimit = DEFAULT_LIMIT;

    /**
     * 直接丢弃意外消息(未注册ID的塞入消息)
     */
    public PurposefulBlockingMessagePool(){
        this(0, null);
    }

    /**
     * 意外消息(未注册ID的消息)塞入时, 存入意外消息池. 在意外消息过期前, 仍能被获取到, 在意外消息过期后,
     * 会被清理任务清理掉, 清理后将无法获得该消息.
     *
     * @param unexpectedItemValidityPeriod 意外消息有效期 ms >0生效
     */
    public PurposefulBlockingMessagePool(long unexpectedItemValidityPeriod){
        this(unexpectedItemValidityPeriod, null);
    }

    /**
     * 意外消息(未注册ID的消息)塞入时, 存入意外消息池. 在意外消息过期前, 仍能被获取到, 在意外消息过期后,
     * 会被清理任务清理掉, 清理后将无法获得该消息.
     *
     * @param unexpectedItemValidityPeriod 意外消息有效期 ms >0生效
     * @param messageDropListener 当消息从意外消息池被丢弃时回调该监听器
     */
    public PurposefulBlockingMessagePool(long unexpectedItemValidityPeriod, MessageDropListener<I> messageDropListener){
        this.unexpectedItemValidityPeriod = unexpectedItemValidityPeriod;
        setMessageDropListener(messageDropListener);
        if (this.unexpectedItemValidityPeriod > 0) {
            this.unexpectedItemLock = new ReentrantLock();
            this.unexpectedItemFlushThreadPool = ThreadPoolExecutorUtils.createLazy(60L, "Thistle-PurposefulBlockMQ-Flush-%d");
            this.unexpectedItemPool = new HashMap<>();
        }
    }

    /**
     * @param messageDropListener 当消息从意外消息池被丢弃时回调该监听器
     */
    public void setMessageDropListener(MessageDropListener<I> messageDropListener){
        this.messageDropListener = messageDropListener;
    }

    /**
     * @param registerLimit 设置注册等待数上限, 注册等待的消息ID超过限制将会抛出异常, 默认1000
     */
    public void setRegisterLimit(int registerLimit){
        this.registerLimit = registerLimit;
    }

    /**
     * @param messageLimit 意外消息数上限(不包括普通消息池), 超过上限将会抛弃塞入的新消息, 默认1000
     */
    public void setMessageLimit(int messageLimit){
        this.messageLimit = messageLimit;
    }

    /**
     * 注册并阻塞等待消息, {@link PurposefulBlockingMessagePool#register(Object)}&{@link PurposefulBlockingMessagePool#wait(Object, long)}
     * @param id 指定的ID
     * @param timeout 超时时间
     * @return 指定ID的目标对象(可能为空)
     * @exception OutOfLimitException 注册等待的消息数超过限制时抛出该异常, 注册被拒绝
     * @exception TimeoutException 阻塞等待超时时抛出该异常
     */
    public I registerAndWait(K id, long timeout) throws OutOfLimitException, TimeoutException{
        register(id);
        return wait(id, timeout);
    }

    /**
     * 注册指定ID, 表明需要目标对象, 注册后该消息池接受该ID目标对象的塞入(restock)
     * @param id 指定的ID
     * @exception OutOfLimitException 注册等待的消息数超过限制时抛出该异常, 注册被拒绝
     */
    public void register(K id) throws OutOfLimitException{
        Condition condition = lock.newCondition();
        try{
            lock.lock();
            if (getRegisterCount() > registerLimit){
                throw new OutOfLimitException("[PurposefulBlockingMessagePool]register out of limit, drop this register : " + registerLimit);
            }
            conditionPool.put(id, condition);
        }finally {
            lock.unlock();
            flush();
        }
    }

    /**
     * 阻塞等待并返回指定ID的目标对象, 必须先调用{@link PurposefulBlockingMessagePool#register(Object)}注册等待的ID.
     * @param id 指定的ID
     * @param timeout 超时时间
     * @return 指定ID的目标对象(可能为空)
     * @exception TimeoutException 阻塞等待超时时抛出该异常
     */
    public I wait(K id, long timeout) throws TimeoutException{
        final long startMillis = System.currentTimeMillis();
        try{
            lock.lock();
            final Condition condition = conditionPool.get(id);
            if (condition == null){
                throw new RuntimeException("[PurposefulBlockingMessagePool]can't wait() before register()");
            }
            I item;
            while ((item = getItem(id)) == null) {
                final long remainTimeout = timeout - (System.currentTimeMillis() - startMillis);
                if (remainTimeout <= 0){
                    break;
                }
                try {
                    if (!condition.await(remainTimeout, TimeUnit.MILLISECONDS)) {
                        break;
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }
            getItem(id);
            conditionPool.remove(id);
            if (item == null){
                throw new TimeoutException("[PurposefulBlockingMessagePool]waiting for message timeout : " + timeout);
            }
            return item;
        } finally {
            lock.unlock();
            flush();
        }
    }

    /**
     * <p>向消息池塞入指定ID的目标对象</p>
     *
     * <p>意外消息直接抛弃模式下, 若该ID未注册, 或等待已超时, 则塞入无效, 抛出异常</p>
     *
     * <p>意外消息池模式下, 若意外消息池消息数量超过限制, 则塞入无效, 抛出异常</p>
     *
     * @param id 指定ID
     * @param item 目标对象
     * @exception MessageDropException 消息塞入失败, 被丢弃时抛出该异常. 意外消息直接丢弃模式下, 消息
     * 塞入消息池时, 因ID未注册而丢弃消息. 意外消息池模式下, 因意外消息池超过数量限制而丢弃消息. 意外消息
     * 池模式下, 因意外消息过期而丢弃消息的, 请使用messageDropListener监听事件.
     */
    public void restock(K id, I item) throws MessageDropException {
        try{
            lock.lock();
            Condition condition = conditionPool.get(id);
            if (condition != null){
                itemPool.put(id, item);
                condition.signalAll();
                return;
            }
        }finally {
            lock.unlock();
            flush();
        }
        //意外消息存入意外消息池
        if(unexpectedItemPool != null){
            try{
                unexpectedItemLock.lock();
                if (getUnexpectedItemCount() > messageLimit){
                    throw new MessageDropException("[PurposefulBlockingMessagePool]unexpected message out of limit, drop this message, id:" + String.valueOf(id) + " limit:" + messageLimit);
                }
                //放入意外消息池
                unexpectedItemPool.put(id, new UnexpectedItem<>(item));
                return;
            } finally {
                unexpectedItemLock.unlock();
            }
        }
        throw new MessageDropException("[PurposefulBlockingMessagePool]id not registered, drop this message, id:" + String.valueOf(id));
    }

    /**
     * @return 注册的ID数
     */
    public int getRegisterCount(){
        try{
            lock.lock();
            return conditionPool.size();
        }finally {
            lock.unlock();
        }
    }

    /**
     * @return 消息数(非意外消息)
     */
    public int getItemCount(){
        try{
            lock.lock();
            return itemPool.size();
        }finally {
            lock.unlock();
        }
    }

    /**
     * @return 意外消息数
     */
    public int getUnexpectedItemCount(){
        if (unexpectedItemPool == null){
            return 0;
        }
        try{
            unexpectedItemLock.lock();
            return unexpectedItemPool.size();
        }finally {
            unexpectedItemLock.unlock();
        }
    }

    /**
     * 立即启动清理任务, 清理意外消息池中的失效消息(过期)
     */
    public void flush(){
        if (unexpectedItemPool != null){
            unexpectedItemFlushThreadPool.execute(new UnexpectedItemFlushTask());
        }
    }

    private I getItem(K id){
        I item = null;
        UnexpectedItem<I> unexpectedItem = null;
        try {
            lock.lock();
            item = itemPool.remove(id);
        }finally {
            lock.unlock();
        }
        if (unexpectedItemPool != null) {
            try {
                unexpectedItemLock.lock();
                unexpectedItem = unexpectedItemPool.remove(id);
            } finally {
                unexpectedItemLock.unlock();
            }
        }
        if (item != null){
            return item;
        }
        if (unexpectedItem != null){
            return unexpectedItem.getItem();
        }
        return null;
    }

    /**
     * 意外消息池清理任务
     */
    private class UnexpectedItemFlushTask implements Runnable{
        @Override
        public void run() {
            //使清理任务间歇进行, 防止过多的占用锁
            LockSupport.parkNanos(UNEXPECTED_ITEM_FLUSH_DELAY);
            List<I> overdueItems = null;
            if (messageDropListener != null) {
                overdueItems = new ArrayList<>();
            }
            try{
                unexpectedItemLock.lock();
                List<K> overdueIds = new ArrayList<>();
                for (Map.Entry<K, UnexpectedItem<I>> entry : unexpectedItemPool.entrySet()){
                    UnexpectedItem<I> unexpectedItem = entry.getValue();
                    if (unexpectedItem == null || unexpectedItem.getItem() == null || unexpectedItem.isOverdue(unexpectedItemValidityPeriod)){
                        overdueIds.add(entry.getKey());
                    }
                }
                for (K id : overdueIds){
                    UnexpectedItem<I> unexpectedItem = unexpectedItemPool.remove(id);
                    if (overdueItems != null && unexpectedItem != null && unexpectedItem.getItem() != null){
                        overdueItems.add(unexpectedItem.getItem());
                    }
                }
            } finally {
                unexpectedItemLock.unlock();
            }
            if (overdueItems != null) {
                for (I item : overdueItems) {
                    messageDropListener.onDrop(item);
                }
            }
        }
    }

    /**
     * 意外消息
     * @param <I> 消息
     */
    private static class UnexpectedItem<I>{

        private I item;
        private long startTime;

        UnexpectedItem(I item){
            this.startTime = System.currentTimeMillis();
            this.item = item;
        }

        I getItem(){
            return item;
        }

        /**
         * 消息是否过期
         * @param unexpectedItemValidityPeriod 意外消息有效期
         * @return true:过期
         */
        boolean isOverdue(long unexpectedItemValidityPeriod){
            return (System.currentTimeMillis() - startTime) > unexpectedItemValidityPeriod;
        }

    }

    /**
     * 消息从意外消息池被抛弃时回调该监听器
     * @param <I>
     */
    public interface MessageDropListener<I>{

        /**
         * 消息从意外消息池被抛弃时回调
         * @param item 消息
         */
        void onDrop(I item);
    }

    /**
     * <p>意外消息直接抛弃模式下, 若该ID未注册, 或等待已超时, 则塞入无效, 抛出异常</p>
     *
     * <p>意外消息池模式下, 若意外消息池消息数量超过限制, 则塞入无效, 抛出异常</p>
     */
    public static class MessageDropException extends Exception{

        private static final long serialVersionUID = 1915505356403872233L;

        public MessageDropException(String detailMessage) {
            super(detailMessage);
        }

    }

    /**
     * 注册等待消息数超出限制抛出异常
     */
    public static class OutOfLimitException extends Exception{

        private static final long serialVersionUID = 2115724388185665168L;

        public OutOfLimitException(String detailMessage) {
            super(detailMessage);
        }

    }

    /**
     * 阻塞等待超时
     */
    public static class TimeoutException extends Exception{

        private static final long serialVersionUID = -1706311472131999721L;

        public TimeoutException(String detailMessage) {
            super(detailMessage);
        }

    }

}