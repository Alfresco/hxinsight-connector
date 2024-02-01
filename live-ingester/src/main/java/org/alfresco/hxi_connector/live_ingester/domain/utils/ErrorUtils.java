/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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

import java.util.Set;

import org.alfresco.hxi_connector.live_ingester.domain.exception.EndpointClientErrorException;
import org.alfresco.hxi_connector.live_ingester.domain.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;

public class ErrorUtils
{
    public static final String UNEXPECTED_STATUS_CODE_MESSAGE = "Unexpected response status code - expecting: %d, received: %d";

    public static void throwExceptionOnUnexpectedStatusCode(int actualStatusCode, int expectedStatusCode)
    {
        if (actualStatusCode >= 400 && actualStatusCode <= 499)
        {
            throw new EndpointClientErrorException(UNEXPECTED_STATUS_CODE_MESSAGE.formatted(expectedStatusCode, actualStatusCode));
        }
        else if (actualStatusCode >= 500 && actualStatusCode <= 599)
        {
            throw new EndpointServerErrorException(UNEXPECTED_STATUS_CODE_MESSAGE.formatted(expectedStatusCode, actualStatusCode));
        }
    }

    public static void wrapErrorIfNecessary(Exception cause, Set<Class<? extends Throwable>> retryReasons)
    {
        if (cause instanceof EndpointServerErrorException)
        {
            throw (EndpointServerErrorException) cause;
        }
        else if (retryReasons.contains(cause.getClass()))
        {
            throw new EndpointServerErrorException(cause);
        }
        else if (cause instanceof EndpointClientErrorException)
        {
            throw (EndpointClientErrorException) cause;
        }
        else if (cause instanceof LiveIngesterRuntimeException)
        {
            throw (LiveIngesterRuntimeException) cause;
        }
        else
        {
            throw new LiveIngesterRuntimeException(cause);
        }
    }
}
