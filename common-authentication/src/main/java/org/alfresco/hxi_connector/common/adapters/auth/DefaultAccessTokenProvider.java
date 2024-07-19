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

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.alfresco.hxi_connector.common.util.EnsureUtils;

@RequiredArgsConstructor
@Slf4j
public class DefaultAccessTokenProvider implements AccessTokenProvider
{
    static final int REFRESH_OFFSET_SECS = 60;
    private final AuthenticationClient authenticationClient;

    private final Map<String, Token> accessTokens = new HashMap<>();

    @Override
    public String getAccessToken(String providerId)
    {
        Token token = accessTokens.get(providerId);
        synchronized (this)
        {
            if (shouldRefreshToken(token))
            {
                refreshAuthenticationResult(providerId);
                token = accessTokens.get(providerId);
            }
        }
        EnsureUtils.ensureNonNull(token, "Authentication result is null");
        return token.getAccessToken();
    }

    private void refreshAuthenticationResult(String providerId)
    {
        log.atDebug().log("Refreshing authentication result for provider {}", providerId);
        AuthenticationResult authenticationResult = authenticationClient.authenticate(providerId);
        accessTokens.put(providerId, new Token(authenticationResult.accessToken(),
                OffsetDateTime.now().plus(authenticationResult.expiresIn(), authenticationResult.temporalUnit()).minusSeconds(REFRESH_OFFSET_SECS)));
    }

    private static boolean shouldRefreshToken(Token token)
    {
        return token == null || OffsetDateTime.now().isAfter(token.getRefreshAt());
    }

    @Data
    static final class Token
    {
        private final String accessToken;
        private final OffsetDateTime refreshAt;
    }
}
