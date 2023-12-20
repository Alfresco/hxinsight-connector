package org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(categoryPropertyResolver.canResolve("cm:categories"));
    }

    @Test
    void shouldBeAbleToResolveTaggableProperty()
    {
        assertTrue(categoryPropertyResolver.canResolve("cm:taggable"));
    }

    @Test
    void shouldNotBeAbleToResolveOtherProperties()
    {
        assertFalse(categoryPropertyResolver.canResolve("cm:other"));
    }

    @Test
    void shouldThrowIfTryingToResolveUpdatedUnsupportedProperty()
    {
        assertThrows(ValidationException.class, () -> categoryPropertyResolver.resolveUpdated("cm:other", ""));
    }

    @Test
    void shouldThrowIfTryingToResolveDeletedUnsupportedProperty()
    {
        assertThrows(ValidationException.class, () -> categoryPropertyResolver.resolveDeleted("cm:other"));
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
        CustomPropertyDelta<Set<String>> resolvedProperty = categoryPropertyResolver.resolveUpdated(taggable, taggablePropertyValue).get();

        // then
        CustomPropertyDelta<Set<String>> expectedProperty = CustomPropertyDelta.updated("cm:taggable", Set.of("51d0b636-3c3b-4e33-ba1f-098474f53e8c", "a9f57ef6-2acf-4b2a-ae85-82cf552bec58"));

        assertEquals(expectedProperty, resolvedProperty);
    }

    @Test
    void shouldDoNothingWithDeletedProperty()
    {
        // given
        String taggable = "cm:taggable";

        // when
        var resolvedProperty = categoryPropertyResolver.resolveDeleted(taggable);

        // then
        CustomPropertyDelta<?> expectedProperty = CustomPropertyDelta.deleted(taggable);

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
