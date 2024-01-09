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
package org.alfresco.hxi_connector.live_ingester.adapters.storage;

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

import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.storage.FileUploadRequest;

@ExtendWith(MockitoExtension.class)
class UrlFileUploaderTest
{
    private static final int STATUS_CODE_200 = 200;

    @Mock
    CamelContext camelContextMock;
    @Mock
    ProducerTemplate producerTemplateMock;
    @Mock
    Exchange exchangeMock;
    @Mock(strictness = Mock.Strictness.LENIENT)
    Message messageMock;

    @InjectMocks
    UrlFileUploader urlFileUploader;

    @BeforeEach
    void setUp()
    {
        given(camelContextMock.createProducerTemplate()).willReturn(producerTemplateMock);
        given(producerTemplateMock.send(any(String.class), any(Processor.class))).willReturn(exchangeMock);
        given(exchangeMock.getMessage()).willReturn(messageMock);
        given(messageMock.getHeader(any(String.class), any(Class.class))).willReturn(STATUS_CODE_200);
    }

    @Test
    void testUpload()
    {
        // given
        FileUploadRequest request = mock(FileUploadRequest.class);

        // when
        urlFileUploader.upload(request);

        // then
        then(messageMock).should().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        then(messageMock).shouldHaveNoMoreInteractions();
    }

    @Test
    void testUpload_invalidResponseStatusCode()
    {
        // given
        FileUploadRequest request = mock(FileUploadRequest.class);
        given(messageMock.getHeader(any(String.class), any(Class.class))).willReturn(500);

        // when
        Throwable thrown = catchThrowable(() -> urlFileUploader.upload(request));

        // then
        then(messageMock).should().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class);
        then(messageMock).should().getBody(String.class);
        assertThat(thrown)
                .isInstanceOf(LiveIngesterRuntimeException.class)
                .hasMessageContaining("received:", 500);
    }
}
