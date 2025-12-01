/*-
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
package org.alfresco.hxi_connector.nucleus_sync.services.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.nucleus_sync.client.AlfrescoClient;
import org.alfresco.hxi_connector.nucleus_sync.model.UserMapping;

@ExtendWith(MockitoExtension.class)
class UserGroupMembershipCacheBuilderServiceTest
{
    @Mock
    private AlfrescoClient alfrescoClient;

    @InjectMocks
    private UserGroupMembershipCacheBuilderService service;

    @Test
    void shouldBuildCacheSuccessfully()
    {
        // Given
        List<UserMapping> users = List.of(
                new UserMapping("john.doe@example.com", "jdoe", "550e8400-e29b-41d4-a716-446655440001"),
                new UserMapping("jane.smith@example.com", "jsmith", "550e8400-e29b-41d4-a716-446655440002"));

        when(alfrescoClient.getUserGroups("jdoe"))
                .thenReturn(List.of("GROUP_DEVELOPERS", "GROUP_ADMINS"));
        when(alfrescoClient.getUserGroups("jsmith"))
                .thenReturn(List.of("GROUP_MARKETING"));

        // When
        Map<String, List<String>> result = service.buildCacheFromAlfresco(users);

        // Then
        assertThat(result)
                .hasSize(2)
                .containsEntry("jdoe", List.of("GROUP_DEVELOPERS", "GROUP_ADMINS"))
                .containsEntry("jsmith", List.of("GROUP_MARKETING"));

        verify(alfrescoClient).getUserGroups("jdoe");
        verify(alfrescoClient).getUserGroups("jsmith");
    }

    @Test
    void shouldFailFastWhenAnyUserGroupFetchFails()
    {
        // Given
        List<UserMapping> users = List.of(
                new UserMapping("john.doe@example.com", "jdoe", "550e8400-e29b-41d4-a716-446655440001"),
                new UserMapping("jane.smith@example.com", "jsmith", "550e8400-e29b-41d4-a716-446655440002"),
                new UserMapping("bob.johnson@example.com", "bjohnson", "550e8400-e29b-41d4-a716-446655440003"));

        when(alfrescoClient.getUserGroups("jdoe"))
                .thenReturn(List.of("GROUP_DEVELOPERS"));
        when(alfrescoClient.getUserGroups("jsmith"))
                .thenThrow(new RuntimeException("API Error"));
        when(alfrescoClient.getUserGroups("bjohnson"))
                .thenReturn(List.of("GROUP_SALES"));

        // When/Then
        assertThatThrownBy(() -> service.buildCacheFromAlfresco(users))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch groups for user: jsmith")
                .hasCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldHandleEmptyUserList()
    {
        // Given
        List<UserMapping> users = List.of();

        // When
        Map<String, List<String>> result = service.buildCacheFromAlfresco(users);

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(alfrescoClient);
    }

    @Test
    void shouldHandleUserWithNoGroups()
    {
        // Given
        List<UserMapping> users = List.of(
                new UserMapping("john.doe@example.com", "jdoe", "550e8400-e29b-41d4-a716-446655440001"));
        when(alfrescoClient.getUserGroups("jdoe"))
                .thenReturn(List.of());

        // When
        Map<String, List<String>> result = service.buildCacheFromAlfresco(users);

        // Then
        assertThat(result)
                .hasSize(1)
                .containsEntry("jdoe", List.of());
    }
}
