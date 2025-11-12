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
package org.alfresco.hxi_connector.nucleus_sync.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import org.alfresco.hxi_connector.nucleus_sync.config.IntegrationTestConfig;
import org.alfresco.hxi_connector.nucleus_sync.entity.UserGroupMembership;

@DataJpaTest
@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
public class UserGroupMembershipRepositoryIntegrationTest
{
    @Autowired
    private UserGroupMembershipRepository repository;

    @Test
    void uniqueConstraint_shouldPreventDuplicateGroupUserPairs()
    {
        UserGroupMembership membership1 = new UserGroupMembership(
                "GROUP_1", "USER_1", "user1@test.com", LocalDateTime.now(), true);
        repository.save(membership1);

        UserGroupMembership membership2 = new UserGroupMembership(
                "GROUP_1", "USER_1", "user1@test.com", LocalDateTime.now(), true);

        assertThatThrownBy(() -> repository.saveAndFlush(membership2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void uniqueConstraint_shouldAllowSameUserInDifferentGroups()
    {
        UserGroupMembership membership1 = new UserGroupMembership(
                "GROUP_1", "USER_1", "user1@test.com", LocalDateTime.now(), true);
        UserGroupMembership membership2 = new UserGroupMembership(
                "GROUP_2", "USER_1", "user1@test.com", LocalDateTime.now(), true);

        repository.save(membership1);
        repository.save(membership2);
        repository.flush();

        assertThat(repository.findAll())
                .hasSize(2)
                .extracting(UserGroupMembership::getAlfrescoGroupId)
                .containsExactlyInAnyOrder("GROUP_1", "GROUP_2");
    }

    @Test
    void findByEmailInAndIsActiveTrue_shouldReturnActiveMembershipsForMultipleEmails()
    {
        repository.save(
                new UserGroupMembership(
                        "GROUP_1", "USER_1", "user1@test.com", LocalDateTime.now(), true));
        repository.save(
                new UserGroupMembership(
                        "GROUP_1", "USER_2", "user2@test.com", LocalDateTime.now(), true));
        repository.save(
                new UserGroupMembership(
                        "GROUP_1", "USER_3", "user3@test.com", LocalDateTime.now(), false));

        List<UserGroupMembership> result = repository.findByEmailInAndIsActiveTrue(
                List.of("user1@test.com", "user3@test.com"));

        assertThat(result)
                .hasSize(1)
                .extracting(UserGroupMembership::getEmail)
                .containsExactly("user1@test.com");
    }
}
