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

package org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model;

import static org.alfresco.hxi_connector.live_ingester.domain.utils.EnsureUtils.ensureNonNull;

import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeMetadataEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.property.custom.CustomPropertyDeleted;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.property.custom.CustomPropertyUpdated;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.property.CustomPropertyResolver;

@Getter
@ToString
@EqualsAndHashCode
public abstract class CustomPropertyDelta<T>
{
    private final String propertyName;

    public static <T> CustomPropertyUpdated<T> updated(String key, T propertyValue)
    {
        return new CustomPropertyUpdated<>(key, propertyValue);
    }

    public static <T> CustomPropertyDeleted<T> deleted(String key)
    {
        return new CustomPropertyDeleted<>(key);
    }

    protected CustomPropertyDelta(String propertyName)
    {
        ensureNonNull(propertyName, "Property key cannot be null");

        this.propertyName = propertyName;
    }

    public boolean canBeResolvedWith(CustomPropertyResolver<?> resolver)
    {
        return resolver.canResolve(this);
    }

    public abstract void applyOn(UpdateNodeMetadataEvent event);

    public abstract <R> Optional<CustomPropertyDelta<R>> resolveWith(CustomPropertyResolver<R> resolver);
}
