package org.alfresco.hxi_connector.live_ingester.messaging.in.mapper.property.resolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.live_ingester.domain.exception.ValidationException;

class CategoryPropertyResolverTest
{

    ObjectMapper objectMapper = new ObjectMapper();

    CategoryPropertyResolver categoryPropertyResolver = new CategoryPropertyResolver();

    @Test
    void shouldBeAbleToResolveCategoriesProperty()
    {
        assertTrue(categoryPropertyResolver.canResolve(Map.entry("cm:categories", "")));
    }

    @Test
    void shouldBeAbleToResolveTaggableProperty()
    {
        assertTrue(categoryPropertyResolver.canResolve(Map.entry("cm:taggable", "")));
    }

    @Test
    void shouldNotBeAbleToResolveOtherProperties()
    {
        assertFalse(categoryPropertyResolver.canResolve(Map.entry("cm:other", "")));
    }

    @Test
    void shouldThrowIfTryingToResolveUnsupportedProperty()
    {
        assertThrows(ValidationException.class, () -> categoryPropertyResolver.resolve(Map.entry("cm:other", "")));
    }

    @Test
    void shouldResolvePropertyToIds()
    {
        // given
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
        Map.Entry<String, ?> givenProperty = asEntry(propertyDefinition);

        // when
        Map.Entry<String, Set<String>> resolvedProperty = categoryPropertyResolver.resolve(givenProperty);

        // then
        Map.Entry<String, Set<String>> expectedProperty = Map.entry("cm:taggable", Set.of("51d0b636-3c3b-4e33-ba1f-098474f53e8c", "a9f57ef6-2acf-4b2a-ae85-82cf552bec58"));

        assertEquals(expectedProperty, resolvedProperty);
    }

    private Map.Entry<String, ?> asEntry(String json)
    {
        return toMap(json).entrySet().stream().findFirst().get();
    }

    @SneakyThrows
    private Map<String, Object> toMap(String json)
    {
        return objectMapper.readValue(json, new TypeReference<>() {});
    }
}
