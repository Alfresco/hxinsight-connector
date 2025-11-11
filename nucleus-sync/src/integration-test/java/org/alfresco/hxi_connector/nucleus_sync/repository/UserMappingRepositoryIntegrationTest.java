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
import org.alfresco.hxi_connector.nucleus_sync.entity.UserMapping;

@DataJpaTest
@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
public class UserMappingRepositoryIntegrationTest
{
    @Autowired
    private UserMappingRepository repository;

    @Test
    public void uniqueEmailConstraint_shouldPreventDuplicates()
    {
        UserMapping user1 = new UserMapping("test@example.com", "alf123", "nuc123", LocalDateTime.now(), true);

        repository.save(user1);

        UserMapping user2 = new UserMapping("test@example.com", "alf456", "nuc456", LocalDateTime.now(), true);

        assertThatThrownBy(() -> repository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void findByIsActiveTrue_shouldOnlyReturnActiveUsers()
    {
        repository.save(new UserMapping("active@test.com", "alf1", "nuc1", LocalDateTime.now(), true));
        repository.save(new UserMapping("inactive@test.com", "alf2", "nuc2", LocalDateTime.now(), false));

        List<UserMapping> activeUsers = repository.findByIsActiveTrue();

        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getEmail()).isEqualTo("active@test.com");
    }

    @Test
    public void existsByEmail_shouldReturnTrueWhenEmailExists()
    {
        repository.save(new UserMapping("exists@test.com", "alf1", "nuc1", LocalDateTime.now(), true));

        assertThat(repository.existsByEmail("exists@test.com")).isTrue();
        assertThat(repository.existsByEmail("notexists@test.com")).isFalse();
    }
}
