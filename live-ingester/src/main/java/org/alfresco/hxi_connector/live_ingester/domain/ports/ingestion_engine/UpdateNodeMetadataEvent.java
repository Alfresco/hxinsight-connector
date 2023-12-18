/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 Alfresco Software Limited
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

import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyState.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(staticName = "create")
public class UpdateNodeMetadataEvent
{
    private final Map<String, NodeProperty<?>> metadataPropertiesToSet = new HashMap<>();

    private final Set<String> metadataPropertiesToUnset = new HashSet<>();

    public UpdateNodeMetadataEvent set(NodeProperty<?> metadataProperty)
    {
        metadataPropertiesToSet.put(metadataProperty.name(), metadataProperty);

        return this;
    }

    public UpdateNodeMetadataEvent unset(String metadataPropertyName)
    {
        metadataPropertiesToUnset.add(metadataPropertyName);

        return this;
    }
}
