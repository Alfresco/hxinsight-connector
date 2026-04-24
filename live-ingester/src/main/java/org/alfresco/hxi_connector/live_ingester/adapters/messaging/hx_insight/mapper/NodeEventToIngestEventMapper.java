/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.mapper;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hyland.sdk.cic.ingest.object.IngestEvent;
import org.hyland.sdk.cic.ingest.object.IngestEventProperties;
import org.hyland.sdk.cic.ingest.object.PropertyArray;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.AuthorityInfo;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.ContentProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.DeleteNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.PermissionsProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType;

@Component
public class NodeEventToIngestEventMapper
{
    private static final Map<String, String> ANNOTATION_MAP = Map.of(
            "cm:name", "name",
            "createdAt", "dateCreated",
            "modifiedAt", "dateModified",
            "aspectsNames", "aspects",
            "type", "type",
            "createdBy", "createdBy",
            "modifiedBy", "modifiedBy",
            "ancestors", "hierarchy",
            "PERMISSIONS", "principals");

    public IngestEvent map(NodeEvent event)
    {
        if (event instanceof UpdateNodeEvent updateEvent)
        {
            return mapUpdate(updateEvent);
        }
        else if (event instanceof DeleteNodeEvent deleteEvent)
        {
            return mapDelete(deleteEvent);
        }
        throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
    }

    private IngestEvent mapDelete(DeleteNodeEvent event)
    {
        return IngestEvent.builder(IngestEvent.Type.DELETE, event.getObjectId())
                .sourceId(event.getSourceId())
                .date(Instant.ofEpochMilli(event.getTimestamp()))
                .build();
    }

    private IngestEvent mapUpdate(UpdateNodeEvent event)
    {
        IngestEvent.Type type = event.getEventType() == EventType.CREATE_OR_UPDATE
                ? IngestEvent.Type.CREATE_OR_UPDATE
                : IngestEvent.Type.DELETE;

        IngestEventProperties.Builder propsBuilder = IngestEventProperties.builder();
        boolean hasProperties = false;

        for (NodeProperty<?> property : event.getMetadataPropertiesToSet().values())
        {
            IngestEventProperties mapped = mapMetadataProperty(property);
            if (mapped != null)
            {
                propsBuilder.put(property.name(), mapped);
                hasProperties = true;
            }
        }

        for (ContentProperty property : event.getContentPropertiesToSet().values())
        {
            propsBuilder.put(property.propertyName(), mapContentProperty(property));
            hasProperties = true;
        }

        for (PermissionsProperty property : event.getPermissionsPropertiesToSet().values())
        {
            propsBuilder.put(property.propertyName(), mapPermissionsProperty(property));
            hasProperties = true;
        }

        IngestEvent.Builder builder = IngestEvent.builder(type, event.getObjectId())
                .sourceId(event.getSourceId())
                .date(Instant.ofEpochMilli(event.getTimestamp()));

        if (hasProperties)
        {
            builder.properties(propsBuilder.build());
        }

        return builder.build();
    }

    private IngestEventProperties mapMetadataProperty(NodeProperty<?> property)
    {
        Object value = property.value();

        if (value instanceof Collection<?> collection && collection.isEmpty())
        {
            return null;
        }

        IngestEventProperties.Builder builder = IngestEventProperties.builder();
        putValue(builder, "value", value);

        String annotation = ANNOTATION_MAP.get(property.name());
        if (annotation != null)
        {
            builder.put("annotation", annotation);
        }
        else
        {
            builder.put("type", determineType(value));
        }

        return builder.build();
    }

    private IngestEventProperties mapContentProperty(ContentProperty property)
    {
        IngestEventProperties.Builder fileBuilder = IngestEventProperties.builder();

        if (property.id() != null)
        {
            fileBuilder.put("id", property.id());
        }
        if (property.mimeType() != null)
        {
            fileBuilder.put("content-type", property.mimeType());
        }

        if (property.sourceMimeType() != null || property.sourceSizeInBytes() != null || property.sourceFileName() != null)
        {
            fileBuilder.put("content-metadata", contentMetadata -> {
                if (property.sourceMimeType() != null)
                {
                    contentMetadata.put("content-type", property.sourceMimeType());
                }
                if (property.sourceSizeInBytes() != null)
                {
                    contentMetadata.put("size", property.sourceSizeInBytes());
                }
                if (property.sourceFileName() != null)
                {
                    contentMetadata.put("name", property.sourceFileName());
                }
            });
        }

        return IngestEventProperties.builder()
                .put("file", fileBuilder.build())
                .build();
    }

    private IngestEventProperties mapPermissionsProperty(PermissionsProperty property)
    {
        IngestEventProperties.Builder builder = IngestEventProperties.builder();

        builder.put("value", valueBuilder -> {
            valueBuilder.put("read", mapAuthorityList(property.allowAccess()));
            valueBuilder.put("deny", mapAuthorityList(property.denyAccess()));
            valueBuilder.put("principalsType", "effective");
        });

        String annotation = ANNOTATION_MAP.get(property.propertyName());
        if (annotation != null)
        {
            builder.put("annotation", annotation);
        }

        return builder.build();
    }

    private PropertyArray mapAuthorityList(List<AuthorityInfo> authorities)
    {
        if (authorities == null || authorities.isEmpty())
        {
            return PropertyArray.empty();
        }
        IngestEventProperties[] items = authorities.stream()
                .map(auth -> IngestEventProperties.builder()
                        .put("id", auth.id())
                        .put("type", auth.type().name())
                        .build())
                .toArray(IngestEventProperties[]::new);
        return PropertyArray.of(items);
    }

    @SuppressWarnings("unchecked")
    private void putValue(IngestEventProperties.Builder builder, String key, Object value)
    {
        if (value == null)
        {
            builder.putNull(key);
        }
        else if (value instanceof String s)
        {
            builder.put(key, s);
        }
        else if (value instanceof Boolean b)
        {
            builder.put(key, b);
        }
        else if (value instanceof Integer i)
        {
            builder.put(key, i);
        }
        else if (value instanceof Long l)
        {
            builder.put(key, l);
        }
        else if (value instanceof Float f)
        {
            builder.put(key, (double) f);
        }
        else if (value instanceof Double d)
        {
            builder.put(key, d);
        }
        else if (value instanceof Collection<?> collection)
        {
            builder.put(key, toPropertyArray(collection));
        }
        else if (value instanceof Map<?, ?> map)
        {
            builder.put(key, mapToProperties((Map<String, Object>) map));
        }
        else
        {
            builder.put(key, value.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private PropertyArray toPropertyArray(Collection<?> collection)
    {
        if (collection.isEmpty())
        {
            return PropertyArray.empty();
        }

        Object first = collection.iterator().next();

        if (first instanceof String)
        {
            return PropertyArray.of(collection.stream().map(Object::toString).toArray(String[]::new));
        }
        else if (first instanceof Integer)
        {
            return PropertyArray.of(collection.stream().mapToInt(o -> (Integer) o).toArray());
        }
        else if (first instanceof Long)
        {
            return PropertyArray.of(collection.stream().mapToLong(o -> (Long) o).toArray());
        }
        else if (first instanceof Double)
        {
            return PropertyArray.of(collection.stream().mapToDouble(o -> (Double) o).toArray());
        }
        else if (first instanceof Boolean)
        {
            boolean[] arr = new boolean[collection.size()];
            int i = 0;
            for (Object o : collection)
            {
                arr[i++] = (Boolean) o;
            }
            return PropertyArray.of(arr);
        }
        else if (first instanceof Map)
        {
            IngestEventProperties[] items = collection.stream()
                    .map(o -> mapToProperties((Map<String, Object>) o))
                    .toArray(IngestEventProperties[]::new);
            return PropertyArray.of(items);
        }

        return PropertyArray.of(collection.stream().map(Object::toString).toArray(String[]::new));
    }

    @SuppressWarnings("unchecked")
    private IngestEventProperties mapToProperties(Map<String, Object> map)
    {
        IngestEventProperties.Builder builder = IngestEventProperties.builder();
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            putValue(builder, entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    private String determineType(Object value)
    {
        if (value instanceof Boolean)
        {
            return "boolean";
        }
        else if (value instanceof Integer || value instanceof Long)
        {
            return "integer";
        }
        else if (value instanceof Float || value instanceof Double)
        {
            return "float";
        }
        else if (value instanceof Map<?, ?>)
        {
            return "object";
        }
        else if (value instanceof Collection<?> collection)
        {
            if (collection.isEmpty())
            {
                return "string";
            }
            Object first = collection.iterator().next();
            return determineType(first);
        }
        return "string";
    }
}
