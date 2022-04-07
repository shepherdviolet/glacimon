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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import sviolet.thistle.util.judge.CheckUtils;

/**
 * 用于Spring容器中修改TxTimer配置(仅支持部分配置), 引入本配置类即可.
 *
 * <code>@Import(DefaultTxTimerSpringConfig.class)</code>
 *
 * @author S.Violet
 */
@Configuration
public class DefaultTxTimerSpringConfig {

    @Value("${slate.txtimer.reportall.interval:}")
    private void setReportAllInterval(String reportAllInterval){
        if (!CheckUtils.isEmptyOrBlank(reportAllInterval)){
            DefaultTxTimerConfig.setReportAllInterval(reportAllInterval);
        }
    }

    @Value("${slate.txtimer.threshold.avg:}")
    private void setThresholdAvg(String avg){
        if (!CheckUtils.isEmptyOrBlank(avg)){
            DefaultTxTimerConfig.setThresholdAvg(avg);
        }
    }

    @Value("${slate.txtimer.threshold.max:}")
    private void setThresholdMax(String max){
        if (!CheckUtils.isEmptyOrBlank(max)){
            DefaultTxTimerConfig.setThresholdMax(max);
        }
    }

    @Value("${slate.txtimer.threshold.min:}")
    private void setThresholdMin(String min){
        if (!CheckUtils.isEmptyOrBlank(min)){
            DefaultTxTimerConfig.setThresholdMin(min);
        }
    }

    @Value("${slate.txtimer.report.printpermin:}")
    private void setReportPrintsPerMinute(String reportPrintsPerMinute){
        if (!CheckUtils.isEmptyOrBlank(reportPrintsPerMinute)){
            DefaultTxTimerConfig.setReportPrintsPerMinute(reportPrintsPerMinute);
        }
    }

}
