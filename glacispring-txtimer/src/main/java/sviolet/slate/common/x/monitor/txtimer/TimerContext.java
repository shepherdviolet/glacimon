/*
 * Copyright (C) 2015-2019 S.Violet
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

package sviolet.slate.common.x.monitor.txtimer;

/**
 * 开始计时后, 会返回这个对象, 在计时结束时调用这个对象的exit方法即可
 *
 * @author S.Violet
 */
public interface TimerContext extends AutoCloseable {

    /**
     * 结束计时
     */
    void exit(int resultCode);

    /**
     * 结束计时
     */
    void exit();

    /**
     * 结束计时, 等同于exit
     */
    @Override
    void close();

    /**
     * TimerContext的基础实现
     */
    class Basic implements TimerContext {

        @Override
        public void exit(int resultCode) {
            TxTimer.exit(this, resultCode);
        }

        @Override
        public void exit() {
            exit(0);
        }

        @Override
        public void close() {
            exit(0);
        }

    }

}
