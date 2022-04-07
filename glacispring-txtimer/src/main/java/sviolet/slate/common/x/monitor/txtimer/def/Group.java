/*
 * Copyright (C) 2015-2018 S.Violet
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
 * Project GitHub: https://github.com/shepherdviolet/slate
 * Email: shepherdviolet@163.com
 */

package sviolet.slate.common.x.monitor.txtimer.def;

import sviolet.thistle.model.concurrent.lock.UnsafeSpinLock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class Group {

    private DefaultTxTimerProvider2 provider;

    Map<String, Transaction> transactions;

    Group(DefaultTxTimerProvider2 provider) {
        this.provider = provider;
        transactions = new ConcurrentHashMap<>(provider.mapInitCap);
    }

    Transaction getTransaction(String transactionName){
        Transaction transaction = transactions.get(transactionName);
        if (transaction == null) {
            //用StringHashLocks分散碰撞的可能性
            @SuppressWarnings("deprecation")
            UnsafeSpinLock lock = provider.locks.getLock(transactionName);
            try {
                lock.lock();
                transaction = transactions.get(transactionName);
                if (transaction == null) {
                    transaction = new Transaction(provider);
                    transactions.put(transactionName, transaction);
                }
            } finally {
                lock.unlock();
            }
        }
        return transaction;
    }

}
