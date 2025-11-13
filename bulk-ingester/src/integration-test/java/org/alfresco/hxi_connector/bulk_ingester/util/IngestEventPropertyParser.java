/*-
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
package org.alfresco.hxi_connector.bulk_ingester.util;

import static java.util.stream.Collectors.toSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IngestEventPropertyParser
{

    public static Map<String, Serializable> parseProperties(String... properties)
    {
        return Arrays.stream(properties)
                .map(IngestEventPropertyParser::parseProperty)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, Serializable> parseProperties(
            String primaryParentId,
            List<String> primaryAncestorIds,
            String... properties)
    {
        Map<String, Serializable> props = parseProperties(properties);

        Map<String, Serializable> ancestorsMap = new HashMap<>();
        ancestorsMap.put("primaryParentId", primaryParentId != null ? primaryParentId : "");
        ancestorsMap.put("primaryAncestorIds", (Serializable) new ArrayList<>(primaryAncestorIds));
        props.put("ancestors", (Serializable) ancestorsMap);

        return props;
    }

    private static Map.Entry<String, Serializable> parseProperty(String property)
    {
        String[] propertySplit = property.split("=");

        String key = propertySplit[0];

        if (propertySplit.length == 1)
        {
            return Map.entry(key, "");
        }

        String value = propertySplit[1];

        return Map.entry(key, parsePropertyValue(value));
    }

    private static Serializable parsePropertyValue(String value)
    {
        if (value.startsWith("["))
        {
            value = value.replace("[", "").replace("]", "");

            if (value.isBlank())
            {
                return (Serializable) Set.of();
            }

            return (Serializable) Arrays.stream(value.split(","))
                    .map(String::stripLeading)
                    .collect(toSet());
        }

        if (StringUtils.isNumeric(value))
        {
            return Long.parseLong(value);
        }

        if (value.equals("true") || value.equals("false"))
        {
            return Boolean.parseBoolean(value);
        }

        return value;
    }
}