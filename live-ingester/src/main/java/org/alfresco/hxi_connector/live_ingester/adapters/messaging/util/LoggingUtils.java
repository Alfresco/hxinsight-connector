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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.util;

import static org.alfresco.hxi_connector.common.constant.HttpHeaders.AUTHORIZATION;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.NoArgsConstructor;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.event.Level;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class LoggingUtils
{
    private static final String MASK = "***";
    private static final Set<String> HEADERS_TO_MASK = Set.of(AUTHORIZATION);

    public static void logMaskedExchangeState(Exchange exchange, Logger log, Level level)
    {
        Map<String, Object> properties = exchange.getProperties();
        Map<String, Object> headers = new HashMap<>(exchange.getMessage().getHeaders()).entrySet().stream()
                .peek(e -> {
                    if (HEADERS_TO_MASK.contains(e.getKey()))
                    {
                        e.setValue(MASK);
                    }
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        String bodyType = exchange.getMessage().getBody() != null ? exchange.getMessage().getBody().getClass().getName() : "";
        String body = exchange.getMessage().getBody(String.class);

        log.atLevel(level).log("Exchange: [\n\tProperties: {}\n\tHeaders: {}\n\tBodyType: {}\n\tBody: {}\n]",
                properties, headers, bodyType, body);
    }
}
