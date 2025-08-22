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
import static java.util.stream.Collectors.toSet;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.model.FieldType.FILE;
import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.model.FieldType.VALUE;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.model.HierarchyMetadata;
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
    private static final String ANCESTORS="ancestors";

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
                event.getMetadataPropertiesToSet().values().forEach(property -> writeProperty(jgen, VALUE, property.name(), property.value(), true));
                event.getContentPropertiesToSet().values().forEach(property -> writeProperty(jgen, FILE, property.propertyName(), new FileMetadata(property), true));
                event.getAncestorsPropertiesToSet().values().forEach(property -> writeProperty(jgen,VALUE,property.propertyName(),new HierarchyMetadata(property),true));
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

    void writeProperty(JsonGenerator jgen, FieldType fieldType, String name, Object value, boolean shouldCheckForAnnotation)
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
                writeNestedMap(jgen, fieldType, nestedMap);
            }
            else
            {
                jgen.writeObjectField(getLowerCase(fieldType), value);
            }

            boolean hasAnnotation = false;
            if (shouldCheckForAnnotation)
            {
                hasAnnotation = writeAnnotation(jgen, name);
            }
            if (!hasAnnotation)
            {
                writeType(jgen, value,name);
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
        case ANCESTORS:
            jgen.writeObjectField("annotation", "hierarchy");
            break;
        default:
            hasAnnotation = false;
            break;
        }
        return hasAnnotation;
    }

    private void writeType(JsonGenerator jgen, Object value,String propertyName) throws IOException
    {
        if (value instanceof FileMetadata)
        {
            return;
        }

        String type = determineType(value);
        if(Objects.equals(propertyName, "ancestors"))
        {
            type="object";
        }
        jgen.writeObjectField("type", type);
    }

    private String getLowerCase(Object object)
    {
        return object.toString().toLowerCase(ENGLISH);
    }

    protected String determineType(Object value)
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
            if (collection.isEmpty())
            {
                throw new IllegalArgumentException("Empty collections should not be passed to selectTypeByValue.");
            }
            Set<String> types = collection.stream()
                    .map(this::determineType)
                    .collect(toSet());
            // If there is a mixture of types then return "string" as a fallback.
            if (types.size() == 1)
            {
                return types.iterator().next();
            }
            return "string";
        }
        else if (value instanceof Map<?, ?>)
        {
            return "object";
        }
        return "string";
    }

    private void writeNestedMap(JsonGenerator jgen, FieldType fieldType, Map<?, ?> map) throws IOException
    {
        jgen.writeObjectFieldStart(getLowerCase(fieldType));
        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            jgen.writeObjectField(key, value);
        }
        jgen.writeEndObject();
    }
}
