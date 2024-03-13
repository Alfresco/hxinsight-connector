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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_BY_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.UPDATE;

import org.junit.jupiter.api.Test;

class UpdateNodeMetadataEventTest
{
    private static final String NODE_ID = "node-id";

    @Test
    void shouldOverwriteAlreadySetProperty()
    {
        // given
        UpdateNodeMetadataEvent updateNodeMetadataEvent = new UpdateNodeMetadataEvent(NODE_ID, UPDATE);

        NodeProperty<String> name1 = new NodeProperty<>(CREATED_BY_PROPERTY, "admin");
        NodeProperty<String> name2 = new NodeProperty<>(CREATED_BY_PROPERTY, "hruser");

        // when
        updateNodeMetadataEvent.set(name1);
        updateNodeMetadataEvent.set(name2);

        // then
        assertFalse(updateNodeMetadataEvent.getMetadataPropertiesToSet().containsValue(name1));
        assertTrue(updateNodeMetadataEvent.getMetadataPropertiesToSet().containsValue(name2));
    }

    @Test
    void shouldNotDuplicatePropertiesToUnset()
    {
        // given
        UpdateNodeMetadataEvent updateNodeMetadataEvent = new UpdateNodeMetadataEvent(NODE_ID, UPDATE);

        // when
        updateNodeMetadataEvent.unset(CREATED_BY_PROPERTY);
        updateNodeMetadataEvent.unset(CREATED_BY_PROPERTY);

        // then
        assertEquals(1, updateNodeMetadataEvent.getMetadataPropertiesToUnset().size());
        assertTrue(updateNodeMetadataEvent.getMetadataPropertiesToUnset().contains(CREATED_BY_PROPERTY));
    }
}
