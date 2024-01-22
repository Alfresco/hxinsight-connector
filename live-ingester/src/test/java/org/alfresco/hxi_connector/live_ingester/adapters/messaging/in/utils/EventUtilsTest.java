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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.in.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import org.junit.jupiter.api.Test;

import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

class EventUtilsTest
{

    @Test
    void shouldDetectNodeCreatedEvent()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        given(event.getType()).willReturn(NODE_CREATED.getType());

        // then
        assertTrue(EventUtils.isEventTypeCreated(event));
    }

    @Test
    void shouldDetectEventTypeIsNotCreated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        given(event.getType()).willReturn(NODE_DELETED.getType());

        // then
        assertFalse(EventUtils.isEventTypeCreated(event));
    }

    @Test
    void shouldDetectNodeUpdatedEvent()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        given(event.getType()).willReturn(NODE_UPDATED.getType());

        // then
        assertTrue(EventUtils.isEventTypeUpdated(event));
    }

    @Test
    void shouldDetectEventTypeIsNotUpdated()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        given(event.getType()).willReturn(NODE_CREATED.getType());

        // then
        assertFalse(EventUtils.isEventTypeUpdated(event));
    }

    @Test
    void shouldDetectNodeDeletedEvent()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        given(event.getType()).willReturn(NODE_DELETED.getType());

        // then
        assertTrue(EventUtils.isEventTypeDeleted(event));
    }

    @Test
    void shouldDetectEventTypeIsNotDeleted()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();

        given(event.getType()).willReturn(NODE_UPDATED.getType());

        // then
        assertFalse(EventUtils.isEventTypeDeleted(event));
    }
}
