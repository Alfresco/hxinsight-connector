/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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
package org.alfresco.hxi_connector.live_ingester.subsystem;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.hxi_connector.live_ingester.subsystem.Exceptions.IamSyncException;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IdentitySyncSubsystemTest
{
    private static final String USER_ID = "alice";
    private static final String EMAIL = "alice@example.com";
    private static final String GROUP_ID = "group-1";

    @Mock
    private UserManager userManager;
    @Mock
    private GroupManager groupManager;

    private IdentitySyncSubsystem subsystem;

    @BeforeEach
    void setUp()
    {
        subsystem = new IdentitySyncSubsystem(userManager, groupManager);
    }

    @Test
    void shouldSilentlySkipNullEvent()
    {
        subsystem.handleIAMEvents(null);

        verifyNoInteractions(userManager, groupManager);
    }

    @Test
    void shouldSilentlySkipEventWithNullData()
    {
        @SuppressWarnings("unchecked")
        RepoEvent<DataAttributes<NodeResource>> event = mock(RepoEvent.class);

        subsystem.handleIAMEvents(event);

        verifyNoInteractions(userManager, groupManager);
    }

    @Test
    void shouldDispatchUserCreatedEventToCreateOrMapUser()
    {
        RepoEvent<DataAttributes<NodeResource>> event = userEvent(
                AuthorizationConstants.EVENT_CREATED_TYPE,
                AuthorizationConstants.PERSON_TYPE,
                EMAIL);

        subsystem.handleIAMEvents(event);

        then(userManager).should().createOrMapUser(EMAIL, USER_ID);
        then(groupManager).shouldHaveNoInteractions();
    }

    @Test
    void shouldDispatchUserDeletedEventToHandleUpdateOrDelete()
    {
        RepoEvent<DataAttributes<NodeResource>> event = userEvent(
                AuthorizationConstants.EVENT_DELETE_TYPE,
                AuthorizationConstants.PERSON_TYPE,
                null);

        subsystem.handleIAMEvents(event);

        then(userManager).should().handleUpdateOrDelete(event);
    }

    @Test
    void shouldDispatchPersonUpdateEventToHandleUpdateOrDelete()
    {
        RepoEvent<DataAttributes<NodeResource>> event = userEvent(
                AuthorizationConstants.EVENT_UPDATED_TYPE,
                AuthorizationConstants.PERSON_TYPE,
                null);

        subsystem.handleIAMEvents(event);

        then(userManager).should().handleUpdateOrDelete(event);
    }

    @Test
    void shouldIgnoreUserUpdateForNonPersonNodeType()
    {
        RepoEvent<DataAttributes<NodeResource>> event = userEvent(
                AuthorizationConstants.EVENT_UPDATED_TYPE,
                AuthorizationConstants.USER_TYPE,
                null);

        subsystem.handleIAMEvents(event);

        then(userManager).should(never()).handleUpdateOrDelete(any());
        then(userManager).should(never()).createOrMapUser(anyString(), anyString());
    }

    @Test
    void shouldDispatchGroupCreatedEventToCreateGroup()
    {
        RepoEvent<DataAttributes<NodeResource>> event = groupEvent(AuthorizationConstants.EVENT_CREATED_TYPE);

        subsystem.handleIAMEvents(event);

        then(groupManager).should().createGroup(any(NodeResource.class));
    }

    @Test
    void shouldDispatchGroupUpdatedEventToHandleDeletionOrUpdation()
    {
        RepoEvent<DataAttributes<NodeResource>> event = groupEvent(AuthorizationConstants.EVENT_UPDATED_TYPE);

        subsystem.handleIAMEvents(event);

        then(groupManager).should().handleDeletionOrUpdation(event);
    }

    @Test
    void shouldDispatchGroupDeletedEventToDeleteGroup()
    {
        RepoEvent<DataAttributes<NodeResource>> event = groupEvent(AuthorizationConstants.EVENT_DELETE_TYPE);

        subsystem.handleIAMEvents(event);

        then(groupManager).should().deleteGroup(eq(GROUP_ID));
    }

    @Test
    void shouldIgnoreEventWithUnknownNodeType()
    {
        RepoEvent<DataAttributes<NodeResource>> event = build("cm:other", AuthorizationConstants.EVENT_CREATED_TYPE,
                NodeResource.builder().setId("x").setNodeType("cm:other").build(), null);

        subsystem.handleIAMEvents(event);

        verifyNoInteractions(userManager, groupManager);
    }

    @Test
    void shouldThrowWhenUserEventResourcesAreAllNull()
    {
        @SuppressWarnings("unchecked")
        RepoEvent<DataAttributes<NodeResource>> event = mock(RepoEvent.class);
        @SuppressWarnings("unchecked")
        DataAttributes<NodeResource> data = mock(DataAttributes.class);
        given(event.getData()).willReturn(data);
        // type detection needs node type → use only resourceBefore
        NodeResource before = NodeResource.builder().setNodeType(AuthorizationConstants.PERSON_TYPE).build();
        given(data.getResource()).willReturn(null);
        given(data.getResourceBefore()).willReturn(before);
        given(event.getType()).willReturn(AuthorizationConstants.EVENT_DELETE_TYPE);

        // resource fallback works → should be dispatched, no throw
        subsystem.handleIAMEvents(event);
        then(userManager).should().handleUpdateOrDelete(event);
    }

    // helpers

    @SuppressWarnings("unchecked")
    private static RepoEvent<DataAttributes<NodeResource>> userEvent(String eventType, String nodeType, String email)
    {
        Map<String, Serializable> props = new HashMap<>();
        props.put(AuthorizationConstants.USERNAME_PROPERTY_1, USER_ID);
        if (email != null)
        {
            props.put(AuthorizationConstants.EMAIL_PROPERTY, email);
        }

        NodeResource resource = NodeResource.builder()
                .setId(USER_ID)
                .setNodeType(nodeType)
                .setProperties(props)
                .build();

        return build(nodeType, eventType, resource, resource);
    }

    private static RepoEvent<DataAttributes<NodeResource>> groupEvent(String eventType)
    {
        NodeResource resource = NodeResource.builder()
                .setId(GROUP_ID)
                .setNodeType(AuthorizationConstants.GROUP_TYPE)
                .build();
        return build(AuthorizationConstants.GROUP_TYPE, eventType, resource, resource);
    }

    @SuppressWarnings("unchecked")
    private static RepoEvent<DataAttributes<NodeResource>> build(
            String unusedType, String eventType, NodeResource resource, NodeResource before)
    {
        RepoEvent<DataAttributes<NodeResource>> event = mock(RepoEvent.class);
        DataAttributes<NodeResource> data = mock(DataAttributes.class);
        given(event.getData()).willReturn(data);
        given(event.getType()).willReturn(eventType);
        given(data.getResource()).willReturn(resource);
        given(data.getResourceBefore()).willReturn(before);
        return event;
    }
}


