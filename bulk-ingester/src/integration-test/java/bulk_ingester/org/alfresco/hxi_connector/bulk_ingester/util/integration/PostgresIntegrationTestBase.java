/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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
package org.alfresco.hxi_connector.bulk_ingester.util.integration;

import java.time.Duration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.alfresco.hxi_connector.bulk_ingester.util.DockerTags;

@Testcontainers
@SuppressWarnings({"PMD.UseUtilityClass", "PMD.UnusedPrivateMethod"})
public class PostgresIntegrationTestBase
{
    private static final String POSTGRES_IMAGE = "postgres";
    private static final String POSTGRES_TAG = DockerTags.getOrDefault("postgres.tag", "14.4");
    @Container
    private static PostgreSQLContainer<?> postgres = createPostgresContainer();

    private static PostgreSQLContainer<?> createPostgresContainer()
    {
        return new PostgreSQLContainer<>(DockerImageName.parse(POSTGRES_IMAGE).withTag(POSTGRES_TAG))
                .withFileSystemBind("./src/integration-test/resources/alfresco-dump.sql", "/docker-entrypoint-initdb.d/init-postgres.sql", BindMode.READ_ONLY)
                .withPassword("alfresco")
                .withUsername("alfresco")
                .withDatabaseName("alfresco")
                .withCommand("-N 500")
                .withStartupTimeout(Duration.ofMinutes(2))
                .withReuse(true);
    }

    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry)
    {
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl());
        registry.add("spring.datasource.username", () -> postgres.getUsername());
        registry.add("spring.datasource.password", () -> postgres.getPassword());
    }
}
