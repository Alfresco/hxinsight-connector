/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_BY_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE_OR_UPDATE;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

class UpdateNodeEventTest
{
    private static final String NODE_ID = "node-id";
    private static final String SOURCE_ID = "dummy-source-id";
    private static final long TIMESTAMP = Instant.now().toEpochMilli();

    @Test
    void shouldOverwriteAlreadySetProperty()
    {
        // given
        UpdateNodeEvent updateNodeEvent = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP);

        NodeProperty<String> name1 = new NodeProperty<>(CREATED_BY_PROPERTY, "admin");
        NodeProperty<String> name2 = new NodeProperty<>(CREATED_BY_PROPERTY, "hruser");

        // when
        updateNodeEvent.addMetadataInstruction(name1);
        updateNodeEvent.addMetadataInstruction(name2);

        // then
        assertFalse(updateNodeEvent.getMetadataPropertiesToSet().containsValue(name1));
        assertTrue(updateNodeEvent.getMetadataPropertiesToSet().containsValue(name2));
    }

    @Test
    void shouldAddAncestorInstruction()
    {
        UpdateNodeEvent updateNodeEvent = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP);
        AncestorsProperty ancestorsProperty = new AncestorsProperty("ancestors", "parent-id", List.of("grandparent-id"));

        updateNodeEvent.addAncestorInstruction(ancestorsProperty);

        assertTrue(updateNodeEvent.getAncestorsPropertiesToSet().containsValue(ancestorsProperty));
    }

    @Test
    void shouldOverwriteAlreadySetAncestorProperty()
    {
        UpdateNodeEvent updateNodeEvent = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP);
        AncestorsProperty ancestors1 = new AncestorsProperty("ancestors", "parent-id-1", List.of("grandparent-id-1"));
        AncestorsProperty ancestors2 = new AncestorsProperty("ancestors", "parent-id-2", List.of("grandparent-id-2"));

        updateNodeEvent.addAncestorInstruction(ancestors1);
        updateNodeEvent.addAncestorInstruction(ancestors2);

        assertFalse(updateNodeEvent.getAncestorsPropertiesToSet().containsValue(ancestors1));
        assertTrue(updateNodeEvent.getAncestorsPropertiesToSet().containsValue(ancestors2));
    }

    @Test
    void shouldReturnSameInstanceAfterAddingAncestorInstruction()
    {
        UpdateNodeEvent updateNodeEvent = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP);
        AncestorsProperty ancestorsProperty = new AncestorsProperty("ancestors", "parent-id", List.of());

        UpdateNodeEvent result = updateNodeEvent.addAncestorInstruction(ancestorsProperty);

        assertSame(updateNodeEvent, result);
    }

    @Test
    void shouldAddAncestorInstructionWithEmptyAncestorsList()
    {
        UpdateNodeEvent updateNodeEvent = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP);
        AncestorsProperty ancestorsProperty = new AncestorsProperty("ancestors", "parent-id", List.of());

        updateNodeEvent.addAncestorInstruction(ancestorsProperty);

        assertTrue(updateNodeEvent.getAncestorsPropertiesToSet().containsValue(ancestorsProperty));
    }
}
