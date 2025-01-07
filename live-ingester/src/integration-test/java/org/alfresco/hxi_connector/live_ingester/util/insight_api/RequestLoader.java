/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
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
public class RequestLoader
{
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
