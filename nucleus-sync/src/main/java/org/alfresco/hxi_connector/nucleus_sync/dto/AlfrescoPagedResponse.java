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
package org.alfresco.hxi_connector.nucleus_sync.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AlfrescoPagedResponse<T>
{
    private ListWrapper<T> list;

    public ListWrapper<T> getList()
    {
        return list;
    }

    public void setList(ListWrapper<T> list)
    {
        this.list = list;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ListWrapper<T>(List<EntryWrapper<T>> entries, Pagination pagination)
    {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EntryWrapper<T>(T entry)
    {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pagination(int count, boolean hasMoreItems, int totalItems, int skipCount, int maxItems)
    {}
}
