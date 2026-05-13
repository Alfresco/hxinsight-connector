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

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.alfresco.hxi_connector.nucleus_client.client.AlfrescoClient;
import org.alfresco.hxi_connector.nucleus_client.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_client.dto.NucleusGroupInput;
import org.alfresco.hxi_connector.nucleus_client.dto.NucleusGroupOutput;
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
class GroupManagerTest
{
    private static final String GROUP_ID = "group-1";
    private static final String GROUP_NODE_ID = "node-uuid-1";

    @Mock
    private NucleusClient nucleusClient;
    @Mock
    private MappingManager mappingManager;
    @Mock
    private AlfrescoClient alfrescoClient;

    private GroupManager groupManager;

    @BeforeEach
    void setUp()
    {
        groupManager = new GroupManager(nucleusClient, mappingManager, alfrescoClient);
    }

    @Test
    void shouldCreateGroupWhenNotExisting()
    {
        NodeResource resource = groupResource();
        given(nucleusClient.getGroupByExternalId(GROUP_ID)).willReturn(Optional.empty());

        groupManager.createGroup(resource);

        then(nucleusClient).should().createGroups(List.of(new NucleusGroupInput(GROUP_ID)));
    }

    @Test
    void shouldSkipCreateWhenGroupAlreadyExists()
    {
        NodeResource resource = groupResource();
        given(nucleusClient.getGroupByExternalId(GROUP_ID))
                .willReturn(Optional.of(new NucleusGroupOutput(GROUP_ID)));

        groupManager.createGroup(resource);

        then(nucleusClient).should(never()).createGroups(anyList());
    }

    @Test
    void shouldDeleteGroupViaClient()
    {
        groupManager.deleteGroup(GROUP_ID);

        then(nucleusClient).should().deleteGroup(GROUP_ID);
    }

    @Test
    void handleDeletionOrUpdationShouldDeleteWhenMembershipUnchangedAndNodeMissingInRepo()
    {
        RepoEvent<DataAttributes<NodeResource>> event = groupEvent();
        given(mappingManager.isGroupMembershipUpdated(event)).willReturn(false);
        given(alfrescoClient.isNodeExist(GROUP_NODE_ID)).willReturn(false);

        groupManager.handleDeletionOrUpdation(event);

        then(nucleusClient).should().deleteGroup(GROUP_ID);
    }

    @Test
    void handleDeletionOrUpdationShouldDoNothingWhenMembershipChanged()
    {
        RepoEvent<DataAttributes<NodeResource>> event = groupEvent();
        given(mappingManager.isGroupMembershipUpdated(event)).willReturn(true);

        groupManager.handleDeletionOrUpdation(event);

        then(nucleusClient).should(never()).deleteGroup(GROUP_ID);
    }

    @Test
    void handleDeletionOrUpdationShouldDoNothingWhenGroupStillExistsInRepo()
    {
        RepoEvent<DataAttributes<NodeResource>> event = groupEvent();
        given(mappingManager.isGroupMembershipUpdated(event)).willReturn(false);
        given(alfrescoClient.isNodeExist(GROUP_NODE_ID)).willReturn(true);

        groupManager.handleDeletionOrUpdation(event);

        then(nucleusClient).should(never()).deleteGroup(GROUP_ID);
    }

    @SuppressWarnings("unchecked")
    private static RepoEvent<DataAttributes<NodeResource>> groupEvent()
    {
        RepoEvent<DataAttributes<NodeResource>> event = mock(RepoEvent.class);
        DataAttributes<NodeResource> data = mock(DataAttributes.class);

        given(event.getData()).willReturn(data);
        given(data.getResource()).willReturn(groupResource());

        return event;
    }

    private static NodeResource groupResource()
    {
        Map<String, Serializable> props = new HashMap<>();
        props.put(AuthorizationConstants.GROUP_NAME_PROPERTY, GROUP_ID);

        return NodeResource.builder()
                .setId(GROUP_NODE_ID)
                .setNodeType(AuthorizationConstants.GROUP_TYPE)
                .setProperties(props)
                .build();
    }
}
