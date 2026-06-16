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
package org.alfresco.hxi_connector.nucleus_sync.client;

/**
 * Thrown when an HTTP call receives a 5xx server error. This exception is retryable — transient server-side failures (503, 502, etc.) are worth retrying after a backoff. In contrast, 4xx client errors indicate misconfiguration or bad input and should NOT be retried.
 */
public class RetryableServerException extends RuntimeException
{
    private final int statusCode;

    public RetryableServerException(int statusCode, String message)
    {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode()
    {
        return statusCode;
    }
}
