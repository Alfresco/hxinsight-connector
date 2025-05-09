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

package org.alfresco.hxi_connector.live_ingester.adapters.config.jackson;

import static java.util.Locale.ENGLISH;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.jackson.exception.JsonSerializationException;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.DeleteNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType;

@Component
public class DeleteNodeEventSerializer extends StdSerializer<DeleteNodeEvent>
{
    public DeleteNodeEventSerializer()
    {
        this(null);
    }

    public DeleteNodeEventSerializer(Class<DeleteNodeEvent> t)
    {
        super(t);
    }

    @Override
    public void serialize(DeleteNodeEvent event, JsonGenerator jgen, SerializerProvider provider)
    {
        try
        {
            jgen.writeStartArray();
            jgen.writeStartObject();

            jgen.writeStringField("objectId", event.getObjectId());
            jgen.writeStringField("sourceId", event.getSourceId());
            jgen.writeNumberField("sourceTimestamp", event.getTimestamp());
            jgen.writeStringField("eventType", serializeEventType(event.getEventType()));

            jgen.writeEndObject();
            jgen.writeEndArray();
        }
        catch (Exception e)
        {
            throw new JsonSerializationException("Property serialization failed", e);
        }
    }

    private String serializeEventType(EventType eventType)
    {
        return eventType.toString().toLowerCase(ENGLISH);
    }
}
