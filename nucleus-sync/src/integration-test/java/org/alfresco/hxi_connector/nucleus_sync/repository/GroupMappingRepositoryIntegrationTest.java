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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import org.alfresco.hxi_connector.nucleus_sync.config.IntegrationTestConfig;
import org.alfresco.hxi_connector.nucleus_sync.entity.GroupMapping;

@DataJpaTest
@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
public class GroupMappingRepositoryIntegrationTest
{
    @Autowired
    private GroupMappingRepository repository;

    @Test
    void uniqueAlfrescoGroupIdConstraint_shouldPreventDuplicates()
    {
        GroupMapping group1 = new GroupMapping("GROUP_1", "Group One", LocalDateTime.now(), true, 5);
        repository.save(group1);

        GroupMapping group2 = new GroupMapping("GROUP_1", "Group Duplicate", LocalDateTime.now(), true, 3);

        assertThatThrownBy(() -> repository.saveAndFlush(group2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
