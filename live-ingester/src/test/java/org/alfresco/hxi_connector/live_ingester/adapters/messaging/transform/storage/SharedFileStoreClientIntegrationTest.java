package org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.storage;

import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.github.tomakehurst.wiremock.client.WireMock;
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
import org.testcontainers.utility.DockerImageName;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.domain.exception.EndpointClientErrorException;
import org.alfresco.hxi_connector.live_ingester.domain.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformEngineFileStorage;
import org.alfresco.hxi_connector.live_ingester.util.DockerTags;

@SpringBootTest(classes = {
        IntegrationProperties.class,
        SharedFileStoreClient.class})
@ActiveProfiles("test")
@EnableAutoConfiguration
@EnableRetry
@Testcontainers
class SharedFileStoreClientIntegrationTest
{
    private static final String WIREMOCK_IMAGE = "wiremock/wiremock";
    private static final String WIREMOCK_TAG = DockerTags.getOrDefault("wiremock.tag", "3.3.1");
    private static final String FILE_ID = "file-id";
    private static final String SFS_DOWNLOAD_FILE_PATH = "/alfresco/api/-default-/private/sfs/versions/1/file/" + FILE_ID;
    private static final int RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 0;

    @Container
    @SuppressWarnings("PMD.FieldNamingConventions")
    static final WireMockContainer wireMockServer = new WireMockContainer(DockerImageName.parse(WIREMOCK_IMAGE).withTag(WIREMOCK_TAG));

    @SpyBean
    TransformEngineFileStorage sharedFileStoreClient;

    @BeforeAll
    static void beforeAll()
    {
        WireMock.configureFor(wireMockServer.getHost(), wireMockServer.getPort());
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

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry)
    {
        registry.add("alfresco.transform.shared-file-store.host", () -> "http://" + wireMockServer.getHost());
        registry.add("alfresco.transform.shared-file-store.port", wireMockServer::getPort);
        registry.add("alfresco.transform.shared-file-store.retry.attempts", () -> RETRY_ATTEMPTS);
        registry.add("alfresco.transform.shared-file-store.retry.initialDelay", () -> RETRY_DELAY_MS);
    }
}
