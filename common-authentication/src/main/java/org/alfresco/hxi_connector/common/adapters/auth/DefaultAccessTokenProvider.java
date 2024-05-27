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
package org.alfresco.hxi_connector.common.adapters.auth;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import org.apache.camel.CamelContext;

import org.alfresco.hxi_connector.common.exception.HxInsightConnectorRuntimeException;

@RequiredArgsConstructor
public class DefaultAccessTokenProvider implements AccessTokenProvider
{

    private final CamelContext camelContext;
    private final AuthenticationClient authenticationClient;

    private final Map<String, Map.Entry<AuthenticationResult, OffsetDateTime>> accessTokens = new HashMap<>();

    @Override
    public String getAccessToken(String clientRegistrationId)
    {
        waitFor(camelContext::isStarted);
        Map.Entry<AuthenticationResult, OffsetDateTime> authenticationResultEntry = accessTokens.get(clientRegistrationId);
        if (authenticationResultEntry == null || authenticationResultEntry.getValue().isBefore(OffsetDateTime.now()))
        {
            refreshAuthenticationResult(clientRegistrationId);
            authenticationResultEntry = accessTokens.get(clientRegistrationId);
        }
        Objects.requireNonNull(authenticationResultEntry, "Authentication result is null");
        AuthenticationResult authenticationResult = authenticationResultEntry.getKey();
        return authenticationResult.accessToken();
    }

    private void refreshAuthenticationResult(String clientRegistrationId)
    {
        AuthenticationResult authenticationResult = authenticationClient.authenticate(clientRegistrationId);
        accessTokens.put(clientRegistrationId, Map.entry(authenticationResult,
                OffsetDateTime.now().plus(authenticationResult.expiresIn(), authenticationResult.temporalUnit())));
    }

    private static void waitFor(Supplier<Boolean> supplier)
    {
        while (!supplier.get())
        {
            try
            {
                TimeUnit.MILLISECONDS.sleep(100);
            }
            catch (InterruptedException e)
            {
                throw new HxInsightConnectorRuntimeException(e);
            }
        }
    }
}
