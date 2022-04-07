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

package sviolet.slate.common.helper.rocketmq.producer;

import org.apache.rocketmq.common.message.Message;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>用于将RocketMQ的批量消息拆分成1M, 进行分批发送(官方DEMO)</p>
 *
 * <code>
 *         //Messages of the same batch should have: same topic, same waitStoreMsgOK and no schedule support.
 *         String topic = "simple";
 *         List<Message> messages = new ArrayList<>();
 *         messages.add(new Message(topic, "A", "Hello batch 1".getBytes()));
 *         messages.add(new Message(topic, "B", "Hello batch 2".getBytes()));
 *         messages.add(new Message(topic, "C", "Hello batch 3".getBytes()));
 *         MessageListSplitter splitter = new MessageListSplitter(messages);
 *         while (splitter.hasNext()) {
 *             List<Message> listItem = splitter.next();
 *             defaultProducer.send(listItem);
 *         }
 * </code>
 *
 * @author from rocketmq demo
 */
public class RocketMqMessageListSplitter implements Iterator<List<Message>> {

    private static final int SIZE_LIMIT = 1000 * 1000;
    private final List<Message> messages;
    private int currIndex;

    public RocketMqMessageListSplitter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public boolean hasNext() {
        return currIndex < messages.size();
    }

    @Override
    public List<Message> next() {
        int nextIndex = currIndex;
        int totalSize = 0;
        for (; nextIndex < messages.size(); nextIndex++) {
            Message message = messages.get(nextIndex);
            int tmpSize = message.getTopic().length() + message.getBody().length;
            Map<String, String> properties = message.getProperties();
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                tmpSize += entry.getKey().length() + entry.getValue().length();
            }
            //for log overhead
            tmpSize = tmpSize + 20;
            if (tmpSize > SIZE_LIMIT) {
                //it is unexpected that single message exceeds the SIZE_LIMIT
                //here just let it go, otherwise it will block the splitting process
                if (nextIndex - currIndex == 0) {
                    //if the next sublist has no element, add this one and then break, otherwise just break
                    nextIndex++;
                }
                break;
            }
            if (tmpSize + totalSize > SIZE_LIMIT) {
                break;
            } else {
                totalSize += tmpSize;
            }

        }
        List<Message> subList = messages.subList(currIndex, nextIndex);
        currIndex = nextIndex;
        return subList;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

}