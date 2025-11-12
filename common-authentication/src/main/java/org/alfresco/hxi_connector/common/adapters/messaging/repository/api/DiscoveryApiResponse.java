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
package org.alfresco.hxi_connector.common.adapters.messaging.repository.api;

import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscoveryApiResponse
{
    @NotBlank private RepositoryInfoEntry entry;

    public String getFullVersion()
    {
        return entry.getRepository().getVersion().getMajor() + "." +
                entry.getRepository().getVersion().getMinor() + "." + entry.getRepository().getVersion().getPatch();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RepositoryInfoEntry
    {
        @NotBlank private RepositoryInfo repository;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RepositoryInfo
    {
        @NotBlank private String id;
        @NotBlank private RepositoryVersion version;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RepositoryVersion
    {
        @NotBlank private String major;
        @NotBlank private String minor;
        @NotBlank private String patch;

    }

}
