package org.alfresco.hxi_connector.live_ingester.messaging.in.mapper.property.resolver;

import static org.alfresco.hxi_connector.live_ingester.domain.utils.EnsureUtils.ensureThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class CategoryPropertyResolver implements CustomPropertyResolver<Set<String>>
{
    private static final String CATEGORIES_PROPERTY_NAME = "cm:categories";
    private static final String TAGS_PROPERTY_NAME = "cm:taggable";

    @Override
    public boolean canResolve(Map.Entry<String, ?> property)
    {
        return property.getKey().equals(CATEGORIES_PROPERTY_NAME) || property.getKey().equals(TAGS_PROPERTY_NAME);
    }

    @Override
    public Map.Entry<String, Set<String>> resolve(Map.Entry<String, ?> property)
    {
        ensureThat(canResolve(property), "Unsupported property %s", property);

        List<Map<String, Object>> propertyValue = (List<Map<String, Object>>) property.getValue();

        Set<String> ids = propertyValue.stream()
                .map(this::resolve)
                .collect(Collectors.toSet());

        return Map.entry(property.getKey(), ids);
    }

    private String resolve(Map<String, Object> entry)
    {
        return (String) entry.get("id");
    }
}
