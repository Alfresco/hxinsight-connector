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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta.deleted;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta.updated;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.domain.exception.ValidationException;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;

class CategoryPropertyResolverTest
{

    ObjectMapper objectMapper = new ObjectMapper();

    CategoryPropertyResolver categoryPropertyResolver = new CategoryPropertyResolver();

    @Test
    void shouldBeAbleToResolveCategoriesProperty()
    {
        assertTrue(categoryPropertyResolver.canResolve(deleted("cm:categories")));
    }

    @Test
    void shouldBeAbleToResolveTaggableProperty()
    {
        assertTrue(categoryPropertyResolver.canResolve(deleted("cm:taggable")));
    }

    @Test
    void shouldNotBeAbleToResolveOtherProperties()
    {
        assertFalse(categoryPropertyResolver.canResolve(deleted("cm:other")));
    }

    @Test
    void shouldThrowIfTryingToResolveUpdatedUnsupportedProperty()
    {
        assertThrows(ValidationException.class, () -> categoryPropertyResolver.resolveUpdated(updated("cm:other", "")));
    }

    @Test
    void shouldThrowIfTryingToResolveDeletedUnsupportedProperty()
    {
        assertThrows(ValidationException.class, () -> categoryPropertyResolver.resolveDeleted(deleted("cm:other")));
    }

    @Test
    void shouldResolveUpdatedPropertyToIds()
    {
        // given
        String taggable = "cm:taggable";
        String propertyDefinition = """
                {
                  "cm:taggable": [
                    {
                      "storeRef": {
                        "protocol": "workspace",
                        "identifier": "SpacesStore"
                      },
                      "id": "51d0b636-3c3b-4e33-ba1f-098474f53e8c"
                    },
                    {
                      "storeRef": {
                        "protocol": "workspace",
                        "identifier": "SpacesStore"
                      },
                      "id": "a9f57ef6-2acf-4b2a-ae85-82cf552bec58"
                    }
                  ]
                }
                """;
        List<Object> taggablePropertyValue = getTaggablePropertyValue(propertyDefinition);

        // when
        CustomPropertyDelta<Set<String>> resolvedProperty = categoryPropertyResolver.resolveUpdated(updated(taggable, taggablePropertyValue)).get();

        // then
        CustomPropertyDelta<Set<String>> expectedProperty = updated("cm:taggable", Set.of("51d0b636-3c3b-4e33-ba1f-098474f53e8c", "a9f57ef6-2acf-4b2a-ae85-82cf552bec58"));

        assertEquals(expectedProperty, resolvedProperty);
    }

    @Test
    void shouldDoNothingWithDeletedProperty()
    {
        // given
        String taggable = "cm:taggable";

        // when
        var resolvedProperty = categoryPropertyResolver.resolveDeleted(deleted(taggable));

        // then
        CustomPropertyDelta<?> expectedProperty = deleted(taggable);

        assertTrue(resolvedProperty.isPresent());
        assertEquals(expectedProperty, resolvedProperty.get());
    }

    private List<Object> getTaggablePropertyValue(String json)
    {
        return toMap(json).entrySet()
                .stream()
                .findFirst()
                .map(property -> (List<Object>) property.getValue())
                .get();
    }

    @SneakyThrows
    private Map<String, Object> toMap(String json)
    {
        return objectMapper.readValue(json, new TypeReference<>() {});
    }
}
