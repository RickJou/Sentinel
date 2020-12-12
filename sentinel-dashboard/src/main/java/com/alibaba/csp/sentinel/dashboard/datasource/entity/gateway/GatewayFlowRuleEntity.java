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
package com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayParamFlowItem;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.slots.block.Rule;

import java.util.Date;
import java.util.Objects;

/**
 * Entity for {@link GatewayFlowRule}.
 *
 * @author cdfive
 * @since 1.7.0
 */
public class GatewayFlowRuleEntity implements RuleEntity {

    /**间隔单位*/
    /**
     * 0-秒
     */
    public static final int INTERVAL_UNIT_SECOND = 0;
    /**
     * 1-分
     */
    public static final int INTERVAL_UNIT_MINUTE = 1;
    /**
     * 2-时
     */
    public static final int INTERVAL_UNIT_HOUR = 2;
    /**
     * 3-天
     */
    public static final int INTERVAL_UNIT_DAY = 3;

    private Long id;
    private String app;
    private String ip;
    private Integer port;

    private Date gmtCreate;
    private Date gmtModified;

    private String resource;
    private Integer resourceMode;

    private Integer grade;
    private Double count;
    private Long interval;
    private Integer intervalUnit;

    private Integer controlBehavior;
    private Integer burst;

    private Integer maxQueueingTimeoutMs;

    private GatewayParamFlowItemEntity paramItem;

    //是否使用自定义返回的流控报文
    private boolean useResponseBody;

    //返回自定义http status
    private Integer limitStatus;

    //返回自定义报文内容
    private String limitResponseBody;


    public static Long calIntervalSec(Long interval, Integer intervalUnit) {
        switch (intervalUnit) {
            case INTERVAL_UNIT_SECOND:
                return interval;
            case INTERVAL_UNIT_MINUTE:
                return interval * 60;
            case INTERVAL_UNIT_HOUR:
                return interval * 60 * 60;
            case INTERVAL_UNIT_DAY:
                return interval * 60 * 60 * 24;
            default:
                break;
        }

        throw new IllegalArgumentException("Invalid intervalUnit: " + intervalUnit);
    }

    public static Object[] parseIntervalSec(Long intervalSec) {
        if (intervalSec % (60 * 60 * 24) == 0) {
            return new Object[]{intervalSec / (60 * 60 * 24), INTERVAL_UNIT_DAY};
        }

        if (intervalSec % (60 * 60) == 0) {
            return new Object[]{intervalSec / (60 * 60), INTERVAL_UNIT_HOUR};
        }

        if (intervalSec % 60 == 0) {
            return new Object[]{intervalSec / 60, INTERVAL_UNIT_MINUTE};
        }

        return new Object[]{intervalSec, INTERVAL_UNIT_SECOND};
    }

    public GatewayFlowRule toGatewayFlowRule() {
        GatewayFlowRule rule = new GatewayFlowRule();
        rule.setResource(resource);
        rule.setResourceMode(resourceMode);

        rule.setGrade(grade);
        rule.setCount(count);
        rule.setIntervalSec(calIntervalSec(interval, intervalUnit));

        rule.setControlBehavior(controlBehavior);

        if (burst != null) {
            rule.setBurst(burst);
        }

        if (maxQueueingTimeoutMs != null) {
            rule.setMaxQueueingTimeoutMs(maxQueueingTimeoutMs);
        }

        if (paramItem != null) {
            GatewayParamFlowItem ruleItem = new GatewayParamFlowItem();
            rule.setParamItem(ruleItem);
            ruleItem.setParseStrategy(paramItem.getParseStrategy());
            ruleItem.setFieldName(paramItem.getFieldName());
            ruleItem.setPattern(paramItem.getPattern());

            if (paramItem.getMatchStrategy() != null) {
                ruleItem.setMatchStrategy(paramItem.getMatchStrategy());
            }
        }

        return rule;
    }

    public static GatewayFlowRuleEntity fromGatewayFlowRule(String app, String ip, Integer port, GatewayFlowRule rule) {
        GatewayFlowRuleEntity entity = new GatewayFlowRuleEntity();
        entity.setApp(app);
        entity.setIp(ip);
        entity.setPort(port);

        entity.setResource(rule.getResource());
        entity.setResourceMode(rule.getResourceMode());

        entity.setGrade(rule.getGrade());
        entity.setCount(rule.getCount());
        Object[] intervalSecResult = parseIntervalSec(rule.getIntervalSec());
        entity.setInterval((Long) intervalSecResult[0]);
        entity.setIntervalUnit((Integer) intervalSecResult[1]);

        entity.setControlBehavior(rule.getControlBehavior());
        entity.setBurst(rule.getBurst());
        entity.setMaxQueueingTimeoutMs(rule.getMaxQueueingTimeoutMs());

        GatewayParamFlowItem paramItem = rule.getParamItem();
        if (paramItem != null) {
            GatewayParamFlowItemEntity itemEntity = new GatewayParamFlowItemEntity();
            entity.setParamItem(itemEntity);
            itemEntity.setParseStrategy(paramItem.getParseStrategy());
            itemEntity.setFieldName(paramItem.getFieldName());
            itemEntity.setPattern(paramItem.getPattern());
            itemEntity.setMatchStrategy(paramItem.getMatchStrategy());
        }

        return entity;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    @Override
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    @Override
    public Rule toRule() {
        return null;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public GatewayParamFlowItemEntity getParamItem() {
        return paramItem;
    }

    public void setParamItem(GatewayParamFlowItemEntity paramItem) {
        this.paramItem = paramItem;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Integer getResourceMode() {
        return resourceMode;
    }

    public void setResourceMode(Integer resourceMode) {
        this.resourceMode = resourceMode;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    public Long getInterval() {
        return interval;
    }

    public void setInterval(Long interval) {
        this.interval = interval;
    }

    public Integer getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(Integer intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public Integer getControlBehavior() {
        return controlBehavior;
    }

    public void setControlBehavior(Integer controlBehavior) {
        this.controlBehavior = controlBehavior;
    }

    public Integer getBurst() {
        return burst;
    }

    public void setBurst(Integer burst) {
        this.burst = burst;
    }

    public Integer getMaxQueueingTimeoutMs() {
        return maxQueueingTimeoutMs;
    }

    public void setMaxQueueingTimeoutMs(Integer maxQueueingTimeoutMs) {
        this.maxQueueingTimeoutMs = maxQueueingTimeoutMs;
    }

    public boolean isUseResponseBody() {
        return useResponseBody;
    }

    public void setUseResponseBody(boolean useResponseBody) {
        this.useResponseBody = useResponseBody;
    }

    public Integer getLimitStatus() {
        return limitStatus;
    }

    public void setLimitStatus(Integer limitStatus) {
        this.limitStatus = limitStatus;
    }

    public String getLimitResponseBody() {
        return limitResponseBody;
    }

    public void setLimitResponseBody(String limitResponseBody) {
        this.limitResponseBody = limitResponseBody;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (!(o instanceof GatewayFlowRuleEntity)){ return false;}

        GatewayFlowRuleEntity that = (GatewayFlowRuleEntity) o;

        if (useResponseBody != that.useResponseBody) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (app != null ? !app.equals(that.app) : that.app != null) {
            return false;
        }
        if (ip != null ? !ip.equals(that.ip) : that.ip != null) {
            return false;
        }
        if (port != null ? !port.equals(that.port) : that.port != null) {
            return false;
        }
        if (gmtCreate != null ? !gmtCreate.equals(that.gmtCreate) : that.gmtCreate != null) {
            return false;
        }
        if (gmtModified != null ? !gmtModified.equals(that.gmtModified) : that.gmtModified != null) {
            return false;
        }
        if (resource != null ? !resource.equals(that.resource) : that.resource != null) {
            return false;
        }
        if (resourceMode != null ? !resourceMode.equals(that.resourceMode) : that.resourceMode != null) {
            return false;
        }
        if (grade != null ? !grade.equals(that.grade) : that.grade != null) {
            return false;
        }
        if (count != null ? !count.equals(that.count) : that.count != null) {
            return false;
        }
        if (interval != null ? !interval.equals(that.interval) : that.interval != null) {
            return false;
        }
        if (intervalUnit != null ? !intervalUnit.equals(that.intervalUnit) : that.intervalUnit != null) {
            return false;
        }
        if (controlBehavior != null ? !controlBehavior.equals(that.controlBehavior) : that.controlBehavior != null) {
            return false;
        }
        if (burst != null ? !burst.equals(that.burst) : that.burst != null) {
            return false;
        }
        if (maxQueueingTimeoutMs != null ? !maxQueueingTimeoutMs.equals(that.maxQueueingTimeoutMs) : that.maxQueueingTimeoutMs != null) {
            return false;
        }
        if (paramItem != null ? !paramItem.equals(that.paramItem) : that.paramItem != null) {
            return false;
        }
        if (limitStatus != null ? !limitStatus.equals(that.limitStatus) : that.limitStatus != null) {
            return false;
        }
        return limitResponseBody != null ? limitResponseBody.equals(that.limitResponseBody) : that.limitResponseBody == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (app != null ? app.hashCode() : 0);
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + (gmtCreate != null ? gmtCreate.hashCode() : 0);
        result = 31 * result + (gmtModified != null ? gmtModified.hashCode() : 0);
        result = 31 * result + (resource != null ? resource.hashCode() : 0);
        result = 31 * result + (resourceMode != null ? resourceMode.hashCode() : 0);
        result = 31 * result + (grade != null ? grade.hashCode() : 0);
        result = 31 * result + (count != null ? count.hashCode() : 0);
        result = 31 * result + (interval != null ? interval.hashCode() : 0);
        result = 31 * result + (intervalUnit != null ? intervalUnit.hashCode() : 0);
        result = 31 * result + (controlBehavior != null ? controlBehavior.hashCode() : 0);
        result = 31 * result + (burst != null ? burst.hashCode() : 0);
        result = 31 * result + (maxQueueingTimeoutMs != null ? maxQueueingTimeoutMs.hashCode() : 0);
        result = 31 * result + (paramItem != null ? paramItem.hashCode() : 0);
        result = 31 * result + (useResponseBody ? 1 : 0);
        result = 31 * result + (limitStatus != null ? limitStatus.hashCode() : 0);
        result = 31 * result + (limitResponseBody != null ? limitResponseBody.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GatewayFlowRuleEntity{" +
                "id=" + id +
                ", app='" + app + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", gmtCreate=" + gmtCreate +
                ", gmtModified=" + gmtModified +
                ", resource='" + resource + '\'' +
                ", resourceMode=" + resourceMode +
                ", grade=" + grade +
                ", count=" + count +
                ", interval=" + interval +
                ", intervalUnit=" + intervalUnit +
                ", controlBehavior=" + controlBehavior +
                ", burst=" + burst +
                ", maxQueueingTimeoutMs=" + maxQueueingTimeoutMs +
                ", paramItem=" + paramItem +
                '}';
    }
}
