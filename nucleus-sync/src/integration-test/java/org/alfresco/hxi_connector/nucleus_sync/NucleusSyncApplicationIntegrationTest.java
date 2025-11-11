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
package org.alfresco.hxi_connector.nucleus_sync;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import org.alfresco.hxi_connector.nucleus_sync.repository.GroupMappingRepository;
import org.alfresco.hxi_connector.nucleus_sync.repository.UserGroupMembershipRepository;
import org.alfresco.hxi_connector.nucleus_sync.repository.UserMappingRepository;

@SpringBootTest
@ActiveProfiles("test")
public class NucleusSyncApplicationIntegrationTest
{
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserMappingRepository userMappingRepository;

    @Autowired
    private GroupMappingRepository groupMappingRepository;

    @Autowired
    private UserGroupMembershipRepository userGroupMembershipRepository;

    @Test
    public void contextLoads()
    {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    public void repositoriesAreLoaded()
    {
        assertThat(userMappingRepository).isNotNull();
        assertThat(groupMappingRepository).isNotNull();
        assertThat(userGroupMembershipRepository).isNotNull();
    }
}
