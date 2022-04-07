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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.thistle.compat.queue.CompatLinkedBlockingDeque;

/**
 * 强化版LinkedBlockingDeque<Br/>
 * 1.支持元素不可重复<br/>
 * 2.增加"闯入方式入队"方法<br/>
 * 3.增加"移除所有等于指定对象的元素"方法<br/>
 * 4.元素意外移除监听<br/>
 *
 * @author S.Violet
 */
public class TLinkedBlockingDeque<E> extends CompatLinkedBlockingDeque<E> {

    //SETTINGS////////////////////////////////////
    /**
     * 元素不可重复标记
     */
    private boolean unrepeatable = false;

    private OnElementUnexpectedRemoveListener mOnElementUnexpectedRemoveListener;

    //Variable////////////////////////////////////

    /**
     * 默认容量Integer.MAX_VALUE, 允许元素重复
     */
    public TLinkedBlockingDeque() {
        this(Integer.MAX_VALUE, false);
    }

    /**
     * @param capacity 队列容量
     * @param unrepeatable true:元素不可重复, 当元素塞入队列时, 会清除队列中原有相同的元素(元素equals()
     *                     方法决定是否相同), 该设置可能会使性能有所下降
     */
    public TLinkedBlockingDeque(int capacity, boolean unrepeatable) {
        super(capacity);
        this.unrepeatable = unrepeatable;
    }

    /**
     * @param unrepeatable true:元素不可重复, 当元素塞入队列时, 会清除队列中原有相同的元素(元素equals()
     *                     方法决定是否相同), 该设置可能会使性能有所下降
     */
    public TLinkedBlockingDeque(Collection<? extends E> c, boolean unrepeatable) {
        super(c);
        this.unrepeatable = unrepeatable;
    }

    /*********************************************************
     * public 入队
     */

    @Override
    public boolean offerFirst(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        Node<E> node = new Node<E>(e);
        final ReentrantLock lock = this.lock;
        boolean result;
        Object unlinkedElement = null;
        lock.lock();
        try {
            //不可重复模式, 移除队列中的相同元素
            if(unrepeatable) {
                for (Node<E> p = first; p != null; p = p.next) {
                    if (e.equals(p.item)) {
                        unlinkedElement = p.item;
                        unlink(p);
                    }
                }
            }
            result = linkFirst(node);
        } finally {
            lock.unlock();
        }
        if(mOnElementUnexpectedRemoveListener != null && unlinkedElement != null){
            mOnElementUnexpectedRemoveListener.onUnexpectedRemove(unlinkedElement);
        }
        return result;
    }

    @Override
    public boolean offerLast(E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        Node<E> node = new Node<E>(e);
        final ReentrantLock lock = this.lock;
        boolean result;
        Object unlinkedElement = null;
        lock.lock();
        try {
            //不可重复模式, 移除队列中的相同元素
            if(unrepeatable) {
                for (Node<E> p = first; p != null; p = p.next) {
                    if (e.equals(p.item)) {
                        unlinkedElement = p.item;
                        unlink(p);
                    }
                }
            }
            result = linkLast(node);
        } finally {
            lock.unlock();
        }
        if(mOnElementUnexpectedRemoveListener != null && unlinkedElement != null){
            mOnElementUnexpectedRemoveListener.onUnexpectedRemove(unlinkedElement);
        }
        return result;
    }

    @Override
    public void putFirst(E e) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        }
        Node<E> node = new Node<E>(e);
        final ReentrantLock lock = this.lock;
        Object unlinkedElement = null;
        lock.lock();
        try {
            //不可重复模式, 移除队列中的相同元素
            if(unrepeatable) {
                for (Node<E> p = first; p != null; p = p.next) {
                    if (e.equals(p.item)) {
                        unlinkedElement = p.item;
                        unlink(p);
                    }
                }
            }
            while (!linkFirst(node)) {
                notFull.await();
            }
        } finally {
            lock.unlock();
        }
        if(mOnElementUnexpectedRemoveListener != null && unlinkedElement != null){
            mOnElementUnexpectedRemoveListener.onUnexpectedRemove(unlinkedElement);
        }
    }

    @Override
    public void putLast(E e) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        }
        Node<E> node = new Node<E>(e);
        final ReentrantLock lock = this.lock;
        Object unlinkedElement = null;
        lock.lock();
        try {
            //不可重复模式, 移除队列中的相同元素
            if(unrepeatable) {
                for (Node<E> p = first; p != null; p = p.next) {
                    if (e.equals(p.item)) {
                        unlinkedElement = p.item;
                        unlink(p);
                    }
                }
            }
            while (!linkLast(node)) {
                notFull.await();
            }
        } finally {
            lock.unlock();
        }
        if(mOnElementUnexpectedRemoveListener != null && unlinkedElement != null){
            mOnElementUnexpectedRemoveListener.onUnexpectedRemove(unlinkedElement);
        }
    }

    @Override
    public boolean offerFirst(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        }
        Node<E> node = new Node<E>(e);
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        boolean result;
        Object unlinkedElement = null;
        lock.lockInterruptibly();
        try {
            //不可重复模式, 移除队列中的相同元素
            if(unrepeatable) {
                for (Node<E> p = first; p != null; p = p.next) {
                    if (e.equals(p.item)) {
                        unlinkedElement = p.item;
                        unlink(p);
                    }
                }
            }
            while (!linkFirst(node)) {
                if (nanos <= 0) {
                    result = false;
                }else {
                    nanos = notFull.awaitNanos(nanos);
                }
            }
            result = true;
        } finally {
            lock.unlock();
        }
        if(mOnElementUnexpectedRemoveListener != null && unlinkedElement != null){
            mOnElementUnexpectedRemoveListener.onUnexpectedRemove(unlinkedElement);
        }
        return result;
    }

    @Override
    public boolean offerLast(E e, long timeout, TimeUnit unit) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        }
        Node<E> node = new Node<E>(e);
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        boolean result;
        Object unlinkedElement = null;
        lock.lockInterruptibly();
        try {
            //不可重复模式, 移除队列中的相同元素
            if(unrepeatable) {
                for (Node<E> p = first; p != null; p = p.next) {
                    if (e.equals(p.item)) {
                        unlinkedElement = p.item;
                        unlink(p);
                    }
                }
            }
            while (!linkLast(node)) {
                if (nanos <= 0) {
                    result = false;
                }else {
                    nanos = notFull.awaitNanos(nanos);
                }
            }
            result = true;
        } finally {
            lock.unlock();
        }
        if(mOnElementUnexpectedRemoveListener != null && unlinkedElement != null){
            mOnElementUnexpectedRemoveListener.onUnexpectedRemove(unlinkedElement);
        }
        return result;
    }

    /**
     * [增强]闯入方式入队(队首)<br/>
     * 若队列未满, 则插入队列头部<br/>
     * 若队列已满, 强行移除队尾的元素, 并将自己插入队列头部<br/>
     * @param e
     */
    public void intrudeFirst(E e){
        if (e == null) {
            throw new NullPointerException();
        }
        Node<E> node = new Node<E>(e);
        final ReentrantLock lock = this.lock;
        List<Object> unlinkedElements = null;
        lock.lock();
        try {
            while(!linkFirst(node)){
                if (unlinkedElements == null) {
                    unlinkedElements = new ArrayList<>();
                }
                unlinkedElements.add(unlinkLast());
            }
        } finally {
            lock.unlock();
        }
        if(mOnElementUnexpectedRemoveListener != null && unlinkedElements != null){
            for(Object obj : unlinkedElements){
                mOnElementUnexpectedRemoveListener.onUnexpectedRemove(obj);
            }
        }
    }

    /**
     * [增强]闯入方式入队(队尾)<br/>
     * 若队列未满, 则插入队列尾部<br/>
     * 若队列已满, 强行移除队尾的元素, 并将自己插入队列尾部<br/>
     * @param e
     */
    public void intrudeLast(E e){
        if (e == null) {
            throw new NullPointerException();
        }
        Node<E> node = new Node<E>(e);
        final ReentrantLock lock = this.lock;
        List<Object> unlinkedElements = null;
        lock.lock();
        try {
            while(!linkLast(node)){
                if (unlinkedElements == null) {
                    unlinkedElements = new ArrayList<>();
                }
                unlinkedElements.add(unlinkLast());
            }
        } finally {
            lock.unlock();
        }
        if(mOnElementUnexpectedRemoveListener != null && unlinkedElements != null){
            for(Object obj : unlinkedElements){
                mOnElementUnexpectedRemoveListener.onUnexpectedRemove(obj);
            }
        }
    }

    /**
     * [增强]闯入方式入队(队尾)<br/>
     * 若队列未满, 则插入队列尾部<br/>
     * 若队列已满, 强行移除队尾的元素, 并将自己插入队列尾部<br/>
     * @param e
     */
    public void intrude(E e){
        intrudeLast(e);
    }

    /*********************************************************
     * public 移除元素
     */

    /**
     * [增强]把队列中所有等于指定对象的元素全部移除(根据元素.equals()判断)<br/>
     * 在元素不可重复模式下, 建议使用remove(Object);
     * @param o 指定对象
     */
    public boolean removeAll(Object o){
        if (o == null) {
            return false;
        }
        final ReentrantLock lock = this.lock;
        boolean result = false;
        lock.lock();
        try {
            for (Node<E> p = first; p != null; p = p.next) {
                if (o.equals(p.item)) {
                    unlink(p);
                    result = true;
                }
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    /*********************************************************
     * 监听器
     */

    /**
     * 设置元素意外移除监听器<br/>
     * 当队列中的元素因为入队操作被意外移除队列的情况回调监听<br/>
     * 1.元素不可重复模式时,向队列添加重复元素后,触发回调并传入被移除的元素对象<Br/>
     * 2.使用intrude/intrudeFirst/intrudeLast闯入式入队方法, 若队列中原有元素因此被
     * 移除后, 触发回调并传入被移除的元素对象<Br/>
     *
     * @param listener 监听器
     */
    public void setOnElementRemoveListener(OnElementUnexpectedRemoveListener listener) {
        this.mOnElementUnexpectedRemoveListener = listener;
    }

    /*********************************************************
     * 内部类
     */

    /**
     * [内部类]元素意外移除监听器<br/>
     * 当队列中的元素因为入队操作被意外移除队列的情况回调监听<br/>
     * 1.元素不可重复模式时,向队列添加重复元素后,触发回调并传入被移除的元素对象<Br/>
     * 2.使用intrude/intrudeFirst/intrudeLast闯入式入队方法, 若队列中原有元素因此被
     * 移除后, 触发回调并传入被移除的元素对象<Br/>
     *
     */
    public interface OnElementUnexpectedRemoveListener {
        /**
         * 当元素被意外删除时调用
         * @param element 被删除的元素
         */
        void onUnexpectedRemove(Object element);
    }

}
