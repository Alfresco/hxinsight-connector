package org.alfresco.hxi_connector.e2e_test.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.util.Set;

public final class TestJsonUtils
{
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public static Set<String> getSetProperty(JsonNode jsonNode, String propertyName)
    {
        return objectMapper.readValue(jsonNode.get(propertyName).get("value").toString(), new TypeReference<>() {});
    }
}
