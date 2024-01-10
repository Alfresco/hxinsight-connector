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

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.IS_FILE;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.MODIFIED_BY_USER_WITH_ID;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PredefinedNodeMetadataProperty.NAME;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeMetadataEvent;

class UpdateNodeMetadataEventSerializerTest
{
    private final UpdateNodeMetadataEventSerializer serializer = new UpdateNodeMetadataEventSerializer();

    @Test
    public void shouldSerializeEmptyEvent()
    {
        UpdateNodeMetadataEvent emptyEvent = UpdateNodeMetadataEvent.create();

        String expectedJson = """
                {
                  "setProperties" : [ ],
                  "unsetProperties" : [ ]
                }""";
        String actualJson = serialize(emptyEvent);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void shouldSerializePropertiesToSet()
    {
        UpdateNodeMetadataEvent event = UpdateNodeMetadataEvent.create()
                .set(NAME.withValue("some-name"))
                .set(IS_FILE.withValue(true))
                .set(MODIFIED_BY_USER_WITH_ID.withValue("000-000-000"));

        String expectedJson = """
                {
                  "setProperties" : [ {
                    "isFile" : true
                  }, {
                    "name" : "some-name"
                  }, {
                    "modifiedByUserWithId" : "000-000-000"
                  } ],
                  "unsetProperties" : [ ]
                }""";
        String actualJson = serialize(event);

        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void shouldSerializePropertiesToUnset()
    {
        UpdateNodeMetadataEvent event = UpdateNodeMetadataEvent.create()
                .unset(NAME.getName())
                .unset(IS_FILE.getName())
                .unset(MODIFIED_BY_USER_WITH_ID.getName());

        String expectedJson = """
                {
                  "setProperties" : [ ],
                  "unsetProperties" : [ "isFile", "name", "modifiedByUserWithId" ]
                }""";
        String actualJson = serialize(event);

        assertEquals(expectedJson, actualJson);
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
}
