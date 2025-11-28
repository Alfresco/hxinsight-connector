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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.extensions.webscripts.WebScriptException;

class HttpUtilsTest
{
    @Test
    void shouldThrowIfStatusCodeDoesNotMatchExpected()
    {
        // given
        int expectedStatus = 200;
        int actualStatus = 500;

        HttpResponse<?> httpResponse = mock(HttpResponse.class);
        given(httpResponse.statusCode()).willReturn(actualStatus);

        // when
        WebScriptException exception = assertThrows(WebScriptException.class, () -> HttpUtils.ensureCorrectHttpStatusReturned(expectedStatus, httpResponse));

        // then
        assertEquals(actualStatus, exception.getStatus());
        assertTrue(exception.getMessage().contains("Request to hxi failed, expected status 200, received 500"));
    }

    @Test
    @SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
    void shouldDoNothingIfStatusCodeMatchesExpected()
    {
        // given
        int expectedStatus = 201;
        int actualStatus = 201;

        HttpResponse<?> httpResponse = mock(HttpResponse.class);
        given(httpResponse.statusCode()).willReturn(actualStatus);

        // when, then
        HttpUtils.ensureCorrectHttpStatusReturned(expectedStatus, httpResponse);
    }
}
