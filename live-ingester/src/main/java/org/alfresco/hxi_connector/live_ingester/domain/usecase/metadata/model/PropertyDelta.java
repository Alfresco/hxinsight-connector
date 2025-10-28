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

package org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model;

import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensureNonNull;

import java.util.List;
import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.property.AncestorsPropertyUpdated;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.property.ContentPropertyUpdated;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.property.PropertyUpdated;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.property.PropertyResolver;

@Getter
@ToString
@EqualsAndHashCode
public abstract class PropertyDelta<T>
{
    private final String propertyName;

    public static <T> PropertyUpdated<T> updated(String key, T propertyValue)
    {
        return new PropertyUpdated<>(key, propertyValue);
    }

    public static ContentPropertyUpdated contentPropertyUpdated(String key, String id, String mimeType)
    {
        return ContentPropertyUpdated.builder(key).id(id).mimeType(mimeType).build();
    }

    public static ContentPropertyUpdated contentMetadataUpdated(String key, String sourceMimeType, Long sourceSizeInBytes, String sourceFileName)
    {
        return ContentPropertyUpdated.builder(key)
                .sourceMimeType(sourceMimeType)
                .sourceSizeInBytes(sourceSizeInBytes)
                .sourceFileName(sourceFileName).build();
    }

    public static AncestorsPropertyUpdated ancestorsMetadataUpdated(String key, String parentId, List<String> ancestorIds)
    {
        return AncestorsPropertyUpdated.builder(key)
                .parentId(parentId)
                .ancestorIds(ancestorIds)
                .build();
    }

    protected PropertyDelta(String propertyName)
    {
        ensureNonNull(propertyName, "Property key cannot be null");

        this.propertyName = propertyName;
    }

    public boolean canBeResolvedWith(PropertyResolver<?> resolver)
    {
        return resolver.canResolve(this);
    }

    public abstract void applyOn(UpdateNodeEvent event);

    public abstract <R> Optional<PropertyDelta<R>> resolveWith(PropertyResolver<R> resolver);
}
