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
package org.alfresco.hxi_connector.nucleus_sync.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class SchemaValidationIntegrationTest
{
    @Autowired
    private DataSource dataSource;

    @Test
    public void userMappingsTableExists() throws Exception
    {
        try (Connection conn = dataSource.getConnection())
        {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet tables = metaData.getTables(null, null, "USER_MAPPINGS", null))
            {
                assertThat(tables.next()).isTrue();
            }
        }
    }

    @Test
    public void groupMappingsTableExists() throws Exception
    {
        try (Connection conn = dataSource.getConnection())
        {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet tables = metaData.getTables(null, null, "GROUP_MAPPINGS", null))
            {
                assertThat(tables.next()).isTrue();
            }
        }
    }

    @Test
    public void userGroupMembershipsTableExists() throws Exception
    {
        try (Connection conn = dataSource.getConnection())
        {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet tables = metaData.getTables(null, null, "USER_GROUP_MEMBERSHIPS", null))
            {
                assertThat(tables.next()).isTrue();
            }
        }
    }

    @Test
    public void userMappingsHasUniqueEmailConstraint() throws Exception
    {
        try (Connection conn = dataSource.getConnection())
        {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet indexes = metaData.getIndexInfo(null, null, "USER_MAPPINGS", true, false))
            {
                boolean hasUniqueConstraint = false;
                while (indexes.next())
                {
                    String columnName = indexes.getString("COLUMN_NAME");
                    if ("EMAIL".equalsIgnoreCase(columnName))
                    {
                        hasUniqueConstraint = true;
                        break;
                    }
                }
                assertThat(hasUniqueConstraint).isTrue();
            }
        }
    }
}
