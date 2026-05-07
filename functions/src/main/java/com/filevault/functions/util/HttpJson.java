package com.filevault.functions.util;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

public final class HttpJson {

    private HttpJson() {
    }

    public static HttpResponseMessage ok(HttpRequestMessage<?> request, Object body) throws Exception {
        String json = JsonUtil.MAPPER.writeValueAsString(body);
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(json)
                .build();
    }

    public static HttpResponseMessage created(HttpRequestMessage<?> request, Object body) throws Exception {
        String json = JsonUtil.MAPPER.writeValueAsString(body);
        return request.createResponseBuilder(HttpStatus.CREATED)
                .header("Content-Type", "application/json")
                .body(json)
                .build();
    }

    public static HttpResponseMessage json(HttpRequestMessage<?> request, HttpStatus status, Object body) throws Exception {
        String json = JsonUtil.MAPPER.writeValueAsString(body);
        return request.createResponseBuilder(status)
                .header("Content-Type", "application/json")
                .body(json)
                .build();
    }

    public static HttpResponseMessage rawJson(HttpRequestMessage<?> request, HttpStatus status, String json) {
        return request.createResponseBuilder(status)
                .header("Content-Type", "application/json")
                .body(json)
                .build();
    }

    public static HttpResponseMessage noContent(HttpRequestMessage<?> request) {
        return request.createResponseBuilder(HttpStatus.NO_CONTENT).build();
    }
}
