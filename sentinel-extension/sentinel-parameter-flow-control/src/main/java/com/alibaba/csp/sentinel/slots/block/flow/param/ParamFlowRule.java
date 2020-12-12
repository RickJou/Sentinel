/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
 */
package com.alibaba.csp.sentinel.slots.block.flow.param;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

/**
 * Rules for "hot-spot" frequent parameter flow control.
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 * @since 0.2.0
 */
public class ParamFlowRule extends AbstractRule {

    public ParamFlowRule() {}

    public ParamFlowRule(String resourceName) {
        setResource(resourceName);
    }

    /**
     * The threshold type of flow control (0: thread count, 1: QPS).
     */
    private int grade = RuleConstant.FLOW_GRADE_QPS;

    /**
     * Parameter index.
     */
    private Integer paramIdx;

    /**
     * The threshold count.
     */
    private double count;

    /**
     * Traffic shaping behavior (since 1.6.0).
     */
    private int controlBehavior = RuleConstant.CONTROL_BEHAVIOR_DEFAULT;

    private int maxQueueingTimeMs = 0;
    private int burstCount = 0;
    private long durationInSec = 1;

    //是否使用自定义返回的流控报文
    private boolean useResponseBody;

    //返回自定义http status
    private Integer limitStatus;

    //返回自定义报文内容
    private String limitResponseBody;

    /**
     * Original exclusion items of parameters.
     */
    private List<ParamFlowItem> paramFlowItemList = new ArrayList<ParamFlowItem>();

    /**
     * Parsed exclusion items of parameters. Only for internal use.
     */
    private Map<Object, Integer> hotItems = new HashMap<Object, Integer>();

    /**
     * Indicating whether the rule is for cluster mode.
     */
    private boolean clusterMode = false;
    /**
     * Cluster mode specific config for parameter flow rule.
     */
    private ParamFlowClusterConfig clusterConfig;

    public int getControlBehavior() {
        return controlBehavior;
    }

    public ParamFlowRule setControlBehavior(int controlBehavior) {
        this.controlBehavior = controlBehavior;
        return this;
    }

    public int getMaxQueueingTimeMs() {
        return maxQueueingTimeMs;
    }

    public ParamFlowRule setMaxQueueingTimeMs(int maxQueueingTimeMs) {
        this.maxQueueingTimeMs = maxQueueingTimeMs;
        return this;
    }

    public int getBurstCount() {
        return burstCount;
    }

    public ParamFlowRule setBurstCount(int burstCount) {
        this.burstCount = burstCount;
        return this;
    }

    public long getDurationInSec() {
        return durationInSec;
    }

    public ParamFlowRule setDurationInSec(long durationInSec) {
        this.durationInSec = durationInSec;
        return this;
    }

    public int getGrade() {
        return grade;
    }

    public ParamFlowRule setGrade(int grade) {
        this.grade = grade;
        return this;
    }

    public Integer getParamIdx() {
        return paramIdx;
    }

    public ParamFlowRule setParamIdx(Integer paramIdx) {
        this.paramIdx = paramIdx;
        return this;
    }

    public double getCount() {
        return count;
    }

    public ParamFlowRule setCount(double count) {
        this.count = count;
        return this;
    }

    public List<ParamFlowItem> getParamFlowItemList() {
        return paramFlowItemList;
    }

    public ParamFlowRule setParamFlowItemList(List<ParamFlowItem> paramFlowItemList) {
        this.paramFlowItemList = paramFlowItemList;
        return this;
    }

    public Integer retrieveExclusiveItemCount(Object value) {
        if (value == null || hotItems == null) {
            return null;
        }
        return hotItems.get(value);
    }

    Map<Object, Integer> getParsedHotItems() {
        return hotItems;
    }

    ParamFlowRule setParsedHotItems(Map<Object, Integer> hotItems) {
        this.hotItems = hotItems;
        return this;
    }

    public boolean isClusterMode() {
        return clusterMode;
    }

    public ParamFlowRule setClusterMode(boolean clusterMode) {
        this.clusterMode = clusterMode;
        return this;
    }

    public ParamFlowClusterConfig getClusterConfig() {
        return clusterConfig;
    }

    public ParamFlowRule setClusterConfig(ParamFlowClusterConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
        return this;
    }

    public boolean isUseResponseBody() {
        return useResponseBody;
    }

    public ParamFlowRule setUseResponseBody(boolean useResponseBody) {
        this.useResponseBody = useResponseBody;
        return this;
    }

    public Integer getLimitStatus() {
        return limitStatus;
    }

    public ParamFlowRule setLimitStatus(Integer limitStatus) {
        this.limitStatus = limitStatus;
        return this;
    }

    public String getLimitResponseBody() {
        return limitResponseBody;
    }

    public ParamFlowRule setLimitResponseBody(String limitResponseBody) {
        this.limitResponseBody = limitResponseBody;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){ return true;}
        if (!(o instanceof ParamFlowRule)){ return false;}
        if (!super.equals(o)){ return false;}

        ParamFlowRule that = (ParamFlowRule) o;

        if (grade != that.grade) {
            return false;
        }
        if (Double.compare(that.count, count) != 0) {
            return false;
        }
        if (controlBehavior != that.controlBehavior) {
            return false;
        }
        if (maxQueueingTimeMs != that.maxQueueingTimeMs) {
            return false;
        }
        if (burstCount != that.burstCount) {
            return false;
        }
        if (durationInSec != that.durationInSec) {
            return false;
        }
        if (useResponseBody != that.useResponseBody) {
            return false;
        }
        if (clusterMode != that.clusterMode) {
            return false;
        }
        if (paramIdx != null ? !paramIdx.equals(that.paramIdx) : that.paramIdx != null) {
            return false;
        }
        if (limitStatus != null ? !limitStatus.equals(that.limitStatus) : that.limitStatus != null) {
            return false;
        }
        if (limitResponseBody != null ? !limitResponseBody.equals(that.limitResponseBody) : that.limitResponseBody != null) {
            return false;
        }
        if (paramFlowItemList != null ? !paramFlowItemList.equals(that.paramFlowItemList) : that.paramFlowItemList != null) {
            return false;
        }
        if (hotItems != null ? !hotItems.equals(that.hotItems) : that.hotItems != null) {
            return false;
        }
        return clusterConfig != null ? clusterConfig.equals(that.clusterConfig) : that.clusterConfig == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + grade;
        result = 31 * result + (paramIdx != null ? paramIdx.hashCode() : 0);
        temp = Double.doubleToLongBits(count);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + controlBehavior;
        result = 31 * result + maxQueueingTimeMs;
        result = 31 * result + burstCount;
        result = 31 * result + (int) (durationInSec ^ (durationInSec >>> 32));
        result = 31 * result + (useResponseBody ? 1 : 0);
        result = 31 * result + (limitStatus != null ? limitStatus.hashCode() : 0);
        result = 31 * result + (limitResponseBody != null ? limitResponseBody.hashCode() : 0);
        result = 31 * result + (paramFlowItemList != null ? paramFlowItemList.hashCode() : 0);
        result = 31 * result + (hotItems != null ? hotItems.hashCode() : 0);
        result = 31 * result + (clusterMode ? 1 : 0);
        result = 31 * result + (clusterConfig != null ? clusterConfig.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ParamFlowRule{" +
            "grade=" + grade +
            ", paramIdx=" + paramIdx +
            ", count=" + count +
            ", controlBehavior=" + controlBehavior +
            ", maxQueueingTimeMs=" + maxQueueingTimeMs +
            ", burstCount=" + burstCount +
            ", durationInSec=" + durationInSec +
            ", paramFlowItemList=" + paramFlowItemList +
            ", clusterMode=" + clusterMode +
            ", clusterConfig=" + clusterConfig +
            '}';
    }
}
