package org.alfresco.hxi_connector.live_ingester.util.insight_api;

import java.util.Map;

public record HxInsightRequest(String url, Map<String, String> headers, String body) {
}
