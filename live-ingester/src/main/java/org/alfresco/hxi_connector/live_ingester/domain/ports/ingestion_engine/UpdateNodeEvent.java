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

package org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType;

@Getter
@ToString
@EqualsAndHashCode
public class UpdateNodeEvent implements NodeEvent
{
    private final String objectId;
    private final EventType eventType;
    private final Map<String, NodeProperty<?>> metadataPropertiesToSet = new HashMap<>();
    private final Map<String, ContentProperty> contentPropertiesToSet = new HashMap<>();
    private final Set<String> propertiesToUnset = new HashSet<>();
    private final String sourceId;

    public UpdateNodeEvent(String objectId, EventType eventType, String sourceId)
    {
        this.objectId = objectId;
        this.eventType = eventType;
        this.sourceId = sourceId;
    }

    public UpdateNodeEvent addContentInstruction(ContentProperty contentProperty)
    {
        contentPropertiesToSet.put(contentProperty.propertyName(), contentProperty);
        return this;
    }

    public UpdateNodeEvent addMetadataInstruction(NodeProperty<?> metadataProperty)
    {
        metadataPropertiesToSet.put(metadataProperty.name(), metadataProperty);

        return this;
    }

    public UpdateNodeEvent addUnsetInstruction(String metadataPropertyName)
    {
        propertiesToUnset.add(metadataPropertyName);

        return this;
    }
}
