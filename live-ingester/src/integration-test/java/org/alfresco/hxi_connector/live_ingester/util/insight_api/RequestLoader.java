/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2024 Alfresco Software Limited
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

public class RequestLoader
{
    static ObjectMapper jsonMapper = new ObjectMapper();
    static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public static HxInsightRequest load(String path)
    {
        InputStream stream = HxInsightRequest.class.getResourceAsStream(path);
        HashMap<String, Object> data;
        try
        {
            data = yamlMapper.readValue(stream.readAllBytes(), HashMap.class);
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
                bodyAsString = jsonMapper.writeValueAsString(body);
            }
            catch (JsonProcessingException e)
            {
                throw new RuntimeException(e);
            }
        }
        return new HxInsightRequest((String) data.get("url"), (Map) data.get("headers"), bodyAsString);
    }
}
