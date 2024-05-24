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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.EventUtils.PREDICTION_REVIEW_STATUS_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.DELETE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.UPDATE;
import static org.alfresco.repo.event.v1.model.EventType.CHILD_ASSOC_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_UPDATED;

import java.util.Map;

import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType;
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

    @Test
    void shouldConvertCreateEventType()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        given(event.getType()).willReturn(NODE_CREATED.getType());

        // when
        EventType eventType = EventUtils.getEventType(event);

        // then
        assertEquals(CREATE, eventType);
    }

    @Test
    void shouldConvertUpdateEventType()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        given(event.getType()).willReturn(NODE_UPDATED.getType());

        // when
        EventType eventType = EventUtils.getEventType(event);

        // then
        assertEquals(UPDATE, eventType);
    }

    @Test
    void shouldConvertDeleteEventType()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        given(event.getType()).willReturn(NODE_DELETED.getType());

        // when
        EventType eventType = EventUtils.getEventType(event);

        // then
        assertEquals(DELETE, eventType);
    }

    @Test
    void shouldRaiseExceptionForOtherEventType()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        given(event.getType()).willReturn(CHILD_ASSOC_CREATED.getType());

        // then
        assertThrows(LiveIngesterRuntimeException.class, () -> EventUtils.getEventType(event));
    }

    @Test
    void shouldCheckIfPredictionWasConfirmed()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        given(event.getData()).willReturn(mock());
        given(event.getData().getResourceBefore()).willReturn(mock());
        given(event.getData().getResourceBefore().getProperties()).willReturn(Map.of(
                PREDICTION_REVIEW_STATUS_PROPERTY, "UNREVIEWED"));
        given(event.getData().getResource()).willReturn(mock());
        given(event.getData().getResource().getProperties()).willReturn(Map.of(
                PREDICTION_REVIEW_STATUS_PROPERTY, "CONFIRMED"));

        // then
        assertTrue(EventUtils.wasPredictionConfirmed(event));
    }

    @Test
    void ifReviewStatusIsUnchanged_shouldReturnFalse()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        given(event.getData()).willReturn(mock());
        given(event.getData().getResourceBefore()).willReturn(mock());
        given(event.getData().getResourceBefore().getProperties()).willReturn(Map.of(
                "some_other_prop", "some value"));
        given(event.getData().getResource()).willReturn(mock());
        given(event.getData().getResource().getProperties()).willReturn(Map.of(
                PREDICTION_REVIEW_STATUS_PROPERTY, "CONFIRMED"));

        // then
        assertFalse(EventUtils.wasPredictionConfirmed(event));
    }

    @Test
    void ifPropertiesAreNotChanged_shouldReturnFalse()
    {
        // given
        RepoEvent<DataAttributes<NodeResource>> event = mock();
        given(event.getData()).willReturn(mock());
        given(event.getData().getResourceBefore()).willReturn(mock());
        given(event.getData().getResource()).willReturn(mock());
        given(event.getData().getResource().getProperties()).willReturn(Map.of(
                PREDICTION_REVIEW_STATUS_PROPERTY, "CONFIRMED"));

        // then
        assertFalse(EventUtils.wasPredictionConfirmed(event));
    }

}
