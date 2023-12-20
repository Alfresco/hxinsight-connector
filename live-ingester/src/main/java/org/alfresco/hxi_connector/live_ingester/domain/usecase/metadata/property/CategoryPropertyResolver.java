package org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.property;

import static org.alfresco.hxi_connector.live_ingester.domain.utils.EnsureUtils.ensureThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.CustomPropertyDelta;

@Component
public class CategoryPropertyResolver implements CustomPropertyResolver<Set<String>>
{
    private static final String CATEGORIES_PROPERTY_NAME = "cm:categories";
    private static final String TAGS_PROPERTY_NAME = "cm:taggable";

    @Override
    public boolean canResolve(String propertyName)
    {
        return propertyName.equals(CATEGORIES_PROPERTY_NAME) || propertyName.equals(TAGS_PROPERTY_NAME);
    }

    @Override
    public Optional<CustomPropertyDelta<Set<String>>> resolveUpdated(String propertyName, Object propertyValue)
    {
        ensureThat(canResolve(propertyName), "Unsupported property. name: %s, value: %s", propertyName, propertyValue);

        Set<String> ids = ((List<Map<String, Object>>) propertyValue)
                .stream()
                .map(this::getId)
                .collect(Collectors.toSet());

        return Optional.of(CustomPropertyDelta.updated(propertyName, ids));
    }

    private String getId(Map<String, Object> entry)
    {
        return (String) entry.get("id");
    }

    @Override
    public Optional<CustomPropertyDelta<Set<String>>> resolveDeleted(String propertyName)
    {
        ensureThat(canResolve(propertyName), "Unsupported property. name: %s", propertyName);

        return Optional.of(CustomPropertyDelta.deleted(propertyName));
    }
}
