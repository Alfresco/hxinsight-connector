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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.MimeTypeMapper.DEFAULT_MIME_TYPES;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.MimeTypeMapper.EMPTY_MIME_TYPE;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.mapper.MimeTypeMapper.getType;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Transform;

@ExtendWith(MockitoExtension.class)
class MimeTypeMapperTest
{

    @Mock
    private IntegrationProperties mockIntegrationProperties;
    @Mock
    private IntegrationProperties.Alfresco mockAlfrescoProperties;
    @Mock
    private Transform mockTransformProperties;
    @Mock
    private Transform.MimeType mockMimeTypeProperties;

    @InjectMocks
    private MimeTypeMapper objectUnderTest;

    @BeforeEach
    void mockProperties()
    {
        given(mockIntegrationProperties.alfresco()).willReturn(mockAlfrescoProperties);
        given(mockAlfrescoProperties.transform()).willReturn(mockTransformProperties);
        given(mockTransformProperties.mimeType()).willReturn(mockMimeTypeProperties);
    }

    @ParameterizedTest
    @ValueSource(strings = {"text/plain", "application/pdf", "text/html", "image/png", "image/jpg", "image/gif", "application/msword"})
    void givenNoMappingsConfigured_whenAnySourceMimeTypeAsInput_thenAlwaysReturnDefaultMimeType(String sourceMimeType)
    {
        given(mockMimeTypeProperties.mapping()).willReturn(null);
        // when
        String resultMimeType = objectUnderTest.mapMimeType(sourceMimeType);
        // then
        assertEquals(DEFAULT_MIME_TYPES.getOrDefault(sourceMimeType, getWildcardMappingFromDefault(sourceMimeType)), resultMimeType);
    }

    @ParameterizedTest
    @CsvSource({"text/html,application/pdf", "text/plain,application/pdf", "application/msword,application/pdf", "application/pdf,application/pdf"})
    void givenExactMappingsConfigured_whenSourceMimeTypeMatchesTarget_thenReturnMatchingTargetMimeType(String sourceMimeType, String targetMimeType)
    {
        given(mockMimeTypeProperties.mapping()).willReturn(Map.of(sourceMimeType, targetMimeType));
        // when
        String resultMimeType = objectUnderTest.mapMimeType(sourceMimeType);
        // then
        assertEquals(targetMimeType, resultMimeType);
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/png", "image/jpg", "image/gif", "text/richtext", "image/bmp"})
    void givenExactMappingsConfigured_whenSourceMimeTypeMatchesNone_thenReturnEmptyMimeType(String sourceMimeType)
    {
        given(mockMimeTypeProperties.mapping()).willReturn(Map.of("text/html", "application/pdf",
                "text/plain", "application/pdf",
                "application/msword", "application/pdf",
                "application/pdf", "application/pdf"));
        // when
        String resultMimeType = objectUnderTest.mapMimeType(sourceMimeType);
        // then
        assertEquals(EMPTY_MIME_TYPE, resultMimeType);
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/png", "image/gif", "image/bmp"})
    void givenExactAndSubtypeWildcardMappingsConfigured_whenSourceMimeTypeMatchesExactAndWildcard_thenReturnExactMimeType(String sourceMimeType)
    {
        String mimeTypePng = "image/png";
        given(mockMimeTypeProperties.mapping()).willReturn(Map.of(mimeTypePng, mimeTypePng,
                "image/bmp", mimeTypePng,
                "image/gif", mimeTypePng,
                "image/*", "image/jpg",
                "*", "application/pdf"));
        // when
        String resultMimeType = objectUnderTest.mapMimeType(sourceMimeType);
        // then
        assertEquals(mimeTypePng, resultMimeType);
    }

    @ParameterizedTest
    @ValueSource(strings = {"image/png", "image/gif", "image/bmp"})
    void givenOnlyExactAndSubtypeWildcardMappingsConfigured_whenSourceMimeTypeMatchesSubtypeWildcard_thenReturnSubtypeWildcardMimeType(String sourceMimeType)
    {
        String mimeTypeJpg = "image/jpg";
        given(mockMimeTypeProperties.mapping()).willReturn(Map.of("image/*", mimeTypeJpg,
                "*", "application/pdf"));
        // when
        String resultMimeType = objectUnderTest.mapMimeType(sourceMimeType);
        // then
        assertEquals(mimeTypeJpg, resultMimeType);
    }

    @ParameterizedTest
    @CsvSource({"text/html,application/pdf", "text/plain,application/pdf", "application/msword,application/pdf", "application/pdf,application/pdf",
            "image/png,image/png", "image/gif,image/png", "image/bmp,image/png", "image/jpg,image/jpg", "image/tif,image/jpg", "text/richtext,application/pdf"})
    void givenExactAndSubtypeWildcardMappingsConfigured_whenSourceMimeMapped_thenReturnTargetMimeTypeAsProvidedInParams(String sourceMimeType, String targetMimeType)
    {
        given(mockMimeTypeProperties.mapping()).willReturn(Map.of("image/png", "image/png",
                "image/bmp", "image/png",
                "image/gif", "image/png",
                "image/*", "image/jpg",
                "*", "application/pdf"));
        // when
        String resultMimeType = objectUnderTest.mapMimeType(sourceMimeType);
        // when
        assertEquals(targetMimeType, resultMimeType);
    }

    private String getWildcardMappingFromDefault(String inputType)
    {

        for (Map.Entry<String, String> mapping : DEFAULT_MIME_TYPES.entrySet())
        {
            if (mapping.getKey().endsWith("/*") && getType(inputType).equals(getType(mapping.getKey())))
            {
                return StringUtils.defaultIfBlank(mapping.getValue(), EMPTY_MIME_TYPE);
            }
        }
        return DEFAULT_MIME_TYPES.entrySet().stream()
                .filter(mapping -> mapping.getKey().equals("*"))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(EMPTY_MIME_TYPE);
    }
}
