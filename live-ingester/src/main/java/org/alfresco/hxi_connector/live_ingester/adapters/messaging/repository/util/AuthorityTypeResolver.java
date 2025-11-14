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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.common.adapters.auth.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorityTypeResolver {

    private final RestTemplate restTemplate;
    private final AuthService authService;
    @Value("${alfresco.repository.baseurl}")
    private String alfrescoBaseUrl;

    public enum AuthorityType {
        USER, GROUP, ANY
    }

    public Map<String, AuthorityType> resolveAuthorityTypes(Set<String> authorities) {
        Map<String, AuthorityType> authorityTypes = new HashMap<>();
        for (String authority : authorities) {
            authorityTypes.put(authority, resolveAuthorityType(authority));
        }
        return authorityTypes;
    }

    public AuthorityType resolveAuthorityType(String authorityId) {
        if (authorityId.startsWith("GROUP_")) {
            return AuthorityType.GROUP;
        }

        try {
            AuthorityType authorityType = isUser(authorityId) ? AuthorityType.USER :
                    isGroup(authorityId) ? AuthorityType.GROUP : AuthorityType.ANY;

            if (authorityType == AuthorityType.ANY) {
                log.debug("Authority type resolved to ANY for authorityId: {}", authorityId);
            }
            return authorityType;
        } catch (RestClientException e) {
            log.warn("Failed to get authority type for authorityId: {}. Error: {}", authorityId, e.getMessage());
            return AuthorityType.ANY;
        }
    }

    private boolean isUser(String authorityId) {
        return checkAuthority("/api/-default-/public/alfresco/versions/1/people/" + authorityId);
    }

    private boolean isGroup(String authorityId) {
        return checkAuthority("/api/-default-/public/alfresco/versions/1/groups/" + authorityId);
    }

    private boolean checkAuthority(String endpoint) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, String> authHeaders = authService.getAlfrescoAuthHeaders();
            authHeaders.forEach(headers::set);
            restTemplate.exchange(
                    alfrescoBaseUrl + endpoint,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class
            );
            return true;
        } catch (RestClientException e) {
            return false;
        }
    }
}
