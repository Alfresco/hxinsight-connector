/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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
package org.alfresco.hxi_connector.nucleus_client.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NucleusSCIMResponse(
        List<String> schemas,
        int totalResults,
        int itemsPerPage,
        int startIndex,
        List<Resource> resources)
{
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Resource(
            String id,
            String externalId,
            Meta meta,
            List<String> schemas,
            String userName,
            Name name,
            String displayName,
            boolean active,
            List<Email> emails,
            List<Group> groups) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(
            String resourceType,
            OffsetDateTime created,
            OffsetDateTime lastModified,
            String location,
            String version) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Name(
            String givenName,
            String familyName) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Email(
            String value,
            boolean primary) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Group(
            String value,
            String display,
            @JsonProperty("$ref") String ref) {}
}
