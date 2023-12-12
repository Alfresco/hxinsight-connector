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

package org.alfresco.hxi_connector.live_ingester.domain.event;

import static org.alfresco.hxi_connector.live_ingester.domain.model.out.PredefinedNodeProperty.ASPECTS_NAMES;
import static org.alfresco.hxi_connector.live_ingester.domain.model.out.PredefinedNodeProperty.CREATED_AT;
import static org.alfresco.hxi_connector.live_ingester.domain.model.out.PredefinedNodeProperty.CREATED_BY_USER_WITH_ID;
import static org.alfresco.hxi_connector.live_ingester.domain.model.out.PredefinedNodeProperty.IS_FILE;
import static org.alfresco.hxi_connector.live_ingester.domain.model.out.PredefinedNodeProperty.IS_FOLDER;
import static org.alfresco.hxi_connector.live_ingester.domain.model.out.PredefinedNodeProperty.MODIFIED_BY_USER_WITH_ID;
import static org.alfresco.hxi_connector.live_ingester.domain.model.out.PredefinedNodeProperty.NAME;
import static org.alfresco.hxi_connector.live_ingester.domain.model.out.PredefinedNodeProperty.PRIMARY_ASSOC_Q_NAME;
import static org.alfresco.hxi_connector.live_ingester.domain.model.out.PredefinedNodeProperty.TYPE;

import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.model.in.IngestNewNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.model.in.Node;
import org.alfresco.hxi_connector.live_ingester.domain.model.out.event.UpdateNodeMetadataEvent;

@Component
public class UpdateNodeEventMapper
{
    public UpdateNodeMetadataEvent map(IngestNewNodeEvent event)
    {
        Node node = event.node();
        UpdateNodeMetadataEvent updateMetadataEvent = UpdateNodeMetadataEvent.create()
                .set(NAME.withValue(node.name()))
                .set(PRIMARY_ASSOC_Q_NAME.withValue(node.primaryAssocQName()))
                .set(TYPE.withValue(node.nodeType()))
                .set(CREATED_BY_USER_WITH_ID.withValue(node.createdByUserWithId()))
                .set(MODIFIED_BY_USER_WITH_ID.withValue(node.modifiedByUserWithId()))
                .set(ASPECTS_NAMES.withValue(node.aspectNames()))
                .set(IS_FILE.withValue(node.isFile()))
                .set(IS_FOLDER.withValue(node.isFolder()))
                .set(CREATED_AT.withValue(node.createdAt()));
        node.properties().forEach(updateMetadataEvent::set);
        return updateMetadataEvent;
    }
}
