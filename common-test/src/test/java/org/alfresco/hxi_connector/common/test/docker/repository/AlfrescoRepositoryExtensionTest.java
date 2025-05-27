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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.images.builder.dockerfile.DockerfileBuilder;
import org.testcontainers.utility.DockerImageName;

import org.alfresco.hxi_connector.common.test.docker.util.DockerTags;

@ExtendWith(MockitoExtension.class)
class AlfrescoRepositoryExtensionTest
{

    @TempDir
    Path tempDir;

    @Mock
    private DockerfileBuilder dockerfileBuilder;

    private Path mockJarFile;
    private String extensionName = "test-extension";

    @BeforeEach
    void setUp() throws IOException
    {
        Path targetDir = tempDir.resolve("target");
        Files.createDirectories(targetDir);

        mockJarFile = targetDir.resolve(extensionName + "-1.0.0.jar");
        Files.createFile(mockJarFile);
    }

    @Test
    void shouldCreateExtensionWithDefaultValues() throws IOException
    {
        try (MockedStatic<DockerTags> dockerTagsMock = mockStatic(DockerTags.class);
                MockedStatic<Files> filesMock = mockStatic(Files.class))
        {

            String hxiConnectorTag = "1.0.0";
            dockerTagsMock.when(DockerTags::getHxiConnectorTag).thenReturn(hxiConnectorTag);
            dockerTagsMock.when(() -> DockerTags.getOrDefault(anyString(), anyString())).thenReturn("17");
            dockerTagsMock.when(DockerTags::getRepositoryTag).thenReturn("latest");

            Stream<Path> mockPathStream = Stream.of(mockJarFile);
            filesMock.when(() -> Files.list(any(Path.class))).thenReturn(mockPathStream);

            AlfrescoRepositoryExtension extension = new AlfrescoRepositoryExtension(extensionName);

            assertTrue(extension.getDockerImageName()
                    .contains("localhost/alfresco/alfresco-content-repository-extended"));
        }
    }

    @Test
    void shouldCreateExtensionWithCustomImageName() throws IOException
    {
        try (MockedStatic<DockerTags> dockerTagsMock = mockStatic(DockerTags.class);
                MockedStatic<Files> filesMock = mockStatic(Files.class))
        {

            String hxiConnectorTag = "1.0.0";
            String customImageName = "custom/image";
            dockerTagsMock.when(DockerTags::getHxiConnectorTag).thenReturn(hxiConnectorTag);
            dockerTagsMock.when(() -> DockerTags.getOrDefault(anyString(), anyString())).thenReturn("17");

            Stream<Path> mockPathStream = Stream.of(mockJarFile);
            filesMock.when(() -> Files.list(any(Path.class))).thenReturn(mockPathStream);

            AlfrescoRepositoryExtension extension = new AlfrescoRepositoryExtension(extensionName,
                    customImageName);

            assertEquals(customImageName, extension.getDockerImageName());
        }
    }

    @Test
    void shouldCreateExtensionWithSpecificRepositoryType() throws IOException
    {
        try (MockedStatic<DockerTags> dockerTagsMock = mockStatic(DockerTags.class);
                MockedStatic<Files> filesMock = mockStatic(Files.class))
        {

            String hxiConnectorTag = "1.0.0";
            String customImageName = "custom/enterprise/image";
            dockerTagsMock.when(DockerTags::getHxiConnectorTag).thenReturn(hxiConnectorTag);
            dockerTagsMock.when(() -> DockerTags.getOrDefault(anyString(), anyString())).thenReturn("17");
            dockerTagsMock.when(DockerTags::getRepositoryTag).thenReturn("latest");

            Stream<Path> mockPathStream = Stream.of(mockJarFile);
            filesMock.when(() -> Files.list(any(Path.class))).thenReturn(mockPathStream);

            AlfrescoRepositoryExtension extension = new AlfrescoRepositoryExtension(
                    extensionName, customImageName, RepositoryType.ENTERPRISE);

            assertEquals(customImageName, extension.getDockerImageName());
        }
    }

    @Test
    void shouldCreateExtensionWithCustomDockerImageName() throws IOException
    {
        try (MockedStatic<DockerTags> dockerTagsMock = mockStatic(DockerTags.class);
                MockedStatic<Files> filesMock = mockStatic(Files.class))
        {

            String hxiConnectorTag = "1.0.0";
            String customImageName = "custom/specific/image";
            DockerImageName dockerImageName = DockerImageName.parse("alfresco/test").withTag("2.0");

            dockerTagsMock.when(DockerTags::getHxiConnectorTag).thenReturn(hxiConnectorTag);

            Stream<Path> mockPathStream = Stream.of(mockJarFile);
            filesMock.when(() -> Files.list(any(Path.class))).thenReturn(mockPathStream);

            AlfrescoRepositoryExtension extension = new AlfrescoRepositoryExtension(
                    dockerImageName, extensionName, customImageName);

            assertEquals(customImageName, extension.getDockerImageName());
        }
    }

    @Test
    void shouldThrowExceptionWhenJarNotFound() throws IOException
    {
        try (MockedStatic<DockerTags> dockerTagsMock = mockStatic(DockerTags.class);
                MockedStatic<Files> filesMock = mockStatic(Files.class))
        {

            String hxiConnectorTag = "1.0.0";
            dockerTagsMock.when(DockerTags::getHxiConnectorTag).thenReturn(hxiConnectorTag);

            Stream<Path> emptyPathStream = Stream.empty();
            filesMock.when(() -> Files.list(any(Path.class))).thenReturn(emptyPathStream);

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> new AlfrescoRepositoryExtension(extensionName));

            assertTrue(exception.getMessage().contains("not found"));
            assertTrue(exception.getMessage().contains(extensionName + "-" + hxiConnectorTag + ".jar"));
        }
    }
}
