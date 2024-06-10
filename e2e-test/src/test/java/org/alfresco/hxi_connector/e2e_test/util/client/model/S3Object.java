package org.alfresco.hxi_connector.e2e_test.util.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record S3Object(
    @JsonProperty("Key")
    String id,
    @JsonProperty("Size")
    Long size)
{}
