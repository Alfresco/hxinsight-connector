package org.alfresco.hxi_connector.live_ingester.util.insight_api;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestLoader {
    private final static ObjectMapper JSON_MAPPER = new ObjectMapper();
    private final static ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    public static HxInsightRequest load(String path)
    {
        Map<String, Object> data;
        try (InputStream stream = HxInsightRequest.class.getResourceAsStream(path))
        {
            data = YAML_MAPPER.readValue(stream.readAllBytes(), HashMap.class);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
        Object body = data.get("body");
        String bodyAsString = null;
        if (body != null)
        {
            try
            {
                bodyAsString = JSON_MAPPER.writeValueAsString(body);
            }
            catch (JsonProcessingException e)
            {
                throw new LiveIngesterRuntimeException(e);
            }
        }
        return new HxInsightRequest((String) data.get("url"), (Map) data.get("headers"), bodyAsString);
    }
}

