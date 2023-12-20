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

import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyState.DELETED;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyState.UPDATED;
import static org.alfresco.hxi_connector.live_ingester.domain.utils.EnsureUtils.ensureNonNull;

import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeMetadataEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.property.CustomPropertyResolver;

@ToString
@EqualsAndHashCode
public class CustomPropertyDelta<T>
{
    private final PropertyState propertyState;
    private final String propertyName;
    private final T propertyValue;

    public static <T> CustomPropertyDelta<T> updated(String key, T propertyValue)
    {
        ensureNonNull(propertyValue, "Property value cannot be null. Property name: %s", key);

        return new CustomPropertyDelta<>(UPDATED, key, propertyValue);
    }

    public static <T> CustomPropertyDelta<T> deleted(String key)
    {
        return new CustomPropertyDelta<>(DELETED, key, null);
    }

    private CustomPropertyDelta(PropertyState propertyState, String propertyName, T propertyValue)
    {
        ensureNonNull(propertyName, "Property key cannot be null");
        ensureNonNull(propertyState, "Property state cannot be null");

        this.propertyState = propertyState;
        this.propertyValue = propertyValue;
        this.propertyName = propertyName;
    }

    public void applyOn(UpdateNodeMetadataEvent event)
    {
        if (propertyState == UPDATED)
        {
            event.set(new NodeProperty<>(propertyName, propertyValue));
        }
        else if (propertyState == DELETED)
        {
            event.unset(propertyName);
        }
    }

    public boolean canBeResolvedWith(CustomPropertyResolver<?> resolver)
    {
        return resolver.canResolve(propertyName);
    }

    public <R> Optional<CustomPropertyDelta<R>> resolveWith(CustomPropertyResolver<R> resolver)
    {
        if (propertyState == UPDATED)
        {
            return resolver.resolveUpdated(propertyName, propertyValue);
        }
        else if (propertyState == DELETED)
        {
            return resolver.resolveDeleted(propertyName);
        }

        return Optional.empty();
    }
}
