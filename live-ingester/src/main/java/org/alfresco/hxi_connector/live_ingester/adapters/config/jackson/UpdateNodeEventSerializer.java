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

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.model.FieldType.FILE;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.model.FieldType.VALUE;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.jackson.exception.JsonSerializationException;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.model.FieldType;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.model.FileMetadata;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType;

@Component
public class UpdateNodeEventSerializer extends StdSerializer<UpdateNodeEvent>
{
    private static final String NAME = "cm:name";

    public UpdateNodeEventSerializer()
    {
        this(null);
    }

    public UpdateNodeEventSerializer(Class<UpdateNodeEvent> t)
    {
        super(t);
    }

    @Override
    public void serialize(UpdateNodeEvent event, JsonGenerator jgen, SerializerProvider provider)
    {
        try
        {
            jgen.writeStartArray();
            jgen.writeStartObject();

            jgen.writeStringField("objectId", event.getObjectId());
            jgen.writeStringField("sourceId", event.getSourceId());

            jgen.writeStringField("eventType", serializeEventType(event.getEventType()));
            jgen.writeNumberField("sourceTimestamp", event.getTimestamp());

            if (!event.getMetadataPropertiesToSet().isEmpty() || !event.getContentPropertiesToSet().isEmpty())
            {
                jgen.writeObjectFieldStart("properties");
                event.getMetadataPropertiesToSet().values().forEach(property -> writeProperty(jgen, VALUE, property.name(), property.value()));
                event.getContentPropertiesToSet().values().forEach(property -> writeProperty(jgen, FILE, property.propertyName(), new FileMetadata(property)));
                jgen.writeEndObject();
            }

            Set<String> metadataPropertiesToUnset = event.getPropertiesToUnset();
            if (!metadataPropertiesToUnset.isEmpty())
            {
                jgen.writeArrayFieldStart("removedProperties");
                metadataPropertiesToUnset.forEach(propertyName -> writePropertyName(jgen, propertyName));
                jgen.writeEndArray();
            }

            jgen.writeEndObject();
            jgen.writeEndArray();
        }
        catch (Exception e)
        {
            throw new JsonSerializationException("Property serialization failed", e);
        }
    }

    private void writeProperty(JsonGenerator jgen, FieldType fieldType, String name, Object value)
    {
        try
        {
            if (fieldType.equals(VALUE) && name.equals(NAME))
            {
                jgen.writeObjectFieldStart(name);
                jgen.writeObjectField(getLowerCase(fieldType), value);
                jgen.writeObjectField("annotation", "name");
                jgen.writeEndObject();
                return;
            }
            jgen.writeObjectFieldStart(name);
            jgen.writeObjectField(getLowerCase(fieldType), value);
            jgen.writeEndObject();
        }
        catch (IOException e)
        {
            throw new JsonSerializationException("UpdateNodeEvent serialization failed", e);
        }
    }

    private void writePropertyName(JsonGenerator jgen, String propertyName)
    {
        try
        {
            jgen.writeString(propertyName);
        }
        catch (IOException e)
        {
            throw new JsonSerializationException("UpdateNodeEvent serialization failed", e);
        }
    }

    private String serializeEventType(EventType eventType)
    {
        return getLowerCase(eventType);
    }

    private String getLowerCase(Object object)
    {
        return object.toString().toLowerCase(ENGLISH);
    }
}
