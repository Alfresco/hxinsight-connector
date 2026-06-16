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
package org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Pure-function utility that builds JSON response bodies for WireMock stubs. Stateless — all methods are static.
 */
public final class StubResponseBuilder
{
    private StubResponseBuilder()
    {}

    /**
     * Build ACS /people paginated response JSON matching the Alfresco REST API format.
     */
    public static String buildAcsPeoplePageJson(int skipCount, int count, boolean hasMoreItems, int totalItems, int pageSize)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"list\":{")
                .append("\"pagination\":{")
                .append("\"count\":").append(count).append(",")
                .append("\"hasMoreItems\":").append(hasMoreItems).append(",")
                .append("\"totalItems\":").append(totalItems).append(",")
                .append("\"skipCount\":").append(skipCount).append(",")
                .append("\"maxItems\":").append(pageSize)
                .append("},")
                .append("\"entries\":[");

        for (int i = 0; i < count; i++)
        {
            int userId = skipCount + i;
            if (i > 0)
            {
                sb.append(",");
            }
            sb.append("{\"entry\":{")
                    .append("\"id\":\"user").append(userId).append("\",")
                    .append("\"email\":\"user").append(userId).append("@hyland.com\",")
                    .append("\"enabled\":true")
                    .append("}}");
        }

        sb.append("]}}");
        return sb.toString();
    }

    /**
     * Build ACS /people/{userId}/groups paginated response JSON for group entries.
     */
    public static String buildAcsGroupPageJson(int skipCount, int count, boolean hasMoreItems, int totalItems, int pageSize)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"list\":{")
                .append("\"pagination\":{")
                .append("\"count\":").append(count).append(",")
                .append("\"hasMoreItems\":").append(hasMoreItems).append(",")
                .append("\"totalItems\":").append(totalItems).append(",")
                .append("\"skipCount\":").append(skipCount).append(",")
                .append("\"maxItems\":").append(pageSize)
                .append("},")
                .append("\"entries\":[");

        for (int i = 0; i < count; i++)
        {
            int groupIdx = skipCount + i;
            if (i > 0)
            {
                sb.append(",");
            }
            sb.append("{\"entry\":{")
                    .append("\"id\":\"group").append(groupIdx).append("\"")
                    .append("}}");
        }

        sb.append("]}}");
        return sb.toString();
    }

    /**
     * Build a JSON array fragment of Nucleus IAM user objects for a given offset and count.
     */
    public static String buildNucleusUsersJson(int offset, int count)
    {
        return IntStream.range(offset, offset + count)
                .mapToObj(i -> String.format(
                        "{\"userName\":\"user%d\",\"userId\":\"iam-%d\",\"email\":\"user%d@hyland.com\"}", i, i, i))
                .collect(Collectors.joining(","));
    }
}
