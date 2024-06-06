package org.alfresco.hxi_connector.e2e_test.util.client.model;

import java.util.Set;
import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Node(@NotBlank String id, @JsonProperty("aspectNames") Set<String> aspects)
{
    public Node(String id)
    {
        this(id, null);
    }
}
