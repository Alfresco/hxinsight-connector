/*
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
package org.alfresco.hxi_connector.common.util;

import static lombok.AccessLevel.PRIVATE;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import lombok.NoArgsConstructor;

import org.alfresco.hxi_connector.common.exception.EndpointClientErrorException;
import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;
import org.alfresco.hxi_connector.common.exception.HxInsightConnectorRuntimeException;
import org.alfresco.hxi_connector.common.exception.ResourceNotFoundException;

@NoArgsConstructor(access = PRIVATE)
public class ErrorUtils
{
    public static final String UNEXPECTED_STATUS_CODE_MESSAGE = "Unexpected response status code - expecting: %d, received: %d";

    public static void throwExceptionOnUnexpectedStatusCode(int actualStatusCode, int expectedStatusCode)
    {
        if (actualStatusCode == 404)
        {
            throw new ResourceNotFoundException(UNEXPECTED_STATUS_CODE_MESSAGE.formatted(expectedStatusCode, actualStatusCode));
        }
        else if (actualStatusCode >= 400 && actualStatusCode <= 499)
        {
            throw new EndpointClientErrorException(UNEXPECTED_STATUS_CODE_MESSAGE.formatted(expectedStatusCode, actualStatusCode));
        }
        else if (actualStatusCode >= 500 && actualStatusCode <= 599)
        {
            throw new EndpointServerErrorException(UNEXPECTED_STATUS_CODE_MESSAGE.formatted(expectedStatusCode, actualStatusCode));
        }
    }

    public static RuntimeException wrapErrorIfNecessary(Exception cause, Set<Class<? extends Throwable>> retryReasons)
    {
        return wrapError(cause, retryReasons, HxInsightConnectorRuntimeException.class);
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    public static void wrapErrorAndThrowIfNecessary(Exception cause, Set<Class<? extends Throwable>> retryReasons, Class<? extends RuntimeException> runtimeExceptionType)
    {
        throw wrapError(cause, retryReasons, runtimeExceptionType);
    }

    private static RuntimeException wrapError(Exception cause, Set<Class<? extends Throwable>> retryReasons, Class<? extends RuntimeException> runtimeExceptionType)
    {
        if (cause instanceof EndpointServerErrorException)
        {
            return (EndpointServerErrorException) cause;
        }
        else if (retryReasons.contains(cause.getClass()))
        {
            return new EndpointServerErrorException(cause);
        }
        else if (cause instanceof EndpointClientErrorException)
        {
            return (EndpointClientErrorException) cause;
        }
        else if (runtimeExceptionType.isAssignableFrom(cause.getClass()))
        {
            return runtimeExceptionType.cast(cause);
        }
        else
        {
            try
            {
                throw runtimeExceptionType.getDeclaredConstructor(Throwable.class).newInstance(cause);
            }
            catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e)
            {
                return new HxInsightConnectorRuntimeException("Cannot create new instance of exception: %s due to: %s while processing another exception:"
                        .formatted(runtimeExceptionType.getSimpleName(), e.getMessage()), cause);
            }
            catch (RuntimeException e)
            {
                return e;
            }
        }
    }
}
