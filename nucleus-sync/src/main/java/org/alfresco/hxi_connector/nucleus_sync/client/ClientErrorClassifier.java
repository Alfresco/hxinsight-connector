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
package org.alfresco.hxi_connector.nucleus_sync.client;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.nucleus_sync.client.NucleusSyncMetrices.Tags;

/**
 * Maps a throwable to a coarse-grained {@code error.type} tag value used by the
 * client failure counters. Walks the cause chain so wrapped exceptions are still
 * classified meaningfully.
 */
final class ClientErrorClassifier
{
    private ClientErrorClassifier() {}

    static String classify(Throwable t)
    {
        Throwable cur = t;
        while (cur != null)
        {
            if (cur instanceof WebClientResponseException wcre)
            {
                int status = wcre.getStatusCode().value();
                if (status == 401 || status == 403)
                {
                    return Tags.ERR_AUTH;
                }
                if (status >= 500)
                {
                    return Tags.ERR_SERVER;
                }
                if (status >= 400)
                {
                    return Tags.ERR_CLIENT;
                }
            }
            if (cur instanceof EndpointServerErrorException)
            {
                return Tags.ERR_SERVER;
            }
            if (cur instanceof TimeoutException
                    || cur.getClass().getSimpleName().contains("Timeout"))
            {
                return Tags.ERR_TIMEOUT;
            }
            if (cur instanceof WebClientRequestException)
            {
                return Tags.ERR_NETWORK;
            }
            // Jackson serialization (writing) and deserialization (reading) both surface as
            // JsonProcessingException; keep classification coarse-grained as "parsing".
            if (cur instanceof JsonProcessingException)
            {
                return Tags.ERR_PARSING;
            }
            if (cur instanceof IOException)
            {
                return Tags.ERR_NETWORK;
            }
            cur = cur.getCause();
            if (cur == t)
            {
                break;
            }
        }
        return Tags.ERR_UNKNOWN;
    }
}