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
package org.alfresco.hxi_connector.common.test.docker.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.utility.DockerImageName;

class AlfrescoRepositoryContainerTest
{

    @Test
    void shouldCreateContainerWithCommunityType()
    {
        try (AlfrescoRepositoryContainer container = new AlfrescoRepositoryContainer())
        {
            DockerImageName expectedImage = DockerImageName.parse(RepositoryType.COMMUNITY.getImageName())
                    .withTag(AlfrescoRepositoryContainer.REPOSITORY_TAG);
            assertEquals(expectedImage.toString(), container.getDockerImageName());
        }
    }

    @Test
    void shouldCreateContainerWithSpecificType()
    {
        try (AlfrescoRepositoryContainer container = new AlfrescoRepositoryContainer(RepositoryType.ENTERPRISE))
        {
            DockerImageName expectedImage = DockerImageName.parse(RepositoryType.ENTERPRISE.getImageName())
                    .withTag(AlfrescoRepositoryContainer.REPOSITORY_TAG);
            assertEquals(expectedImage.toString(), container.getDockerImageName());
        }
    }

    @Test
    void shouldReturnCorrectBaseUrl()
    {
        try (AlfrescoRepositoryContainer container = new AlfrescoRepositoryContainer() {
            @Override
            public String getHost()
            {
                return "test-host";
            }

            @Override
            public Integer getMappedPort(int originalPort)
            {
                return 9090;
            }
        })
        {
            String baseUrl = container.getBaseUrl();

            assertEquals("http://test-host:9090", baseUrl);
        }
    }

    @Test
    void shouldSetJavaToolOptions()
    {
        try (AlfrescoRepositoryContainer container = new AlfrescoRepositoryContainer())
        {
            String options = "-Xmx2g";

            container.withJavaToolOpts(options);

            assertTrue(container.getEnvMap().containsKey("JAVA_TOOL_OPTIONS"));
            assertEquals(options, container.getEnvMap().get("JAVA_TOOL_OPTIONS"));
        }
    }

    @Test
    void shouldSetJavaOpts()
    {
        try (AlfrescoRepositoryContainer container = new AlfrescoRepositoryContainer())
        {
            String options = "-XX:MaxPermSize=512m";

            container.withJavaOpts(options);

            assertTrue(container.getEnvMap().containsKey("JAVA_OPTS"));
            assertEquals(options, container.getEnvMap().get("JAVA_OPTS"));
        }
    }

    @Test
    void shouldSetCatalinaOpts()
    {
        try (AlfrescoRepositoryContainer container = new AlfrescoRepositoryContainer())
        {
            String options = "-Xdebug";

            container.withCatalinaOpts(options);

            assertTrue(container.getEnvMap().containsKey("CATALINA_OPTS"));
            assertEquals(options, container.getEnvMap().get("CATALINA_OPTS"));
        }
    }

    @Test
    void shouldConfigureContainerWithDebugAndJavaOptions()
    {
        try (AlfrescoRepositoryContainer container = new AlfrescoRepositoryContainer())
        {
            container.configure();

            assertTrue(container.getEnvMap().containsKey("CATALINA_OPTS"));
            assertTrue(container.getEnvMap().containsKey("JAVA_TOOL_OPTIONS"));
            assertTrue(container.getEnvMap().get("CATALINA_OPTS").contains("jdwp=transport=dt_socket"));
            assertTrue(container.getEnvMap().get("JAVA_TOOL_OPTIONS").contains("encryption.keystore.type=JCEKS"));
            assertEquals(2, container.getExposedPorts().size());
            assertTrue(container.getExposedPorts().contains(8080));
            assertTrue(container.getExposedPorts().contains(8000));
        }
    }

    @Test
    void shouldPullRepositoryImage()
    {
        try (MockedStatic<DockerClientFactory> dockerClientFactoryMock = Mockito
                .mockStatic(DockerClientFactory.class))
        {
            DockerClientFactory clientFactoryMock = mock(DockerClientFactory.class);
            DockerClient dockerClientMock = mock(DockerClient.class);
            PullImageCmd pullImageCmdMock = mock(PullImageCmd.class);

            dockerClientFactoryMock.when(DockerClientFactory::instance).thenReturn(clientFactoryMock);
            when(clientFactoryMock.client()).thenReturn(dockerClientMock);
            when(dockerClientMock.pullImageCmd(Mockito.anyString())).thenReturn(pullImageCmdMock);
            when(pullImageCmdMock.exec(Mockito.any())).thenReturn(null);

            AlfrescoRepositoryContainer.pullRepositoryImage(RepositoryType.COMMUNITY);

            verify(dockerClientMock).pullImageCmd(Mockito.contains(RepositoryType.COMMUNITY.getImageName()));

            ArgumentCaptor<PullImageResultCallback> callbackCaptor = ArgumentCaptor
                    .forClass(PullImageResultCallback.class);
            verify(pullImageCmdMock).exec(callbackCaptor.capture());
            assertNotNull(callbackCaptor.getValue());
        }
    }
}
