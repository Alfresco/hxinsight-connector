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

package org.alfresco.hxi_connector.live_ingester.domain.utils;

import static lombok.AccessLevel.PRIVATE;

import java.util.Objects;
import java.util.function.Supplier;

import lombok.NoArgsConstructor;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;
import org.alfresco.hxi_connector.live_ingester.domain.exception.ValidationException;

@NoArgsConstructor(access = PRIVATE)
public class EnsureUtils
{
    public static void ensureNotBlank(String s, String errorMessage, String... formatArgs)
    {
        ensureThat(Objects.nonNull(s) & !s.isBlank(), () -> new ValidationException(String.format(errorMessage, formatArgs)));
    }

    public static void ensureNonNull(Object o, String errorMessage, String... formatArgs)
    {
        ensureThat(Objects.nonNull(o), () -> new ValidationException(String.format(errorMessage, formatArgs)));
    }

    public static void ensureThat(boolean isOk, Supplier<? extends LiveIngesterRuntimeException> exceptionSupplier)
    {
        if (!isOk)
        {
            throw exceptionSupplier.get();
        }
    }
}
