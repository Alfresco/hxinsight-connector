/*
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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.storage;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static software.amazon.awssdk.http.HttpStatusCode.OK;

import java.io.InputStream;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Body;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.common.config.properties.Application;
import org.alfresco.hxi_connector.common.exception.EndpointClientErrorException;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.common.test.docker.util.DockerContainers;
import org.alfresco.hxi_connector.common.test.util.LoggingUtils;
import org.alfresco.hxi_connector.live_ingester.IntegrationCamelTestBase;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformEngineFileStorage;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.File;

@SpringBootTest(classes = {
        IntegrationProperties.class,
        SharedFileStoreClient.class,
        Application.class},
        properties = "logging.level.org.alfresco=DEBUG")
@EnableAutoConfiguration
@EnableRetry
@ActiveProfiles("test")
@Testcontainers
class SharedFileStoreClientIntegrationTest extends IntegrationCamelTestBase
{
    private static final String FILE_ID = "file-id";
    private static final String SFS_DOWNLOAD_FILE_PATH = "/alfresco/api/-default-/private/sfs/versions/1/file/" + FILE_ID;
    private static final int RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 0;

    @Container
    @SuppressWarnings("PMD.FieldNamingConventions")
    static final WireMockContainer wireMockServer = DockerContainers.createWireMockContainer();

    @SpyBean
    TransformEngineFileStorage sharedFileStoreClient;

    @BeforeAll
    static void beforeAll()
    {
        WireMock.configureFor(wireMockServer.getHost(), wireMockServer.getPort());
    }

    @Test
    void testDownloadFile()
    {
        // given
        String fileContent = "Dummy's file dummy content";
        givenThat(get(SFS_DOWNLOAD_FILE_PATH)
                .willReturn(aResponse()
                        .withStatus(OK)
                        .withBody(fileContent.getBytes())));

        // when
        File file = sharedFileStoreClient.downloadFile(FILE_ID);

        // then
        then(sharedFileStoreClient).should().downloadFile(FILE_ID);
        WireMock.verify(getRequestedFor(urlPathEqualTo(SFS_DOWNLOAD_FILE_PATH)));
        assertThat(file)
                .isNotNull()
                .extracting(File::data)
                .extracting(SharedFileStoreClientIntegrationTest::mapToBytesArray)
                .extracting(String::new)
                .isEqualTo(fileContent);
    }

    @Test
    void testDownloadFile_serverError_doRetry()
    {
        // given
        givenThat(get(SFS_DOWNLOAD_FILE_PATH)
                .willReturn(serverError()));

        // when
        Throwable thrown = catchThrowable(() -> sharedFileStoreClient.downloadFile(FILE_ID));

        // then
        then(sharedFileStoreClient).should(times(RETRY_ATTEMPTS)).downloadFile(FILE_ID);
        assertThat(thrown).cause().isInstanceOf(EndpointServerErrorException.class);
    }

    @Test
    void testDownloadFile_clientError_dontRetry()
    {
        // given
        givenThat(get(SFS_DOWNLOAD_FILE_PATH)
                .willReturn(badRequest()));

        // when
        Throwable thrown = catchThrowable(() -> sharedFileStoreClient.downloadFile(FILE_ID));

        // then
        then(sharedFileStoreClient).should(times(1)).downloadFile(FILE_ID);
        assertThat(thrown).cause().isInstanceOf(EndpointClientErrorException.class);
    }

    @Test
    @SuppressWarnings({"ThrowableNotThrown", "ResultOfMethodCallIgnored"})
    void testLoggingOnError()
    {
        // given
        givenThat(get(SFS_DOWNLOAD_FILE_PATH)
                .willReturn(badRequest().withResponseBody(new Body("{\"error\": \"Bad request\"}"))));
        ListAppender<ILoggingEvent> logEntries = LoggingUtils.createLogsListAppender(SharedFileStoreClient.class);

        // when
        catchThrowable(() -> sharedFileStoreClient.downloadFile(FILE_ID));

        // then
        List<String> logs = logEntries.list.stream().map(ILoggingEvent::getFormattedMessage).toList();
        assertThat(logs)
                .isNotEmpty()
                .last().asString()
                .contains("Body: {\"error\": \"Bad request\"}")
                .doesNotContain("Authorization=");
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry)
    {
        registry.add("alfresco.transform.shared-file-store.base-url", () -> "http://" + wireMockServer.getHost() + ":" + wireMockServer.getPort());
        registry.add("alfresco.transform.shared-file-store.retry.attempts", () -> RETRY_ATTEMPTS);
        registry.add("alfresco.transform.shared-file-store.retry.initialDelay", () -> RETRY_DELAY_MS);
    }

    @SneakyThrows
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static byte[] mapToBytesArray(InputStream inputStream)
    {
        return inputStream.readAllBytes();
    }
}
