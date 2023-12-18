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

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeMetadataEvent;

import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyState.UPDATED;
import static org.alfresco.hxi_connector.live_ingester.domain.utils.EnsureUtils.ensureNonNull;

public class PropertyDelta<T> {

    private final PropertyState propertyState;
    private final T propertyValue;

    public static <T> PropertyDelta<T> updated(T propertyValue) {
        return new PropertyDelta<>(PropertyState.UPDATED, propertyValue);
    }

    public static <T> PropertyDelta<T> unchanged(T propertyValue) {
        return new PropertyDelta<>(PropertyState.UNCHANGED, propertyValue);
    }

    private PropertyDelta(PropertyState propertyState, T value) {
        ensureNonNull(propertyState, "Property state cannot be null");
        ensureNonNull(value, "Property key cannot be null");

        this.propertyState = propertyState;
        this.propertyValue = value;
    }

    public void applyAs(PredefinedNodeMetadataProperty<T> predefinedNodeMetadataProperty, UpdateNodeMetadataEvent event) {
        if (propertyState == UPDATED) {
            event.set(predefinedNodeMetadataProperty.withValue(propertyValue));
        }
    }
}
