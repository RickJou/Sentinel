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
package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

/**
 * <p>
 * Each flow rule is mainly composed of three factors: <strong>grade</strong>,
 * <strong>strategy</strong> and <strong>controlBehavior</strong>:
 * </p>
 * <ul>
 *     <li>The {@link #grade} represents the threshold type of flow control (by QPS or thread count).</li>
 *     <li>The {@link #strategy} represents the strategy based on invocation relation.</li>
 *     <li>The {@link #controlBehavior} represents the QPS shaping behavior (actions on incoming request when QPS
 *     exceeds the threshold).</li>
 * </ul>
 *
 * @author jialiang.linjl
 * @author Eric Zhao
 */
public class FlowRule extends AbstractRule {

    public FlowRule() {
        super();
        setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
    }

    public FlowRule(String resourceName) {
        super();
        setResource(resourceName);
        setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
    }

    /**
     * The threshold type of flow control (0: thread count, 1: QPS).
     */
    private int grade = RuleConstant.FLOW_GRADE_QPS;

    /**
     * Flow control threshold count.
     */
    private double count;

    /**
     * Flow control strategy based on invocation chain.
     *
     * {@link RuleConstant#STRATEGY_DIRECT} for direct flow control (by origin);
     * {@link RuleConstant#STRATEGY_RELATE} for relevant flow control (with relevant resource);
     * {@link RuleConstant#STRATEGY_CHAIN} for chain flow control (by entrance resource).
     */
    private int strategy = RuleConstant.STRATEGY_DIRECT;

    /**
     * Reference resource in flow control with relevant resource or context.
     */
    private String refResource;

    /**
     * Rate limiter control behavior.
     * 0. default(reject directly), 1. warm up, 2. rate limiter, 3. warm up + rate limiter
     */
    private int controlBehavior = RuleConstant.CONTROL_BEHAVIOR_DEFAULT;

    private int warmUpPeriodSec = 10;

    /**
     * Max queueing time in rate limiter behavior.
     */
    private int maxQueueingTimeMs = 500;

    private boolean clusterMode;
    /**
     * Flow rule config for cluster mode.
     */
    private ClusterFlowConfig clusterConfig;

    /**
     * The traffic shaping (throttling) controller.
     */
    private TrafficShapingController controller;


    //是否使用自定义返回的流控报文
    private boolean useResponseBody;

    //返回自定义http status
    private Integer limitStatus;

    //返回自定义报文内容
    private String limitResponseBody;

    public int getControlBehavior() {
        return controlBehavior;
    }

    public FlowRule setControlBehavior(int controlBehavior) {
        this.controlBehavior = controlBehavior;
        return this;
    }

    public int getMaxQueueingTimeMs() {
        return maxQueueingTimeMs;
    }

    public FlowRule setMaxQueueingTimeMs(int maxQueueingTimeMs) {
        this.maxQueueingTimeMs = maxQueueingTimeMs;
        return this;
    }

    FlowRule setRater(TrafficShapingController rater) {
        this.controller = rater;
        return this;
    }

    TrafficShapingController getRater() {
        return controller;
    }

    public int getWarmUpPeriodSec() {
        return warmUpPeriodSec;
    }

    public FlowRule setWarmUpPeriodSec(int warmUpPeriodSec) {
        this.warmUpPeriodSec = warmUpPeriodSec;
        return this;
    }

    public int getGrade() {
        return grade;
    }

    public FlowRule setGrade(int grade) {
        this.grade = grade;
        return this;
    }

    public double getCount() {
        return count;
    }

    public FlowRule setCount(double count) {
        this.count = count;
        return this;
    }

    public int getStrategy() {
        return strategy;
    }

    public FlowRule setStrategy(int strategy) {
        this.strategy = strategy;
        return this;
    }

    public String getRefResource() {
        return refResource;
    }

    public FlowRule setRefResource(String refResource) {
        this.refResource = refResource;
        return this;
    }

    public boolean isClusterMode() {
        return clusterMode;
    }

    public FlowRule setClusterMode(boolean clusterMode) {
        this.clusterMode = clusterMode;
        return this;
    }

    public ClusterFlowConfig getClusterConfig() {
        return clusterConfig;
    }

    public FlowRule setClusterConfig(ClusterFlowConfig clusterConfig) {
        this.clusterConfig = clusterConfig;
        return this;
    }

    public boolean isUseResponseBody() {
        return useResponseBody;
    }

    public FlowRule setUseResponseBody(boolean useResponseBody) {
        this.useResponseBody = useResponseBody;
        return this;
    }

    public Integer getLimitStatus() {
        return limitStatus;
    }

    public FlowRule setLimitStatus(Integer limitStatus) {
        this.limitStatus = limitStatus;
        return this;
    }

    public String getLimitResponseBody() {
        return limitResponseBody;
    }

    public FlowRule setLimitResponseBody(String limitResponseBody) {
        this.limitResponseBody = limitResponseBody;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlowRule)) return false;
        if (!super.equals(o)) return false;

        FlowRule flowRule = (FlowRule) o;

        if (grade != flowRule.grade) return false;
        if (Double.compare(flowRule.count, count) != 0) return false;
        if (strategy != flowRule.strategy) return false;
        if (controlBehavior != flowRule.controlBehavior) return false;
        if (warmUpPeriodSec != flowRule.warmUpPeriodSec) return false;
        if (maxQueueingTimeMs != flowRule.maxQueueingTimeMs) return false;
        if (clusterMode != flowRule.clusterMode) return false;
        if (useResponseBody != flowRule.useResponseBody) return false;
        if (refResource != null ? !refResource.equals(flowRule.refResource) : flowRule.refResource != null)
            return false;
        if (clusterConfig != null ? !clusterConfig.equals(flowRule.clusterConfig) : flowRule.clusterConfig != null)
            return false;
        if (controller != null ? !controller.equals(flowRule.controller) : flowRule.controller != null) return false;
        if (limitStatus != null ? !limitStatus.equals(flowRule.limitStatus) : flowRule.limitStatus != null)
            return false;
        return limitResponseBody != null ? limitResponseBody.equals(flowRule.limitResponseBody) : flowRule.limitResponseBody == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + grade;
        temp = Double.doubleToLongBits(count);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + strategy;
        result = 31 * result + (refResource != null ? refResource.hashCode() : 0);
        result = 31 * result + controlBehavior;
        result = 31 * result + warmUpPeriodSec;
        result = 31 * result + maxQueueingTimeMs;
        result = 31 * result + (clusterMode ? 1 : 0);
        result = 31 * result + (clusterConfig != null ? clusterConfig.hashCode() : 0);
        result = 31 * result + (controller != null ? controller.hashCode() : 0);
        result = 31 * result + (useResponseBody ? 1 : 0);
        result = 31 * result + (limitStatus != null ? limitStatus.hashCode() : 0);
        result = 31 * result + (limitResponseBody != null ? limitResponseBody.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FlowRule{" +
            "resource=" + getResource() +
            ", limitApp=" + getLimitApp() +
            ", grade=" + grade +
            ", count=" + count +
            ", strategy=" + strategy +
            ", refResource=" + refResource +
            ", controlBehavior=" + controlBehavior +
            ", warmUpPeriodSec=" + warmUpPeriodSec +
            ", maxQueueingTimeMs=" + maxQueueingTimeMs +
            ", clusterMode=" + clusterMode +
            ", clusterConfig=" + clusterConfig +
            ", controller=" + controller +
            '}';
    }
}
