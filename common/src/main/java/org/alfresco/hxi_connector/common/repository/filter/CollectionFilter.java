/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2024 Alfresco Software Limited
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
package org.alfresco.hxi_connector.common.repository.filter;

import java.util.Collection;
import jakarta.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class CollectionFilter
{

    public static boolean filter(Collection<String> fields, Collection<String> allowed, Collection<String> denied)
    {
        final boolean allow = isAllowed(fields, allowed);
        final boolean deny = isDenied(fields, denied);
        boolean result = allow && !deny;
        log.atDebug().log("Filtering :: Node fields collection: {}. Allowed values: {}. Denied values: {}. Is allowed: {}", fields, allowed, denied, result);
        return result;
    }

    private static boolean isAllowed(@NotNull Collection<String> fields, @NotNull Collection<String> allowed)
    {
        return allowed.isEmpty() || allowed.stream().anyMatch(fields::contains);
    }

    private static boolean isDenied(@NotNull Collection<String> fields, @NotNull Collection<String> denied)
    {
        return denied.stream().anyMatch(fields::contains);
    }
}
