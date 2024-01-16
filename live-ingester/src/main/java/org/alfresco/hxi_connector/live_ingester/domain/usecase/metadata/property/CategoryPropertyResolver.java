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

package org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.property;

import static org.alfresco.hxi_connector.live_ingester.domain.utils.EnsureUtils.ensureThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.property.custom.CustomPropertyDeleted;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.property.custom.CustomPropertyUpdated;

@Component
public class CategoryPropertyResolver implements CustomPropertyResolver<Set<String>>
{
    private static final String CATEGORIES_PROPERTY_NAME = "cm:categories";
    private static final String TAGS_PROPERTY_NAME = "cm:taggable";

    @Override
    public boolean canResolve(CustomPropertyDelta<?> customPropertyDelta)
    {
        String propertyName = customPropertyDelta.getPropertyName();

        return propertyName.equals(CATEGORIES_PROPERTY_NAME) || propertyName.equals(TAGS_PROPERTY_NAME);
    }

    @Override
    public Optional<CustomPropertyDelta<Set<String>>> resolveUpdated(CustomPropertyUpdated<?> updatedProperty)
    {
        ensureThat(canResolve(updatedProperty), "Unsupported property: %s", updatedProperty);

        Set<String> ids = ((List<Map<String, Object>>) updatedProperty.getPropertyValue())
                .stream()
                .map(this::getId)
                .collect(Collectors.toSet());

        return Optional.of(CustomPropertyDelta.updated(updatedProperty.getPropertyName(), ids));
    }

    private String getId(Map<String, Object> entry)
    {
        return (String) entry.get("id");
    }

    @Override
    public Optional<CustomPropertyDelta<Set<String>>> resolveDeleted(CustomPropertyDeleted<?> deletedProperty)
    {
        ensureThat(canResolve(deletedProperty), "Unsupported property: %s", deletedProperty);

        return Optional.of((CustomPropertyDelta<Set<String>>) deletedProperty);
    }
}
