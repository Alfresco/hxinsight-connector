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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.hxi_connector.live_ingester.subsystem.Exceptions.IamSyncException;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.junit.jupiter.api.Test;

class AuthorizationConstantsTest
{
    @Test
    void shouldReturnUserNameFromCmUserName()
    {
        RepoEvent<DataAttributes<NodeResource>> event = eventWithProperties(
                Map.of(AuthorizationConstants.USERNAME_PROPERTY_1, "alice"), null);

        assertThat(AuthorizationConstants.fetchUserId(event)).isEqualTo("alice");
    }

    @Test
    void shouldFallBackToUsrUserNameWhenCmUserNameIsAbsent()
    {
        RepoEvent<DataAttributes<NodeResource>> event = eventWithProperties(
                Map.of(AuthorizationConstants.USERNAME_PROPERTY_2, "bob"), null);

        assertThat(AuthorizationConstants.fetchUserId(event)).isEqualTo("bob");
    }

    @Test
    void shouldFallBackToResourceBeforeWhenLiveResourceIsNull()
    {
        RepoEvent<DataAttributes<NodeResource>> event = eventWithProperties(
                null, Map.of(AuthorizationConstants.USERNAME_PROPERTY_1, "carol"));

        assertThat(AuthorizationConstants.fetchUserId(event)).isEqualTo("carol");
    }

    @Test
    void shouldThrowIamSyncExceptionWhenNoUsernameProperty()
    {
        RepoEvent<DataAttributes<NodeResource>> event = eventWithProperties(
                Map.of("cm:other", "x"), null);
        given(event.getId()).willReturn("evt-42");

        assertThatThrownBy(() -> AuthorizationConstants.fetchUserId(event))
                .isInstanceOf(IamSyncException.class)
                .hasMessageContaining("evt-42");
    }

    @Test
    void shouldThrowIamSyncExceptionWhenEventIsNull()
    {
        assertThatThrownBy(() -> AuthorizationConstants.fetchUserId(null))
                .isInstanceOf(IamSyncException.class);
    }

    @Test
    void userTypesShouldContainPersonAndUserConstants()
    {
        assertThat(AuthorizationConstants.USER_TYPES)
                .containsExactlyInAnyOrder(AuthorizationConstants.PERSON_TYPE, AuthorizationConstants.USER_TYPE);
    }

    @SuppressWarnings("unchecked")
    private static RepoEvent<DataAttributes<NodeResource>> eventWithProperties(
            Map<String, ? extends Serializable> currentProps,
            Map<String, ? extends Serializable> previousProps)
    {
        RepoEvent<DataAttributes<NodeResource>> event = mock(RepoEvent.class);
        DataAttributes<NodeResource> data = mock(DataAttributes.class);
        given(event.getData()).willReturn(data);

        if (currentProps != null)
        {
            NodeResource resource = NodeResource.builder()
                    .setProperties(new HashMap<>(currentProps))
                    .build();
            given(data.getResource()).willReturn(resource);
        }
        if (previousProps != null)
        {
            NodeResource before = NodeResource.builder()
                    .setProperties(new HashMap<>(previousProps))
                    .build();
            given(data.getResourceBefore()).willReturn(before);
        }
        return event;
    }
}

