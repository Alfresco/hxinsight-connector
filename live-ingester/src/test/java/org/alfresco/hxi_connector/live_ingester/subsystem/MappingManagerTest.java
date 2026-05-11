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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.hxi_connector.nucleus_client.client.ClientException;
import org.alfresco.hxi_connector.nucleus_client.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_client.dto.NucleusGroupMemberAssignmentInput;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MappingManagerTest
{
    private static final String USER_ID = "alice";

    @Mock
    private NucleusClient nucleusClient;

    private MappingManager mappingManager;

    @BeforeEach
    void setUp()
    {
        mappingManager = new MappingManager(nucleusClient);
    }

    @Test
    void shouldAddAndRemoveGroupsBasedOnDiff()
    {
        RepoEvent<DataAttributes<NodeResource>> event = newEvent(
                List.of("g1", "g2"), List.of("g2", "g3"));

        boolean changed = mappingManager.updateGroupMapping(event);

        assertThat(changed).isTrue();

        ArgumentCaptor<List<NucleusGroupMemberAssignmentInput>> addCaptor = ArgumentCaptor.forClass(List.class);
        then(nucleusClient).should().assignGroupMembers(addCaptor.capture());
        assertThat(addCaptor.getValue())
                .extracting(NucleusGroupMemberAssignmentInput::externalGroupId)
                .containsExactly("g1");

        then(nucleusClient).should().removeGroupMembers(eq("g3"), eq(List.of(USER_ID)));
    }

    @Test
    void shouldReturnFalseWhenNoMembershipChange()
    {
        RepoEvent<DataAttributes<NodeResource>> event = newEvent(
                List.of("g1", "g2"), List.of("g1", "g2"));

        boolean changed = mappingManager.updateGroupMapping(event);

        assertThat(changed).isFalse();
        then(nucleusClient).should(never()).assignGroupMembers(anyList());
        then(nucleusClient).should(never()).removeGroupMembers(any(), anyList());
    }

    @Test
    void shouldHandleNullSecondaryParentsAsEmpty()
    {
        RepoEvent<DataAttributes<NodeResource>> event = newEvent(List.of("g1"), null);

        boolean changed = mappingManager.updateGroupMapping(event);

        assertThat(changed).isTrue();
        then(nucleusClient).should().assignGroupMembers(anyList());
        then(nucleusClient).should(never()).removeGroupMembers(any(), anyList());
    }

    @Test
    void shouldOnlyRemoveWhenNoNewGroups()
    {
        RepoEvent<DataAttributes<NodeResource>> event = newEvent(List.of(), List.of("g1", "g2"));

        boolean changed = mappingManager.updateGroupMapping(event);

        assertThat(changed).isTrue();
        // Optimisation: must NOT call assignGroupMembers when nothing to add.
        then(nucleusClient).should(never()).assignGroupMembers(anyList());
        then(nucleusClient).should(times(2)).removeGroupMembers(any(), anyList());
    }

    @Test
    void shouldContinueRemovingWhenOneRemovalFails()
    {
        RepoEvent<DataAttributes<NodeResource>> event = newEvent(List.of(), List.of("g1", "g2"));

        doThrow(new ClientException("boom")).when(nucleusClient).removeGroupMembers(eq("g1"), anyList());

        boolean changed = mappingManager.updateGroupMapping(event);

        assertThat(changed).isTrue();
        then(nucleusClient).should().removeGroupMembers(eq("g1"), anyList());
        then(nucleusClient).should().removeGroupMembers(eq("g2"), anyList());
    }

    @Test
    void shouldSwallowAssignFailureAndStillRemove()
    {
        RepoEvent<DataAttributes<NodeResource>> event = newEvent(List.of("gNew"), List.of("gOld"));

        doThrow(new ClientException("boom")).when(nucleusClient).assignGroupMembers(anyList());

        boolean changed = mappingManager.updateGroupMapping(event);

        assertThat(changed).isTrue();
        then(nucleusClient).should().removeGroupMembers(eq("gOld"), eq(List.of(USER_ID)));
    }

    @Test
    void isGroupMembershipUpdatedShouldDetectChanges()
    {
        assertThat(mappingManager.isGroupMembershipUpdated(newEvent(List.of("g1"), List.of("g2")))).isTrue();
        assertThat(mappingManager.isGroupMembershipUpdated(newEvent(List.of("g1"), List.of("g1")))).isFalse();
    }

    @SuppressWarnings("unchecked")
    private static RepoEvent<DataAttributes<NodeResource>> newEvent(List<String> current, List<String> previous)
    {
        RepoEvent<DataAttributes<NodeResource>> event = mock(RepoEvent.class);
        DataAttributes<NodeResource> data = mock(DataAttributes.class);
        given(event.getData()).willReturn(data);

        Map<String, Serializable> props = new HashMap<>();
        props.put(AuthorizationConstants.USERNAME_PROPERTY_1, USER_ID);

        NodeResource.Builder current_b = NodeResource.builder().setProperties(props);
        if (current != null)
        {
            current_b.setSecondaryParents(current);
        }
        given(data.getResource()).willReturn(current_b.build());

        NodeResource.Builder previous_b = NodeResource.builder().setProperties(props);
        if (previous != null)
        {
            previous_b.setSecondaryParents(previous);
        }
        given(data.getResourceBefore()).willReturn(previous_b.build());

        return event;
    }
}

