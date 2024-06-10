package org.alfresco.hxi_connector.e2e_test.util.client.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public record S3Bucket(
    @JsonProperty("Name")
    String name,
    @JsonProperty("Contents")
    @JacksonXmlElementWrapper(useWrapping = false)
    List<S3Object> content)
{
    public S3Bucket
    {
        content = Objects.requireNonNullElse(content, Collections.emptyList());
    }
}
