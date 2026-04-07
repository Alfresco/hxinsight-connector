/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper;

import java.util.Map;
import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class MimeTypeMapper
{

    public static final String EMPTY_MIME_TYPE = "";
    static final Map<String, String> DEFAULT_MIME_TYPES = Map.of("*", "*");
    private final IntegrationProperties integrationProperties;

    @PostConstruct
    void validateMappings()
    {
        Map<String, String> mappings = integrationProperties.alfresco().transform().mimeType().mapping();
        if (mappings == null)
        {
            log.atDebug().log("No custom MIME type mappings configured, using default: {}", DEFAULT_MIME_TYPES);
            return;
        }
        log.atInfo().log("Active MIME type mappings: {}", mappings);
        mappings.forEach((source, target) -> {
            if (!source.equals("*") && !source.contains("/"))
            {
                log.atWarn().log("MIME type mapping key '{}' does not look like a valid MIME type pattern - "
                        + "keys containing '/' must be wrapped in square brackets in YAML config, "
                        + "e.g. \"[text/csv]\": application/pdf", source);
            }
            if (target.contains("*") && !target.equals(source))
            {
                throw new IllegalArgumentException(
                        "Invalid MIME type mapping: wildcard target '%s' is only supported as a passthrough when it matches the source pattern '%s'"
                                .formatted(target, source));
            }
        });
    }

    public String mapMimeType(String inputType)
    {
        final Map<String, String> mappings = ObjectUtils.defaultIfNull(integrationProperties.alfresco().transform().mimeType().mapping(), DEFAULT_MIME_TYPES);
        return mappings.getOrDefault(inputType, determineWildcardMapping(inputType, mappings));
    }

    private String determineWildcardMapping(String inputType, Map<String, String> mappings)
    {
        for (Map.Entry<String, String> mapping : mappings.entrySet())
        {
            if (mapping.getKey().endsWith("/*") && getType(inputType).equals(getType(mapping.getKey())))
            {
                if (mapping.getKey().equals(mapping.getValue()))
                {
                    return inputType;
                }
                return StringUtils.defaultIfBlank(mapping.getValue(), EMPTY_MIME_TYPE);
            }
        }
        return mappings.entrySet().stream()
                .filter(mapping -> mapping.getKey().equals("*"))
                .findFirst()
                .map(entry -> entry.getKey().equals(entry.getValue()) ? inputType : entry.getValue())
                .orElse(EMPTY_MIME_TYPE);
    }

    static String getType(String inputType)
    {
        return inputType.split("/")[0];
    }

}
