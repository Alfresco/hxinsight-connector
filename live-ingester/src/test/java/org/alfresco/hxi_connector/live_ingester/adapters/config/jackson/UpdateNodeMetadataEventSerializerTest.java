/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 Alfresco Software Limited
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

import static org.junit.Assert.assertEquals;

import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.CREATED_BY_USER_WITH_ID;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.IS_FILE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.MODIFIED_BY_USER_WITH_ID;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.NAME;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeMetadataEvent;

class UpdateNodeMetadataEventSerializerTest
{
    private static final String NODE_ID = "node-id";

    private final UpdateNodeMetadataEventSerializer serializer = new UpdateNodeMetadataEventSerializer();

    @Test
    public void shouldSerializeEmptyEvent()
    {
        UpdateNodeMetadataEvent emptyEvent = new UpdateNodeMetadataEvent(NODE_ID);

        String expectedJson = """
                {
                  "objectId": "%s",
                  "properties" : [ ]
                }""".formatted(NODE_ID);
        String actualJson = serialize(emptyEvent);

        assertJsonEquals(expectedJson, actualJson);
    }

    @Test
    public void shouldSerializePropertiesToSet()
    {
        UpdateNodeMetadataEvent event = new UpdateNodeMetadataEvent(NODE_ID)
                .set(NAME.withValue("some-name"))
                .set(IS_FILE.withValue(true))
                .set(MODIFIED_BY_USER_WITH_ID.withValue("000-000-000"));

        String expectedJson = """
                {
                  "objectId": "%s",
                  "properties" : [ {
                    "isFile" : true
                  }, {
                    "name" : "some-name"
                  }, {
                    "modifiedByUserWithId" : "000-000-000"
                  } ]
                }""".formatted(NODE_ID);
        String actualJson = serialize(event);

        assertJsonEquals(expectedJson, actualJson);
    }

    @Test
    public void shouldSerializePropertiesToUnset()
    {
        UpdateNodeMetadataEvent event = new UpdateNodeMetadataEvent(NODE_ID)
                .unset(NAME.getName())
                .unset(IS_FILE.getName())
                .unset(MODIFIED_BY_USER_WITH_ID.getName());

        String expectedJson = """
                {
                  "objectId": "%s",
                  "properties" : [ ],
                  "removedProperties" : [ "isFile", "name", "modifiedByUserWithId" ]
                }""".formatted(NODE_ID);
        String actualJson = serialize(event);

        assertJsonEquals(expectedJson, actualJson);
    }

    @Test
    public void canCopeWithNullUsers()
    {
        UpdateNodeMetadataEvent event = new UpdateNodeMetadataEvent(NODE_ID)
                .set(CREATED_BY_USER_WITH_ID.withValue(null))
                .set(MODIFIED_BY_USER_WITH_ID.withValue(null));

        String expectedJson = """
                {
                  "objectId": "%s",
                  "properties" : [ {
                    "createdByUserWithId" : null
                  }, {
                    "modifiedByUserWithId" : null
                  } ]
                }""".formatted(NODE_ID);
        String actualJson = serialize(event);

        assertJsonEquals(expectedJson, actualJson);
    }

    @SneakyThrows
    private String serialize(UpdateNodeMetadataEvent eventToSerialize)
    {
        ObjectMapper objectMapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(UpdateNodeMetadataEvent.class, serializer);
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
