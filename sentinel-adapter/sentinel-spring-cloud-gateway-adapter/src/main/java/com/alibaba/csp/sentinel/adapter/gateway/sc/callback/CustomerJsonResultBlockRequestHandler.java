/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.gateway.sc.callback;

import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.springframework.web.reactive.function.BodyInserters.fromObject;

/**
 * The default implementation of {@link BlockRequestHandler}.
 * Compatible with Spring WebFlux and Spring Cloud Gateway.
 *
 * @author Eric Zhao
 */
public class CustomerJsonResultBlockRequestHandler implements BlockRequestHandler {

    private static final String DEFAULT_BLOCK_MSG_PREFIX = "Blocked by Sentinel: ";

    private static final String responseByte = "{\"message\":\"flow limit\"}";

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable ex) {
        if (acceptsHtml(exchange)) {
            return htmlErrorResponse(ex);
        }

        // sentinel默认返回实现
        if (!(ex instanceof ParamFlowException)) {
            // JSON result by default.
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(fromObject(buildErrorResult(ex)));
        }

        //自定义返回报文实现
        ParamFlowException paramFlowException = (ParamFlowException) ex;
        ParamFlowRule rule = paramFlowException.getRule();
        //返回自定义的json报文
        JacksonJsonParser parser = new JacksonJsonParser();
        if (rule.isUseResponseBody()) {
            //返回自定义的http status
            HttpStatus httpStatus = HttpStatus.resolve(rule.getLimitStatus());
            if (null == httpStatus) {
                throw new IllegalArgumentException("HttpStatus.resolve(rule.getLimitStatus()) is null; rule.getLimitStatus() is:" + rule.getLimitStatus());
            }

            Map<String, Object> resMap = parser.parseMap(rule.getLimitResponseBody());
            return ServerResponse.status(httpStatus)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(fromObject(resMap));
        } else {
            Map<String, Object> resMap = parser.parseMap(rule.getLimitResponseBody());
            //兜底实现
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON_UTF8)
                    .body(fromObject(resMap));
        }
    }

    private Mono<ServerResponse> htmlErrorResponse(Throwable ex) {
        return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
            .contentType(MediaType.TEXT_PLAIN)
            .syncBody(DEFAULT_BLOCK_MSG_PREFIX + ex.getClass().getSimpleName());
    }

    private ErrorResult buildErrorResult(Throwable ex) {
        return new ErrorResult(HttpStatus.TOO_MANY_REQUESTS.value(),
            DEFAULT_BLOCK_MSG_PREFIX + ex.getClass().getSimpleName());
    }

    /**
     * Reference from {@code DefaultErrorWebExceptionHandler} of Spring Boot.
     */
    private boolean acceptsHtml(ServerWebExchange exchange) {
        try {
            List<MediaType> acceptedMediaTypes = exchange.getRequest().getHeaders().getAccept();
            acceptedMediaTypes.remove(MediaType.ALL);
            MediaType.sortBySpecificityAndQuality(acceptedMediaTypes);
            return acceptedMediaTypes.stream()
                .anyMatch(MediaType.TEXT_HTML::isCompatibleWith);
        } catch (InvalidMediaTypeException ex) {
            return false;
        }
    }

    private static class ErrorResult {
        private final int code;
        private final String message;

        ErrorResult(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
