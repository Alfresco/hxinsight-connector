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
package org.alfresco.hxi_connector.live_ingester.adapters.storage.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.adapters.storage.FileUploadRequest;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class HttpFileUploaderTest
{
    private static final int STATUS_CODE_200 = 200;
    private static final int STATUS_CODE_500 = 500;

    @Mock
    CamelContext camelContextMock;
    @Mock
    ProducerTemplate producerTemplateMock;
    @Mock
    Exchange exchangeMock;
    @Mock
    Message messageMock;

    @InjectMocks
    HttpFileUploader httpFileUploader;

    @BeforeEach
    void setUp()
    {
        given(camelContextMock.createProducerTemplate()).willReturn(producerTemplateMock);
        given(producerTemplateMock.send(any(String.class), any(Processor.class))).willReturn(exchangeMock);
        given(exchangeMock.getMessage()).willReturn(messageMock);
    }

    @Test
    void testUpload()
    {
        // given
        FileUploadRequest request = mock(FileUploadRequest.class);
        httpClientWillRespondWith(STATUS_CODE_200);

        // when
        httpFileUploader.upload(request);

        // then
        then(messageMock).should().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        then(messageMock).shouldHaveNoMoreInteractions();
    }

    @Test
    void testUpload_invalidResponseStatusCode()
    {
        // given
        FileUploadRequest request = mock(FileUploadRequest.class);
        httpClientWillRespondWith(STATUS_CODE_500);

        // when
        Throwable thrown = catchThrowable(() -> httpFileUploader.upload(request));

        // then
        then(messageMock).should().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        then(messageMock).should().getBody(String.class);
        assertThat(thrown)
                .isInstanceOf(LiveIngesterRuntimeException.class)
                .hasMessageContaining("received:", 500);
    }

    private void httpClientWillRespondWith(int statusCode)
    {
        given(messageMock.getHeader(any(String.class), any(Class.class))).willReturn(statusCode);
    }
}
