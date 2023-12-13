/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 Alfresco Software Limited
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

package org.alfresco.hxi_connector.live_ingester.domain.model.in;

import static lombok.AccessLevel.PRIVATE;

import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import org.alfresco.hxi_connector.live_ingester.domain.model.out.NodeProperty;

public record UpdateNodeEvent(
        long time,
        FieldDelta<String> name,
        FieldDelta<String> primaryAssocQName,
        FieldDelta<String> nodeType,
        FieldDelta<String> modifiedByUserWithId,
        FieldDelta<Set<String>> aspectNames,
        FieldDelta<Boolean> isFile,
        FieldDelta<Boolean> isFolder,
        FieldDelta<Set<NodeProperty<?>>> properties)
{

    @Getter
    @Accessors(fluent = true)
    @RequiredArgsConstructor(access = PRIVATE)
    public static class FieldDelta<T>
    {
        private final boolean updated;
        private final T propertyValue;
        private final T propertyValueBefore;

        public static <T> FieldDelta<T> updated(T propertyValue, T propertyValueBefore)
        {
            return new FieldDelta<>(true, propertyValue, propertyValueBefore);
        }

        public static <T> FieldDelta<T> notUpdated(T propertyValue)
        {
            return new FieldDelta<>(false, propertyValue, propertyValue);
        }
    }
}
