package org.alfresco.hxi_connector.live_ingester.messaging.in.utils;

import static org.junit.jupiter.api.Assertions.*;
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
        assertTrue(EventUtils.isEventTypeUpdated(event));
    }
}
