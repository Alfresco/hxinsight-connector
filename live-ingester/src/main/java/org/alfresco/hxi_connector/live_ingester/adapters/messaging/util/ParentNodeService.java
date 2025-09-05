/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParentNodeService {

    private final RestTemplate restTemplate;
    private final AuthService authService;

    @Value("${alfresco.repository.baseurl}")
    private String alfrescoBaseUrl;

    public List<String> getParentNodeId(String nodeId) {
        List<String> parentPath = new ArrayList<>();
        String currentNodeId = nodeId;

        try {
            while (currentNodeId != null) {
                String url = alfrescoBaseUrl + "/api/-default-/public/alfresco/versions/1/nodes/" + currentNodeId + "/parents";

                HttpHeaders headers = createAuthHeaders();
                HttpEntity<String> entity = new HttpEntity<>(headers);

                ResponseEntity<ParentsResponse> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        ParentsResponse.class
                );

                if (response.getBody() != null && response.getBody().getList() != null) {
                    List<ParentEntry> entries = response.getBody().getList().getEntries();

                    if (!entries.isEmpty()) {
                        ParentEntry parentEntry = entries.get(0);
                        String parentId = parentEntry.getEntry().getId();
                        String grandParentId = parentEntry.getEntry().getParentId();

                        parentPath.add(parentId); // Add to beginning to maintain root-to-current order
                        if (grandParentId != null && !parentPath.contains(grandParentId)) {
                            parentPath.add(grandParentId); // Add grandparent after parent
                        }
                        currentNodeId = grandParentId; // Continue with grandparent
                    } else {
                        break; // No more parents
                    }
                } else {
                    break;
                }
            }
            Collections.reverse(parentPath);
            return parentPath;
        } catch (RestClientException e) {
            log.warn("Failed to get parent nodes for nodeId: {}. Error: {}", nodeId, e.getMessage());
            return Collections.emptyList();
        }
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> authHeaders = authService.getAlfrescoAuthHeaders();
        authHeaders.forEach(headers::set);
        return headers;
    }

    // Response DTOs for Alfresco REST API
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParentsResponse {
        @JsonProperty("list")
        private ParentsList list;

        public ParentsList getList() {
            return list;
        }

        public void setList(ParentsList list) {
            this.list = list;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParentsList {
        @JsonProperty("entries")
        private List<ParentEntry> entries;

        public List<ParentEntry> getEntries() {
            return entries != null ? entries : Collections.emptyList();
        }

        public void setEntries(List<ParentEntry> entries) {
            this.entries = entries;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParentEntry {
        @JsonProperty("entry")
        private NodeInfo entry;

        public NodeInfo getEntry() {
            return entry;
        }

        public void setEntry(NodeInfo entry) {
            this.entry = entry;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NodeInfo {
        @JsonProperty("id")
        private String id;

        @JsonProperty("parentId")
        private String parentId;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }
    }
}
