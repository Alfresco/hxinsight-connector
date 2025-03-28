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

import static java.util.Locale.ENGLISH;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.model.FieldType.FILE;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.model.FieldType.VALUE;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.jackson.exception.JsonSerializationException;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.model.FieldType;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.model.FileMetadata;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeEvent;

@Component
public class UpdateNodeEventSerializer extends StdSerializer<UpdateNodeEvent>
{
    private static final String NAME = "cm:name";
    private static final String CREATED_AT = "createdAt";
    private static final String MODIFIED_AT = "modifiedAt";
    private static final String ASPECTS_NAMES = "aspectsNames";
    private static final String TYPE = "type";
    private static final String CREATED_BY = "createdBy";
    private static final String MODIFIED_BY = "modifiedBy";

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

            jgen.writeStringField("eventType", event.getEventType().getValue());
            jgen.writeNumberField("sourceTimestamp", event.getTimestamp());

            if (!event.getMetadataPropertiesToSet().isEmpty() || !event.getContentPropertiesToSet().isEmpty())
            {
                jgen.writeObjectFieldStart("properties");
                event.getMetadataPropertiesToSet().values().forEach(property -> writeProperty(jgen, VALUE, property.name(), property.value()));
                event.getContentPropertiesToSet().values().forEach(property -> writeProperty(jgen, FILE, property.propertyName(), new FileMetadata(property)));
                jgen.writeEndObject();
            }

            jgen.writeEndObject();
            jgen.writeEndArray();
        }
        catch (Exception e)
        {
            throw new JsonSerializationException("Property serialization failed", e);
        }
    }

    void writeProperty(JsonGenerator jgen, FieldType fieldType, String name, Object value)
    {
        try
        {
            if (value instanceof Collection collection && collection.isEmpty())
            {
                return;
            }

            jgen.writeObjectFieldStart(name);

            if (value instanceof Map<?, ?> nestedMap)
            {
                writeNestedProperties(jgen, nestedMap);
            }
            else
            {
                jgen.writeObjectField(getLowerCase(fieldType), value);
                boolean hasAnnotation = writeAnnotation(jgen, name);
                if (!hasAnnotation)
                {
                    writeType(jgen, value);
                }
            }
            jgen.writeEndObject();
        }
        catch (IOException e)
        {
            throw new JsonSerializationException("UpdateNodeEvent serialization failed", e);
        }
    }

    private boolean writeAnnotation(JsonGenerator jgen, String name) throws IOException
    {
        boolean hasAnnotation = true;
        switch (name)
        {
        case CREATED_AT:
            jgen.writeObjectField("annotation", "dateCreated");
            break;
        case MODIFIED_AT:
            jgen.writeObjectField("annotation", "dateModified");
            break;
        case ASPECTS_NAMES:
            jgen.writeObjectField("annotation", "aspects");
            break;
        case NAME:
            jgen.writeObjectField("annotation", "name");
            break;
        case TYPE:
            jgen.writeObjectField("annotation", "type");
            break;
        case CREATED_BY:
            jgen.writeObjectField("annotation", "createdBy");
            break;
        case MODIFIED_BY:
            jgen.writeObjectField("annotation", "modifiedBy");
            break;
        default:
            hasAnnotation = false;
            break;
        }
        return hasAnnotation;
    }

    private void writeType(JsonGenerator jgen, Object value) throws IOException
    {
        if (value instanceof FileMetadata)
        {
            return;
        }

        String type = selectTypeByValue(value);
        jgen.writeObjectField("type", type);
    }

    String selectTypeByValue(Object value)
    {
        if (value instanceof Collection collection)
        {
            if (collection.isEmpty())
            {
                throw new IllegalArgumentException("Empty collections should not be passed to selectTypeByValue.");
            }
            return selectTypeByValue(collection.stream().findAny());
        }
        return determineType(value);
    }

    private String getLowerCase(Object object)
    {
        return object.toString().toLowerCase(ENGLISH);
    }

    private String determineType(Object value)
    {
        if (value instanceof Boolean)
        {
            return "boolean";
        }
        else if (value instanceof Integer)
        {
            return "integer";
        }
        else if (value instanceof Float || value instanceof Double)
        {
            return "float";
        }
        else if (value instanceof Collection<?> collection)
        {
            return collection.stream()
                    .findFirst()
                    .map(this::determineType)
                    .orElse("string");
        }
        else if (value instanceof Map<?, ?>)
        {
            return "object";
        }
        return "string";
    }

    private void writeNestedProperties(JsonGenerator jgen, Map<?, ?> nestedMap) throws IOException
    {
        writeMap(jgen, nestedMap, "object");
    }

    private void writeValue(JsonGenerator jgen, String key, Object value) throws IOException
    {
        jgen.writeObjectFieldStart(key);

        if (value instanceof Map<?, ?> nestedMap)
        {
            writeMap(jgen, nestedMap, "object");
        }
        else if (value instanceof Collection<?> collection)
        {
            jgen.writeObjectField("value", collection);
            jgen.writeStringField("type", determineType(collection));
        }
        else
        {
            jgen.writeStringField("value", value.toString());
            jgen.writeStringField("type", determineType(value));
        }

        jgen.writeEndObject();
    }

    private void writeMap(JsonGenerator jgen, Map<?, ?> map, String type) throws IOException
    {
        jgen.writeObjectFieldStart("value");
        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            writeValue(jgen, entry.getKey().toString(), entry.getValue());
        }
        jgen.writeEndObject();
        jgen.writeStringField("type", type);
    }
}
