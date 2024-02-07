/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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
package org.alfresco.hxi_connector.bulk_ingester.processor.mapper.config;

import java.io.IOException;
import java.util.Map;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.validation.annotation.Validated;

import org.alfresco.hxi_connector.bulk_ingester.processor.mapper.NamespacePrefixMapper;

@Data
@Slf4j
@Validated
@Configuration
@ConfigurationProperties
@PropertySource(value = "${alfresco.bulk.ingest.namespace-prefixes-mapping}", factory = PredefinedNamespacePrefixMapper.JsonPropertySourceFactory.class)
public class PredefinedNamespacePrefixMapper implements NamespacePrefixMapper
{
    @NotNull private Map<String, String> prefixUriMap;

    @Override
    public String toPrefixedName(String namespace, String localName)
    {
        String prefix = prefixUriMap.get(namespace);

        if (prefix == null)
        {
            log.warn("Cannot calculate prefixed name of {} (unknown namespace: {})", localName, namespace);

            return localName;
        }

        return prefix + ":" + localName;
    }

    public static class JsonPropertySourceFactory implements PropertySourceFactory
    {
        @Override
        public org.springframework.core.env.PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException
        {
            Map<String, Object> readValue = new ObjectMapper().readValue(resource.getInputStream(), new TypeReference<>() {});

            return new MapPropertySource("json-property", readValue);
        }
    }
}
