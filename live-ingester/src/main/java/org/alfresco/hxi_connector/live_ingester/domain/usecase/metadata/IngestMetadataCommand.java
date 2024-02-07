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

package org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata;

import static org.alfresco.hxi_connector.live_ingester.domain.utils.EnsureUtils.ensureNonNull;
import static org.alfresco.hxi_connector.live_ingester.domain.utils.EnsureUtils.ensureNotBlank;

import java.util.Set;

import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;

public record IngestMetadataCommand(
        String nodeId,
        boolean isUpdate,
        PropertyDelta<String> nodeType,
        PropertyDelta<String> createdByUserWithId,
        PropertyDelta<String> modifiedByUserWithId,
        PropertyDelta<Set<String>> aspectNames,
        PropertyDelta<Long> createdAt,
        Set<CustomPropertyDelta<?>> properties)
{
    public IngestMetadataCommand
    {
        ensureNotBlank(nodeId, "Node id cannot be blank");
        ensureNonNull(nodeType, "Node %s type delta cannot be null", nodeId);
        ensureNonNull(createdByUserWithId, "Node %s created by user with nodeId delta cannot be null", nodeId);
        ensureNonNull(modifiedByUserWithId, "Node %s modified by user with nodeId delta cannot be null", nodeId);
        ensureNonNull(aspectNames, "Node %s aspect names delta cannot be null", nodeId);
        ensureNonNull(createdAt, "Node %s created at property delta cannot be null", nodeId);
        ensureNonNull(properties, "Node %s custom properties delta cannot be null", nodeId);
    }
}
