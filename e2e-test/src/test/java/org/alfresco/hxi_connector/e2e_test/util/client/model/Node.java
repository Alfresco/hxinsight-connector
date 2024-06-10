package org.alfresco.hxi_connector.e2e_test.util.client.model;

import java.util.Map;
import java.util.Set;
import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Node(
    @NotBlank String id,
    String name,
    String nodeType,
    boolean isFolder,
    boolean isFile,
    String parentId,
    Map<String, Object> properties,
    @JsonProperty("aspectNames") Set<String> aspects,
    String createdAt,
    Map<String, String> createdByUser,
    String modifiedAt,
    Map<String, String> modifiedByUser)
{}
