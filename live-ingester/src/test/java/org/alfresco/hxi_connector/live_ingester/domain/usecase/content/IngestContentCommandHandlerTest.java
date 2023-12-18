package org.alfresco.hxi_connector.live_ingester.domain.usecase.content;

import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequest;
import org.alfresco.hxi_connector.live_ingester.domain.ports.transform_engine.TransformRequester;

@ExtendWith(MockitoExtension.class)
class IngestContentCommandHandlerTest
{
    static final long TIMESTAMP = 1_234_567_890L;
    static final String NODE_ID = "12341234-1234-1234-1234-123412341234";
    static final String PDF_MIMETYPE = "application/pdf";

    @Mock
    TransformRequester transformRequester;
    @InjectMocks
    IngestContentCommandHandler ingestContentCommandHandler;

    @Test
    void shouldRequestNodeContentTransformation()
    {
        // given
        IngestContentCommand command = new IngestContentCommand(TIMESTAMP, NODE_ID);

        // when
        ingestContentCommandHandler.handle(command);

        // then
        TransformRequest expectedTransformationRequest = new TransformRequest(TIMESTAMP, NODE_ID, PDF_MIMETYPE);

        then(transformRequester).should().requestTransform(expectedTransformationRequest);
    }
}
