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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.alfresco.hxi_connector.live_ingester.subsystem.Exceptions.IamSyncException;
import org.alfresco.hxi_connector.live_ingester.subsystem.Strategy.UserFetchingStrategy;
import org.alfresco.hxi_connector.nucleus_client.client.AlfrescoClient;
import org.alfresco.hxi_connector.nucleus_client.client.ClientException;
import org.alfresco.hxi_connector.nucleus_client.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_client.dto.IamUser;
import org.alfresco.hxi_connector.nucleus_client.dto.NucleusUserMappingInput;
import org.alfresco.hxi_connector.nucleus_client.dto.NucleusUserMappingOutput;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserManagerTest
{
    private static final String EXTERNAL_ID = "alice";
    private static final String NODE_ID = "user-node-uuid";
    private static final String EMAIL = "alice@example.com";
    private static final String IAM_USER_ID = "iam-uid-1";

    @Mock
    private NucleusClient nucleusClient;
    @Mock
    private MappingManager mappingManager;
    @Mock
    private UserFetchingStrategy userFetchingStrategy;
    @Mock
    private AlfrescoClient alfrescoClient;

    private UserManager userManager;

    @BeforeEach
    void setUp()
    {
        userManager = new UserManager(nucleusClient, mappingManager, userFetchingStrategy, alfrescoClient);
    }

    @Test
    void shouldCallCreateUserMappingsOnMapUser()
    {
        NucleusUserMappingInput input = new NucleusUserMappingInput(IAM_USER_ID, EXTERNAL_ID);

        userManager.mapUser(input);

        then(nucleusClient).should().createUserMappings(List.of(input));
    }

    @Test
    void deleteUserShouldReturnTrueOnSuccess()
    {
        assertThat(userManager.deleteUser(EXTERNAL_ID)).isTrue();

        then(nucleusClient).should().deleteUserMapping(EXTERNAL_ID);
    }

    @Test
    void deleteUserShouldReturnFalseWhenClientFails()
    {
        doThrow(new ClientException("boom")).when(nucleusClient).deleteUserMapping(EXTERNAL_ID);

        assertThat(userManager.deleteUser(EXTERNAL_ID)).isFalse();
    }

    @Test
    void createOrMapUserShouldSkipWhenMappingAlreadyExists()
    {
        given(nucleusClient.fetchUserMappingByExternalUserId(EXTERNAL_ID))
                .willReturn(Optional.of(new NucleusUserMappingOutput(IAM_USER_ID, EXTERNAL_ID)));

        userManager.createOrMapUser(EMAIL, EXTERNAL_ID);

        then(userFetchingStrategy).should(never()).fetchUserByEmailId(any());
        then(nucleusClient).should(never()).createUserMappings(anyList());
    }

    @Test
    void createOrMapUserShouldMapWhenIamUserFoundByEmail()
    {
        given(nucleusClient.fetchUserMappingByExternalUserId(EXTERNAL_ID)).willReturn(Optional.empty());
        given(userFetchingStrategy.fetchUserByEmailId(EMAIL))
                .willReturn(Optional.of(new IamUser("alice", IAM_USER_ID, EMAIL)));

        userManager.createOrMapUser(EMAIL, EXTERNAL_ID);

        ArgumentCaptor<List<NucleusUserMappingInput>> captor = ArgumentCaptor.forClass(List.class);
        then(nucleusClient).should().createUserMappings(captor.capture());
        assertThat(captor.getValue()).containsExactly(new NucleusUserMappingInput(IAM_USER_ID, EXTERNAL_ID));
    }

    @Test
    void createOrMapUserShouldThrowWhenNoIamUserFound()
    {
        given(nucleusClient.fetchUserMappingByExternalUserId(EXTERNAL_ID)).willReturn(Optional.empty());
        given(userFetchingStrategy.fetchUserByEmailId(EMAIL)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userManager.createOrMapUser(EMAIL, EXTERNAL_ID))
                .isInstanceOf(IamSyncException.class)
                .hasMessageContaining(EMAIL);
    }

    @Test
    void handleUpdateOrDeleteShouldDeleteOnDeleteEvent()
    {
        RepoEvent<DataAttributes<NodeResource>> event = userEvent(
                AuthorizationConstants.EVENT_DELETE_TYPE, AuthorizationConstants.PERSON_TYPE);
        given(mappingManager.isGroupMembershipUpdated(event)).willReturn(false);
        given(alfrescoClient.isNodeExist(NODE_ID)).willReturn(false);

        userManager.handleUpdateOrDelete(event);

        then(nucleusClient).should().deleteUserMapping(EXTERNAL_ID);
        then(mappingManager).should(never()).updateGroupMapping(any());
    }

    @Test
    void handleUpdateOrDeleteShouldDelegateToMappingManagerForPersonUpdate()
    {
        RepoEvent<DataAttributes<NodeResource>> event = userEvent(
                AuthorizationConstants.EVENT_UPDATED_TYPE, AuthorizationConstants.PERSON_TYPE);
        given(mappingManager.isGroupMembershipUpdated(event)).willReturn(true);
        given(alfrescoClient.isNodeExist(NODE_ID)).willReturn(true);

        userManager.handleUpdateOrDelete(event);

        then(mappingManager).should().updateGroupMapping(event);
        then(nucleusClient).should(never()).deleteUserMapping(any());
    }

    @Test
    void handleUpdateOrDeleteShouldIgnoreNonPersonUpdate()
    {
        RepoEvent<DataAttributes<NodeResource>> event = userEvent(
                AuthorizationConstants.EVENT_UPDATED_TYPE, AuthorizationConstants.USER_TYPE);
        given(mappingManager.isGroupMembershipUpdated(event)).willReturn(true);
        given(alfrescoClient.isNodeExist(NODE_ID)).willReturn(true);

        userManager.handleUpdateOrDelete(event);

        then(mappingManager).should(never()).updateGroupMapping(any());
        then(nucleusClient).should(never()).deleteUserMapping(any());
    }

    @SuppressWarnings("unchecked")
    private static RepoEvent<DataAttributes<NodeResource>> userEvent(String eventType, String nodeType)
    {
        RepoEvent<DataAttributes<NodeResource>> event = mock(RepoEvent.class);
        DataAttributes<NodeResource> data = mock(DataAttributes.class);
        given(event.getData()).willReturn(data);
        given(event.getType()).willReturn(eventType);

        Map<String, Serializable> props = new HashMap<>();
        props.put(AuthorizationConstants.USERNAME_PROPERTY_1, EXTERNAL_ID);

        NodeResource resource = NodeResource.builder()
                .setId(NODE_ID)
                .setNodeType(nodeType)
                .setProperties(props)
                .build();
        given(data.getResource()).willReturn(resource);
        given(data.getResourceBefore()).willReturn(resource);
        return event;
    }
}
