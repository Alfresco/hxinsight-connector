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
package org.alfresco.hxi_connector.live_ingester.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class MaskingPatternLayout extends PatternLayout
{
    private static final String MASK = "*****";
    private static final String MASK_FIELD_REGEX_DEFAULT = "\\\"?%s\\\"?\\s*[:=]\\s*[^:=]\\\"?(?!\"\\*\\*\\*\\*\\*\"|[*])[^,\\r\\n]*\\\"?[^,}\\]\\s]";

    private final List<String> sensitiveFields = new ArrayList<>();
    private Pattern multilinePattern;

    public void addMaskField(String sensitiveFieldName)
    {
        sensitiveFields.add(sensitiveFieldName);
        multilinePattern = Pattern.compile(
                // build pattern using logical OR
                String.join("|", sensitiveFields.stream().map(MASK_FIELD_REGEX_DEFAULT::formatted).toArray(String[]::new)),
                Pattern.MULTILINE);
    }

    @Override
    public String doLayout(ILoggingEvent event)
    {
        String message = super.doLayout(event);
        if (multilinePattern == null)
        {
            return message;
        }

        return maskFields(message);
    }

    private String maskFields(String message)
    {
        Matcher matcher = multilinePattern.matcher(message);
        String fixedMessage = message;
        while (matcher.find())
        {
            String snippet = fixedMessage.substring(matcher.start(), matcher.end());
            String toMask = null;
            List<String> snippetParts = Arrays.stream(snippet.split("=")).toList();
            if (snippetParts.size() >= 2)
            {
                toMask = snippetParts.stream()
                        .skip(1)
                        .map(String::strip)
                        .collect(Collectors.joining("="))
                        .replaceAll("\"(.+)\"", "$1");
            }
            else
            {
                snippetParts = Arrays.stream(snippet.split(":")).toList();
                if (snippetParts.size() >= 2)
                {
                    toMask = snippetParts.stream()
                            .skip(1)
                            .map(String::strip)
                            .collect(Collectors.joining(":"))
                            .replaceAll("\"(.+)\"", "$1");
                }
            }

            if (toMask != null)
            {
                String masked = snippet.replace(toMask, MASK);
                fixedMessage = matcher.replaceFirst(masked);
                matcher = multilinePattern.matcher(fixedMessage);
            }
        }
        return fixedMessage;
    }
}
