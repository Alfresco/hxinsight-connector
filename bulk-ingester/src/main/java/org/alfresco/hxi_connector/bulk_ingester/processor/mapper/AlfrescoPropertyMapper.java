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

package org.alfresco.hxi_connector.bulk_ingester.processor.mapper;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import static org.alfresco.hxi_connector.bulk_ingester.processor.mapper.AlfrescoNodeMapper.CONTENT_PROPERTY;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.alfresco.elasticsearch.db.connector.model.AlfrescoNode;
import org.alfresco.elasticsearch.db.connector.model.PropertyValue;
import org.alfresco.elasticsearch.db.connector.model.PropertyValueType;
import org.alfresco.hxi_connector.bulk_ingester.processor.model.ContentInfo;

@Slf4j
@RequiredArgsConstructor
public class AlfrescoPropertyMapper
{
    private final AlfrescoNode alfrescoNode;
    private final String propertyName;

    public Optional<Map.Entry<String, Serializable>> performMapping()
    {
        /* Properties might be duplicated - for example they can have different locale (we can have two descriptions with "en_US_" and "en_UK_" locale) and in this case we want to process them together */
        List<Serializable> propertyValues = alfrescoNode.getNodeProperties().stream()
                .filter(nodeProperty -> nodeProperty.getPropertyKey().getLocalName().equals(propertyName))
                .map(nodeProperty -> mapPropertyValue(nodeProperty.getPropertyValue()))
                .flatMap(Optional::stream)
                .toList();

        if (propertyValues.isEmpty())
        {
            return empty();
        }
        else if (propertyValues.size() == 1)
        {
            return of(Map.entry(propertyName, propertyValues.get(0)));
        }
        else
        {
            return of(Map.entry(propertyName, (Serializable) propertyValues));
        }
    }

    private Optional<Serializable> mapPropertyValue(PropertyValue propertyValue)
    {
        try
        {
            return getPersistedValue(propertyValue).flatMap(persistedValue -> switch (PropertyValueType.getPropertyValueType(propertyValue.getActualType()))
            {
            case CONTENT_DATA_ID -> getContentValue(persistedValue);
            case DATE -> getDateValue(persistedValue);
            case NODEREF -> getNodeRefValue(persistedValue);
            default -> of(persistedValue);
            });
        }
        catch (Exception e)
        {
            log.warn(format("Error occurred while trying to map property %s value %s of node %s", propertyName, propertyValue, alfrescoNode.getId()), e);

            return empty();
        }
    }

    private Optional<Serializable> getNodeRefValue(Serializable persistedValue)
    {
        try
        {
            String stringValue = (String) persistedValue;
            String[] protocolSplit = stringValue.split("://");

            String protocol = protocolSplit[0];

            String[] storeSplit = protocolSplit[1].split("/");
            String storeIdentifier = storeSplit[0];
            String id = storeSplit[1];

            return of((Serializable) Map.of(
                    "id", id,
                    "storeRef", Map.of("identifier", storeIdentifier, "protocol", protocol)));
        }
        catch (Exception e)
        {
            log.error(format("Cannot deserialize noderef property %s with value %s for node %s", propertyName, persistedValue, alfrescoNode.getId()), e);

            return empty();
        }
    }

    private Optional<ContentInfo> getContentValue(Serializable propertyValue)
    {
        if (!propertyName.equals(CONTENT_PROPERTY))
        {
            log.info("Found content under property with name {} for node {}. Content different from cm:content won't be ingested.", propertyName, alfrescoNode.getId());

            return empty();
        }

        return alfrescoNode.getContentData()
                .stream()
                .filter(contentMetadata -> Objects.equals(contentMetadata.getId(), propertyValue))
                .findFirst()
                .map(content -> new ContentInfo(content.getContentSize(), content.getEncoding(), content.getMimetypeStr()))
                .or(() -> {
                    log.error("Content metadata not found for node {}", alfrescoNode.getId());

                    return empty();
                });

    }

    private Optional<ZonedDateTime> getDateValue(Serializable propertyValue)
    {
        try
        {
            return of(ZonedDateTime.parse((String) propertyValue, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        }
        catch (Exception e)
        {
            log.error("Cannot get epoch value from property {} with value {} for node {}", propertyName, propertyValue, alfrescoNode.getId());

            return empty();
        }

    }

    private Optional<Serializable> getPersistedValue(PropertyValue propertyValue)
    {
        return switch (PropertyValueType.getPropertyValueType(propertyValue.getPersistedType()))
        {
        case NULL -> empty();
        case BOOLEAN -> of(propertyValue.getBooleanValue());
        case LONG -> of(propertyValue.getLongValue());
        case FLOAT -> of(propertyValue.getFloatValue());
        case DOUBLE -> of(propertyValue.getDoubleValue());
        case STRING -> of(propertyValue.getStringValue());
        case SERIALIZABLE -> deserializeObject(propertyValue);
        default ->
        {
            log.error("Property {} type not recognized. Cannot extract value {}. Node: {}", propertyName, propertyValue, alfrescoNode.getId());

            yield empty();
        }
        };
    }

    private Optional<Serializable> deserializeObject(PropertyValue propertyValue)
    {
        try
        {
            ObjectInput in = new ObjectInputStream(new ByteArrayInputStream(propertyValue.getSerializableValue()));

            return of((Serializable) in.readObject());
        }
        catch (ClassNotFoundException | IOException e)
        {
            log.error(format("Cannot deserialize property %s value %s. Node: %s", propertyName, propertyValue, alfrescoNode.getId()), e);

            return empty();
        }
    }
}
