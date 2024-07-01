/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2024 Alfresco Software Limited
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
package org.alfresco.hxi_connector.e2e_test;

import java.io.IOException;

import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.alfresco.hxi_connector.e2e_test.util.client.AwsS3Client;
import org.alfresco.hxi_connector.e2e_test.util.client.RepositoryNodesClient;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "GITHUB_ACTIONS_DEPLOY_TIMEOUT", matches = ".*")
/**
 * As of now this single test class needs command line docker-compose executed (../distribution/src/main/resources/docker-compose/docker-compose-minimal.yml) before it is run. It is excluded from the maven builds but run as a separate job in GitHub Actions workflow (thus, relies on GITHUB_ACTIONS_DEPLOY_TIMEOUT being set).
 */
public class CreateNodeE2eDCTest extends CreateNodeE2eTestBase
{
    @BeforeAll
    @SneakyThrows
    public void beforeAll()
    {
        repositoryNodesClient = new RepositoryNodesClient("http://localhost:8080", "admin", "admin");
        awsS3Client = new AwsS3Client("localhost", 4566, BUCKET_NAME);
        WireMock.configureFor("localhost", 8081);
    }

    @AfterEach
    void tearDown()
    {
        WireMock.reset();
    }

    @Test
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    void testCreateNodeContainingImageFile() throws IOException
    {
        testCreateNodeContainingImageFileBase();
    }

    @Test
    @SneakyThrows
    @SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert"})
    void testCreateNodeContainingTextFile() throws IOException
    {
        testCreateNodeContainingTextFileBase();
    }

}
