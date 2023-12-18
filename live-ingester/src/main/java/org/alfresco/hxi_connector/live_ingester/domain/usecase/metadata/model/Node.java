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

package org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model;

import static org.alfresco.hxi_connector.live_ingester.domain.utils.EnsureUtils.ensureNonNull;
import static org.alfresco.hxi_connector.live_ingester.domain.utils.EnsureUtils.ensureNotBlank;

import java.util.Optional;
import java.util.Set;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeProperty;

public record Node(
        String id,
        String name,
        String primaryAssocQName,
        String nodeType,
        String createdByUserWithId,
        String modifiedByUserWithId,
        Optional<String> contentMimeType,
        Set<String> aspectNames,
        boolean isFile,
        boolean isFolder,
        long createdAt,
        Set<NodeProperty<?>> properties)
{
    public Node
    {
        ensureNotBlank(id, "Node id cannot be blank");
        ensureNotBlank(name, "Node %s name cannot be blank", id);
        ensureNotBlank(primaryAssocQName, "Node %s qualified name cannot be blank", id);
        ensureNotBlank(nodeType, "Node %s type cannot be blank", id);
        ensureNotBlank(createdByUserWithId, "Node %s created by user with id cannot be blank", id);
        ensureNotBlank(modifiedByUserWithId, "Node %s modified by user with id cannot be blank", id);
        ensureNonNull(aspectNames, "Node %s aspect names cannot be null", id);
        ensureNonNull(properties, "Node %s properties cannot be null", id);
    }
}
