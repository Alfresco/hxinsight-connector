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

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.PullResponseItem;
import lombok.NonNull;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import org.alfresco.hxi_connector.common.test.docker.util.DockerTags;

@SuppressWarnings("PMD.LongVariable")
public class AlfrescoRepositoryContainer extends GenericContainer<AlfrescoRepositoryContainer>
{
    static final String REPOSITORY_IMAGE_DEFAULT = "alfresco/alfresco-content-repository-community";
    static final String REPOSITORY_ENTERPRISE_IMAGE_DEFAULT = "quay.io/alfresco/alfresco-content-repository";
    static final String REPOSITORY_TAG = DockerTags.getRepositoryTag();
    private static final int REPO_PORT_DEFAULT = 8080;
    private static final int REPO_DEBUG_PORT_DEFAULT = 8000;

    public AlfrescoRepositoryContainer()
    {
        this(false);
    }

    public static void pullRepositoryImage(boolean enterprise)
    {
        DockerClient dockerClient = DockerClientFactory.instance().client();
        PullImageResultCallback callback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item)
            {
                super.onNext(item);
            }
        };
        dockerClient.pullImageCmd(String.valueOf(DockerImageName.parse(!enterprise ? REPOSITORY_IMAGE_DEFAULT : REPOSITORY_ENTERPRISE_IMAGE_DEFAULT).withTag(REPOSITORY_TAG)))
                .exec(callback);
    }

    public AlfrescoRepositoryContainer(boolean enterprise)
    {
        this(DockerImageName.parse(!enterprise ? REPOSITORY_IMAGE_DEFAULT : REPOSITORY_ENTERPRISE_IMAGE_DEFAULT).withTag(REPOSITORY_TAG));
    }

    public AlfrescoRepositoryContainer(@NonNull DockerImageName dockerImageName)
    {
        super(dockerImageName);
        this.addExposedPorts(REPO_PORT_DEFAULT, REPO_DEBUG_PORT_DEFAULT);
    }

    public AlfrescoRepositoryContainer(@NonNull AlfrescoRepositoryExtension repositoryExtension)
    {
        super(repositoryExtension);
        this.addExposedPorts(REPO_PORT_DEFAULT, REPO_DEBUG_PORT_DEFAULT);
    }

    public int getPort()
    {
        return this.getMappedPort(REPO_PORT_DEFAULT);
    }

    public String getBaseUrl()
    {
        return "http://%s:%s".formatted(this.getHost(), this.getPort());
    }

    public AlfrescoRepositoryContainer withCatalinaOpts(String opts)
    {
        this.withEnv("CATALINA_OPTS", opts);
        return this;
    }

    public AlfrescoRepositoryContainer withJavaToolOpts(String opts)
    {
        this.withEnv("JAVA_TOOL_OPTIONS", opts);
        return this;
    }

    public AlfrescoRepositoryContainer withJavaOpts(String opts)
    {
        this.withEnv("JAVA_OPTS", opts);
        return this;
    }

    @Override
    protected void configure()
    {
        super.configure();
        this.withCatalinaOpts("-agentlib:jdwp=transport=dt_socket,address=*:%s,server=y,suspend=n".formatted(REPO_DEBUG_PORT_DEFAULT))
                .withJavaToolOpts("""
                        -Dencryption.keystore.type=JCEKS
                        -Dencryption.cipherAlgorithm=DESede/CBC/PKCS5Padding
                        -Dencryption.keyAlgorithm=DESede
                        -Dencryption.keystore.location=/usr/local/tomcat/shared/classes/alfresco/extension/keystore/keystore
                        -Dmetadata-keystore.password=mp6yc0UD9e
                        -Dmetadata-keystore.aliases=metadata
                        -Dmetadata-keystore.metadata.password=oKIWzVdEdA
                        -Dmetadata-keystore.metadata.algorithm=DESede
                        """.replace("\n", " "));
    }
}
