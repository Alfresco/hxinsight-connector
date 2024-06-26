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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class CamelEventMapper
{

    private final ObjectMapper mapper;

    /**
     * Unmarshalls Camel exchange body to RepoEvent POJO
     *
     * @param exchange
     *            Camel Exchange object
     * @return RepoEvent
     */
    public RepoEvent<DataAttributes<NodeResource>> repoEventFrom(Exchange exchange)
    {
        try
        {
            return mapper.readValue(exchange.getIn().getBody(String.class), new TypeReference<>() {});
        }
        catch (JsonProcessingException e)
        {
            throw new LiveIngesterRuntimeException("Event deserialization failed", e);
        }
    }

    /**
     * This method alters the original type of repo event.
     *
     * @param exchange
     *            Camel exchange object
     * @param newEventType
     *            event type to be altered
     * @return altered repo event
     */
    public RepoEvent<DataAttributes<NodeResource>> alterRepoEvent(Exchange exchange, String newEventType)
    {
        final RepoEvent<DataAttributes<NodeResource>> repoEventOriginal = exchange.getIn().getBody(RepoEvent.class);
        log.atDebug().log("Altering repo event type from {} to {}. Repo Event id: {}", repoEventOriginal.getType(), newEventType, repoEventOriginal.getId());
        return RepoEvent.<DataAttributes<NodeResource>> builder()
                .setData(repoEventOriginal.getData())
                .setDatacontenttype(repoEventOriginal.getDatacontenttype())
                .setDataschema(repoEventOriginal.getDataschema())
                .setExtensionAttributes(repoEventOriginal.getExtensionAttributes())
                .setId(repoEventOriginal.getId())
                .setSource(repoEventOriginal.getSource())
                .setTime(repoEventOriginal.getTime())
                .setType(newEventType)
                .build();
    }
}
