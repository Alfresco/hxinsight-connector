/*
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
package org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model;

import static java.lang.String.format;

import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;

/**
 * Raised when a transform-response message reports a deterministic transform failure (currently {@code status == 400}, ATS' "I cannot produce this rendition, ever" signal). The default-deployment path swallows this state silently after a route-level WARN log; deployments that opt into {@code alfresco.transform.response.throw-failed-transforms=true} surface the abandonment as this exception so the route's error handler (and, when also opted in via {@code dead-letter-enabled}, the {@code DeadLetterChannel}) can produce a structured operator signal — DLQ entry, exception-tagged Micrometer counter, masked exchange-state log line.
 */
public class FailedTransformResponseException extends LiveIngesterRuntimeException
{
    public FailedTransformResponseException(String nodeRef, int status, String errorDetails)
    {
        super(format("Transform-response reported failure for node %s: status=%d, errorDetails=%s",
                nodeRef, status, errorDetails));
    }
}
