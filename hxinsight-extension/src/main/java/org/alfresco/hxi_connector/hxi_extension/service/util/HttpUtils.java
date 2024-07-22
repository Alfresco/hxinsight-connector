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

import static lombok.AccessLevel.PRIVATE;

import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensureThat;

import java.net.http.HttpResponse;

import lombok.NoArgsConstructor;
import org.springframework.extensions.webscripts.WebScriptException;

@NoArgsConstructor(access = PRIVATE)
public final class HttpUtils
{
    public static void ensureCorrectHttpStatusReturned(int expectedStatus, HttpResponse<?> httpResponse)
    {
        ensureThat(httpResponse.statusCode() == expectedStatus,
                () -> new WebScriptException(httpResponse.statusCode(), String.format("Request to hxi failed, expected status %s, received %s", expectedStatus, httpResponse.statusCode())));
    }
}
