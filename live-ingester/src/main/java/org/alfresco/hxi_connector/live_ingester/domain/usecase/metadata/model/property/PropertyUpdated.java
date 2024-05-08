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

package org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.property;

import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensureNonNull;

import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.property.PropertyResolver;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PropertyUpdated<T> extends PropertyDelta<T>
{
    private final T propertyValue;

    public PropertyUpdated(String propertyName, T propertyValue)
    {
        super(propertyName);

        ensureNonNull(propertyValue, "Property value cannot be null. Property name: %s", propertyName);
        this.propertyValue = propertyValue;
    }

    @Override
    public void applyOn(UpdateNodeEvent event)
    {
        event.addMetadataInstruction(new NodeProperty<>(getPropertyName(), propertyValue));
    }

    @Override
    public <R> Optional<PropertyDelta<R>> resolveWith(PropertyResolver<R> resolver)
    {
        return resolver.resolveUpdated(this);
    }
}
