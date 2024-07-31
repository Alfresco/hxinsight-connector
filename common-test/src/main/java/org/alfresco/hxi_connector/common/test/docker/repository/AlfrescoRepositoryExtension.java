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
package org.alfresco.hxi_connector.common.test.docker.repository;

import static org.alfresco.hxi_connector.common.test.docker.repository.AlfrescoRepositoryContainer.REPOSITORY_TAG;
import static org.alfresco.hxi_connector.common.test.docker.repository.RepositoryType.COMMUNITY;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Stream;

import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.dockerfile.statement.SingleArgumentStatement;
import org.testcontainers.utility.DockerImageName;

import org.alfresco.hxi_connector.common.test.docker.util.DockerTags;

public class AlfrescoRepositoryExtension extends ImageFromDockerfile
{
    private static final String REPO_JAVA_VERSION = DockerTags.getOrDefault("repository.java.version", "17");
    private static final String LOCAL_IMAGE_DEFAULT = "localhost/alfresco/alfresco-content-repository-extended";
    private static final String JAVA_INSTALL_SCRIPT = """
            if [[ "$JAVA_VERSION" == "11" ]]; then
              ARCH=$(uname -m | sed s/86_//);
              JAVA_RELEASE=11.0.15_10;
              curl -fsLo java.tar.gz https://github.com/adoptium/temurin${JAVA_VERSION}-binaries/releases/download/jdk-${JAVA_RELEASE/_/+}/OpenJDK${JAVA_VERSION}U-jre_${ARCH}_linux_hotspot_${JAVA_RELEASE}.tar.gz &&
              tar xvfz java.tar.gz &&
              mv jdk-* /usr/lib/jvm/temurin-11-jdk &&
              update-alternatives --install /usr/bin/java java /usr/lib/jvm/temurin-11-jdk/bin/java 1 &&
              update-alternatives --remove java $(update-alternatives --display java | head -2 | tail -1 | cut -d " " -f6);
            fi
            """.replace("\n", " ");
    private static final String JAVA_SWITCH_SCRIPT_NAME = "java-switching-entrypoint.sh";
    private static final String JAVA_SWITCH_SCRIPT_PATH = "/" + JAVA_SWITCH_SCRIPT_NAME;
    private static final String JAVA_SWITCH_SCRIPT = """
            #!/bin/bash -e
            # Switch to Java 11 if it has been installed
            [ -d "/usr/lib/jvm/temurin-11-jdk" ] && export JAVA_HOME=/usr/lib/jvm/temurin-11-jdk
            exec "$@"
            """;

    public AlfrescoRepositoryExtension(@NonNull String extension)
    {
        this(extension, LOCAL_IMAGE_DEFAULT);
    }

    public AlfrescoRepositoryExtension(@NonNull String extension, @NonNull String localImageName)
    {
        this(extension, localImageName, COMMUNITY);
    }

    public AlfrescoRepositoryExtension(@NonNull String extension, @NonNull String localImageName, RepositoryType repositoryType)
    {
        this(DockerImageName.parse(repositoryType.getImageName()).withTag(REPOSITORY_TAG), extension, localImageName);
    }

    public AlfrescoRepositoryExtension(@NonNull DockerImageName imageToExtend, @NonNull String extension, @NonNull String localImageName)
    {
        super(localImageName);
        createImage(imageToExtend, extension);
    }

    private void createImage(DockerImageName dockerImageName, String extension)
    {
        Path jarFile = findTargetJar(extension);
        this.withFileFromPath(jarFile.toString(), jarFile)
                .withFileFromString(JAVA_SWITCH_SCRIPT_NAME, JAVA_SWITCH_SCRIPT)
                .withDockerfileFromBuilder(builder -> builder
                        .from(dockerImageName.toString())
                        .user("root")
                        .copy(jarFile.toString().replace("\\", "/"), "/usr/local/tomcat/webapps/alfresco/WEB-INF/lib/")
                        .withStatement(new SingleArgumentStatement("ARG", "JAVA_VERSION"))
                        .run(JAVA_INSTALL_SCRIPT)
                        .copy(JAVA_SWITCH_SCRIPT_NAME, "/")
                        .run("chmod +x " + JAVA_SWITCH_SCRIPT_PATH)
                        .run("chown -R -h alfresco /usr/local/tomcat")
                        .user("alfresco")
                        .entryPoint(JAVA_SWITCH_SCRIPT_PATH, "catalina.sh", "run", "-security")
                        .build())
                .withBuildArg("JAVA_VERSION", REPO_JAVA_VERSION);
    }

    @SneakyThrows
    private static Path findTargetJar(String name)
    {
        String path = "target";
        String extension = "jar";
        @Cleanup
        Stream<Path> files = Files.list(Paths.get(path));

        String extensionFileName = "%s-%s.%s".formatted(name, DockerTags.getHxiConnectorTag(), extension);
        return files.filter(nameEquals(extensionFileName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("%s file with name: '%s' not found in directory: '%s/'"
                        .formatted(extension.toUpperCase(Locale.ENGLISH), extensionFileName, path)));
    }

    private static Predicate<Path> nameEquals(final String name)
    {
        return path -> path != null && path.getFileName()
                .toString()
                .equalsIgnoreCase(name);
    }
}
