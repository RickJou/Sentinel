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
package com.alibaba.csp.sentinel.dashboard.apollo.provider;

import com.alibaba.csp.sentinel.dashboard.apollo.ApolloConfigUtil;
import com.alibaba.csp.sentinel.dashboard.apollo.ApolloProvider;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author hantianwei@gmail.com
 * @since 1.5.0
 */
@Component("gatewayFlowRuleApolloProvider")
public class GatewayFlowRuleApolloProvider implements DynamicRuleProvider<List<GatewayFlowRuleEntity>> {

    @Autowired
    private ApolloProvider apolloProvider;


    /**
     * 获取apollo中网关流控规则
     *
     * @param appName
     * @return
     * @throws Exception
     */
    @Override
    public List<GatewayFlowRuleEntity> getRules(String appName) {
        String flowDataId = ApolloConfigUtil.getGateWayFlowDataId(appName);
        return (List<GatewayFlowRuleEntity>) apolloProvider.getRules(appName, flowDataId, GatewayFlowRuleEntity.class);
    }


}
