/*
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

package org.alfresco.hxi_connector.live_ingester.adapters.config.jackson;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.CONTENT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_BY_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.MODIFIED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.MODIFIED_BY_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE_OR_UPDATE;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.model.FieldType;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.ContentProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeEvent;

class UpdateNodeEventSerializerTest
{
    private static final String NODE_ID = "node-id";
    private static final String SOURCE_ID = "dummy-source-id";
    private static final long TIMESTAMP = 1_724_225_729_830L;

    @Mock
    private JsonGenerator jsonGenerator;

    @InjectMocks
    private UpdateNodeEventSerializer serializer;

    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldSerializeEmptyEvent()
    {
        UpdateNodeEvent emptyEvent = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP);

        String expectedJson = """
                [
                  {
                    "objectId": "%s",
                    "sourceId": "%s",
                    "eventType": "createOrUpdate",
                    "sourceTimestamp": 1724225729830
                  }
                ]""".formatted(NODE_ID, SOURCE_ID);
        String actualJson = serialize(emptyEvent);

        assertJsonEquals(expectedJson, actualJson);
    }

    @Test
    public void shouldSerializePropertiesToSet()
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>(CREATED_AT_PROPERTY, "2024-02-19T07:56:50.034Z"))
                .addMetadataInstruction(new NodeProperty<>(MODIFIED_BY_PROPERTY, "000-000-000"))
                .addMetadataInstruction(new NodeProperty<>(MODIFIED_AT_PROPERTY, "2025-02-19T07:56:50.034Z"));

        String expectedJson = """
                [
                  {
                    "objectId": "%s",
                    "sourceId": "%s",
                    "eventType": "createOrUpdate",
                    "sourceTimestamp": 1724225729830,
                    "properties": {
                      "createdAt": {"value": "2024-02-19T07:56:50.034Z", "annotation": "dateCreated"},
                      "modifiedAt": {"value": "2025-02-19T07:56:50.034Z", "annotation": "dateModified"},
                      "modifiedBy": {"value": "000-000-000", "annotation": "modifiedBy"}
                    }
                  }
                ]""".formatted(NODE_ID, SOURCE_ID);
        String actualJson = serialize(event);

        assertJsonEquals(expectedJson, actualJson);
    }

    @Test
    public void canCopeWithNullUsers()
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>(CREATED_BY_PROPERTY, null))
                .addMetadataInstruction(new NodeProperty<>(MODIFIED_BY_PROPERTY, null));

        String expectedJson = """
                [
                  {
                    "objectId": "%s",
                    "sourceId": "%s",
                    "eventType": "createOrUpdate",
                    "sourceTimestamp": 1724225729830,
                    "properties": {
                      "createdBy": {"value": null, "annotation": "createdBy"},
                      "modifiedBy": {"value": null, "annotation": "modifiedBy"}
                    }
                  }
                ]""".formatted(NODE_ID, SOURCE_ID);
        String actualJson = serialize(event);

        assertJsonEquals(expectedJson, actualJson);
    }

    @Test
    public void shouldSetContentProperty()
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addContentInstruction(new ContentProperty(CONTENT_PROPERTY, "content-id", "application/pdf",
                        "application/msword", 123L, "something.doc"));

        String expectedJson = """
                [
                  {
                    "objectId": "%s",
                    "sourceId": "%s",
                    "eventType": "createOrUpdate",
                    "sourceTimestamp": 1724225729830,
                    "properties": {
                      "cm:content": {
                        "file": {
                          "id": "content-id",
                          "content-type": "application/pdf",
                          "content-metadata": {
                            "size": 123,
                            "name": "something.doc",
                            "content-type": "application/msword"
                          }
                        }
                      }
                    }
                  }
                ]""".formatted(NODE_ID, SOURCE_ID);
        String actualJson = serialize(event);

        assertJsonEquals(expectedJson, actualJson);
    }

    @Test
    public void shouldOnlySendUpdatedContentMetadata()
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addContentInstruction(new ContentProperty(CONTENT_PROPERTY, null, null,
                        "application/msword", null, null));

        String expectedJson = """
                [
                  {
                    "objectId": "%s",
                    "sourceId": "%s",
                    "eventType": "createOrUpdate",
                    "sourceTimestamp": 1724225729830,
                    "properties": {
                      "cm:content": {
                        "file": {
                          "content-metadata": {
                            "content-type": "application/msword"
                          }
                        }
                      }
                    }
                  }
                ]""".formatted(NODE_ID, SOURCE_ID);
        String actualJson = serialize(event);

        assertJsonEquals(expectedJson, actualJson);
    }

    @Test
    void shouldNotWriteEmptyCollection() throws IOException
    {
        serializer.writeProperty(jsonGenerator, FieldType.VALUE, "testProperty", Collections.emptyList());

        verify(jsonGenerator, never()).writeObjectFieldStart(anyString());
    }

    @Test
    void shouldWriteNonEmptyCollection() throws IOException
    {
        List<String> nonEmptyList = List.of("value1", "value2");

        serializer.writeProperty(jsonGenerator, FieldType.VALUE, "testProperty", nonEmptyList);

        verify(jsonGenerator).writeObjectFieldStart("testProperty");
        verify(jsonGenerator).writeObjectField("value", nonEmptyList);
        verify(jsonGenerator).writeEndObject();
    }

    @Test
    void shouldWriteSingleValue() throws IOException
    {
        String value = "singleTestValue";

        serializer.writeProperty(jsonGenerator, FieldType.VALUE, "testProperty", value);

        verify(jsonGenerator).writeObjectFieldStart("testProperty");
        verify(jsonGenerator).writeObjectField("value", value);
        verify(jsonGenerator).writeEndObject();
    }

    @Test
    public void shouldReturnCorrectTypeForVariousValues()
    {
        assertAll(
                () -> assertEquals("boolean", serializer.selectTypeByValue(true)),
                () -> assertEquals("integer", serializer.selectTypeByValue(123)),
                () -> assertEquals("float", serializer.selectTypeByValue(123.45f)),
                () -> assertEquals("string", serializer.selectTypeByValue("testTypeValue")));
    }

    @Test
    public void shouldThrowExceptionForEmptyCollection()
    {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            serializer.selectTypeByValue(Collections.emptyList());
        });
        assertEquals("Empty collections should not be passed to selectTypeByValue.", exception.getMessage());
    }

    @Test
    public void shouldReturnStringForMixedTypeCollection()
    {
        assertEquals("string", serializer.selectTypeByValue(Arrays.asList(1, "test", 3.14)));
    }

    @Test
    public void shouldReturnStringForNullValue()
    {
        assertEquals("string", serializer.selectTypeByValue(null));
    }

    @SneakyThrows
    private String serialize(UpdateNodeEvent eventToSerialize)
    {
        ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(UpdateNodeEvent.class, serializer);
        objectMapper.registerModule(module);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(eventToSerialize);
    }

    @SneakyThrows
    private void assertJsonEquals(String expectedJson, String actualJson)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode expected = objectMapper.reader().readTree(expectedJson);
        JsonNode actual = objectMapper.reader().readTree(actualJson);
        assertEquals(expected, actual);
    }
}
