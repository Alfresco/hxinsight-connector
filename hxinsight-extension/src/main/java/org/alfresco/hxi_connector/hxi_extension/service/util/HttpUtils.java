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

package org.alfresco.hxi_connector.hxi_extension.service.util;

import static org.apache.http.HttpStatus.SC_OK;

import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensureThat;

import java.net.http.HttpResponse;

import org.springframework.extensions.webscripts.WebScriptException;

public final class HttpUtils
{
    public static void ensureCorrectHttpStatusReturned(int expectedStatus, HttpResponse<?> httpResponse)
    {
        ensureThat(httpResponse.statusCode() == SC_OK,
                () -> new WebScriptException(httpResponse.statusCode(), "Request to hxi failed, expected status %s, received %s".formatted(expectedStatus, httpResponse.statusCode())));
    }
}
