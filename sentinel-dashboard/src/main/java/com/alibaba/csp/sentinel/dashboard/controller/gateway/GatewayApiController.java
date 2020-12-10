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
package com.alibaba.csp.sentinel.dashboard.controller.gateway;

import com.alibaba.csp.sentinel.dashboard.apollo.provider.ApiRuleApolloProvider;
import com.alibaba.csp.sentinel.dashboard.apollo.publish.ApiRuleApolloPublisher;
import com.alibaba.csp.sentinel.dashboard.auth.AuthAction;
import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.client.SentinelApiClient;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiPredicateItemEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.api.AddApiReqVo;
import com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.api.ApiPredicateItemVo;
import com.alibaba.csp.sentinel.dashboard.domain.vo.gateway.api.UpdateApiReqVo;
import com.alibaba.csp.sentinel.dashboard.repository.gateway.InMemApiDefinitionStore;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants.*;

/**
 * Gateway api Controller for manage gateway api definitions.
 *
 * @author cdfive
 * @since 1.7.0
 */
@RestController
@RequestMapping(value = "/gateway/api")
public class GatewayApiController {

    private final Logger logger = LoggerFactory.getLogger(GatewayApiController.class);

    @Autowired
    private InMemApiDefinitionStore repository;

    @Autowired
    private SentinelApiClient sentinelApiClient;

    @Autowired
    private ApiRuleApolloProvider apiRuleApolloProvider;
    @Autowired
    private ApiRuleApolloPublisher apiRuleApolloPublisher;

    @GetMapping("/list.json")
    @AuthAction(AuthService.PrivilegeType.READ_RULE)
    public Result<List<ApiDefinitionEntity>> queryApis(String app, String ip, Integer port) {

        if (StringUtil.isEmpty(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }

        //从apollo中进行查询
        try {
            List<ApiDefinitionEntity> rules = refreshAllApolloRules(app);
            return Result.ofSuccess(rules);
        } catch (Throwable throwable) {
            logger.error("query gateway flow rules by apollo error:", throwable);
            return Result.ofThrowable(-1, throwable);
        }
    }

    @PostMapping("/new.json")
    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)
    public Result<ApiDefinitionEntity> addApi(HttpServletRequest request, @RequestBody AddApiReqVo reqVo) {

        String app = reqVo.getApp();
        if (StringUtil.isBlank(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }

        ApiDefinitionEntity entity = new ApiDefinitionEntity();
        entity.setApp(app.trim());

        String ip = reqVo.getIp();
        if (StringUtil.isBlank(ip)) {
            return Result.ofFail(-1, "ip can't be null or empty");
        }
        entity.setIp(ip.trim());

        Integer port = reqVo.getPort();
        if (port == null) {
            return Result.ofFail(-1, "port can't be null");
        }
        entity.setPort(port);

        // API名称
        String apiName = reqVo.getApiName();
        if (StringUtil.isBlank(apiName)) {
            return Result.ofFail(-1, "apiName can't be null or empty");
        }
        entity.setApiName(apiName.trim());

        // 匹配规则列表
        List<ApiPredicateItemVo> predicateItems = reqVo.getPredicateItems();
        if (CollectionUtils.isEmpty(predicateItems)) {
            return Result.ofFail(-1, "predicateItems can't empty");
        }

        List<ApiPredicateItemEntity> predicateItemEntities = new ArrayList<>();
        for (ApiPredicateItemVo predicateItem : predicateItems) {
            ApiPredicateItemEntity predicateItemEntity = new ApiPredicateItemEntity();

            // 匹配模式
            Integer matchStrategy = predicateItem.getMatchStrategy();
            if (!Arrays.asList(URL_MATCH_STRATEGY_EXACT, URL_MATCH_STRATEGY_PREFIX, URL_MATCH_STRATEGY_REGEX).contains(matchStrategy)) {
                return Result.ofFail(-1, "invalid matchStrategy: " + matchStrategy);
            }
            predicateItemEntity.setMatchStrategy(matchStrategy);

            // 匹配串
            String pattern = predicateItem.getPattern();
            if (StringUtil.isBlank(pattern)) {
                return Result.ofFail(-1, "pattern can't be null or empty");
            }
            predicateItemEntity.setPattern(pattern);

            predicateItemEntities.add(predicateItemEntity);
        }
        entity.setPredicateItems(new LinkedHashSet<>(predicateItemEntities));

        // 检查API名称不能重复
        /*List<ApiDefinitionEntity> allApis = repository.findAllByMachine(MachineInfo.of(app.trim(), ip.trim(), port));
        if (allApis.stream().map(o -> o.getApiName()).anyMatch(o -> o.equals(apiName.trim()))) {
            return Result.ofFail(-1, "apiName exists: " + apiName);
        }*/

        Date date = new Date();
        entity.setGmtCreate(date);
        entity.setGmtModified(date);

        //保存规则至apollo
        addRuleToApollo(app, entity);

        return Result.ofSuccess(entity);
    }

    @PostMapping("/save.json")
    @AuthAction(AuthService.PrivilegeType.WRITE_RULE)
    public Result<ApiDefinitionEntity> updateApi(@RequestBody UpdateApiReqVo reqVo) {
        String app = reqVo.getApp();
        if (StringUtil.isBlank(app)) {
            return Result.ofFail(-1, "app can't be null or empty");
        }

        Long id = reqVo.getId();
        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }

        ApiDefinitionEntity entity = repository.findById(id);
        if (entity == null) {
            return Result.ofFail(-1, "api does not exist, id=" + id);
        }

        // 匹配规则列表
        List<ApiPredicateItemVo> predicateItems = reqVo.getPredicateItems();
        if (CollectionUtils.isEmpty(predicateItems)) {
            return Result.ofFail(-1, "predicateItems can't empty");
        }

        List<ApiPredicateItemEntity> predicateItemEntities = new ArrayList<>();
        for (ApiPredicateItemVo predicateItem : predicateItems) {
            ApiPredicateItemEntity predicateItemEntity = new ApiPredicateItemEntity();

            // 匹配模式
            int matchStrategy = predicateItem.getMatchStrategy();
            if (!Arrays.asList(URL_MATCH_STRATEGY_EXACT, URL_MATCH_STRATEGY_PREFIX, URL_MATCH_STRATEGY_REGEX).contains(matchStrategy)) {
                return Result.ofFail(-1, "Invalid matchStrategy: " + matchStrategy);
            }
            predicateItemEntity.setMatchStrategy(matchStrategy);

            // 匹配串
            String pattern = predicateItem.getPattern();
            if (StringUtil.isBlank(pattern)) {
                return Result.ofFail(-1, "pattern can't be null or empty");
            }
            predicateItemEntity.setPattern(pattern);

            predicateItemEntities.add(predicateItemEntity);
        }
        entity.setPredicateItems(new LinkedHashSet<>(predicateItemEntities));

        Date date = new Date();
        entity.setGmtModified(date);

        //保存规则至apollo
        if (!saveRuleToApollo(app, entity)) {
            return Result.ofFail(-1, "找不到对应的规则,无法修改");
        }

        return Result.ofSuccess(entity);
    }

    @PostMapping("/delete.json")
    @AuthAction(AuthService.PrivilegeType.DELETE_RULE)

    public Result<Long> deleteApi(Long id) {
        if (id == null) {
            return Result.ofFail(-1, "id can't be null");
        }

        //从apollo中删除
        ApiDefinitionEntity entity = repository.findById(id);
        deleteRuleByApollo(entity);

        return Result.ofSuccess(id);
    }

    private boolean publishApis(String app, String ip, Integer port) {
        List<ApiDefinitionEntity> apis = repository.findAllByMachine(MachineInfo.of(app, ip, port));
        return sentinelApiClient.modifyApis(app, ip, port, apis);
    }


    /**
     * 规则列表中是否包含该规则
     *
     * @param rules
     * @param rule
     * @return null:不存在; or 元素所在的下标
     */
    private Integer containerRule(List<ApiDefinitionEntity> rules, ApiDefinitionEntity rule) {
        for (int i = 0; i < rules.size(); i++) {
            boolean resourceModelEquals = StringUtils.equals(rules.get(i).getApiName(), rule.getApiName());

            //api类型和api名称认为是唯一的key
            if (resourceModelEquals) {
                return i;
            }
        }
        return null;
    }

    /**
     * 删除apollo中的规则
     *
     * @param entity
     * @return
     */
    private boolean deleteRuleByApollo(ApiDefinitionEntity entity) {
        List<ApiDefinitionEntity> rules = apiRuleApolloProvider.getRules(entity.getApp());

        Integer index = containerRule(rules, entity);
        if (index != null) {
            rules.remove(index.intValue());
        }

        try {
            apiRuleApolloPublisher.publish(entity.getApp(), rules);
        } catch (Exception ex) {
            logger.error("delete gateway flow rule by Apollo error:", ex);
            return false;
        }

        return true;
    }


    /**
     * 添加规则
     *
     * @param app
     * @param entity
     * @return
     */
    private boolean addRuleToApollo(String app, ApiDefinitionEntity entity) {
        try {
            List<ApiDefinitionEntity> rules = apiRuleApolloProvider.getRules(app);
            rules.add(entity);
            apiRuleApolloPublisher.publish(app, rules);
        } catch (Exception ex) {
            logger.error("save gateway flow rule to Apollo error:", ex);
            return false;
        }

        return true;
    }

    /**
     * 保存规则
     *
     * @return
     */
    private boolean saveRuleToApollo(String app, ApiDefinitionEntity entity) {
        List<ApiDefinitionEntity> rules = apiRuleApolloProvider.getRules(app);

        Integer index = containerRule(rules, entity);
        if (index != null) {
            rules.set(index, entity);
        } else {
            logger.info("找不到对应的规则,无法修改");
            return false;
        }

        try {
            apiRuleApolloPublisher.publish(app, rules);
        } catch (Exception ex) {
            logger.error("save gateway flow rule to Apollo error:", ex);
            return false;
        }

        return true;
    }


    /**
     * 将apollo中的规则刷新到内存中
     *
     * @param app
     */
    private List<ApiDefinitionEntity> refreshAllApolloRules(String app) {
        List<ApiDefinitionEntity> rules = apiRuleApolloProvider.getRules(app);
        repository.saveAll(rules);
        return rules;
    }
}
