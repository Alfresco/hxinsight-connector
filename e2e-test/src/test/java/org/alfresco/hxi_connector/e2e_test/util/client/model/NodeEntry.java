package org.alfresco.hxi_connector.e2e_test.util.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NodeEntry(@JsonProperty("entry") Node node)
{}
