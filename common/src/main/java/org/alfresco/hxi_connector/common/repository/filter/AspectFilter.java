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

import java.util.List;
import java.util.Set;
import jakarta.validation.constraints.NotNull;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class AspectFilter
{

    public static boolean filter(Set<String> aspectNames, List<String> allowed, List<String> denied)
    {
        final boolean allow = isAllowed(aspectNames, allowed);
        final boolean deny = isDenied(aspectNames, denied);
        boolean result = allow && !deny;
        log.atDebug().log("Node aspects: {}. Allowed aspects: {}. Denied aspects: {}. Is allowed: {}", aspectNames, allowed, denied, result);
        return result;
    }

    private static boolean isAllowed(@NotNull Set<String> aspectNames, @NotNull List<String> allowed)
    {
        return allowed.isEmpty() || allowed.stream().anyMatch(aspectNames::contains);
    }

    private static boolean isDenied(@NotNull Set<String> aspectNames, @NotNull List<String> denied)
    {
        return denied.stream().anyMatch(aspectNames::contains);
    }
}
