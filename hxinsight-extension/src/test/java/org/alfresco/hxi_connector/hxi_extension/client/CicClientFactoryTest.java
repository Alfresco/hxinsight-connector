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
package org.alfresco.hxi_connector.hxi_extension.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.hyland.sdk.cic.agent.AgentService;
import org.hyland.sdk.cic.http.client.auth.AuthenticationHttpClient;
import org.hyland.sdk.cic.qna.QnaService;
import org.junit.jupiter.api.Test;

class CicClientFactoryTest
{
    private static final int MAX_ATTEMPTS = 3;
    private static final long INITIAL_DELAY_MS = 500;
    private static final double MULTIPLIER = 2.0;
    private static final long MAX_DELAY_MS = 30_000;

    @Test
    void parseScopesShouldReturnEmptySetForNull()
    {
        assertTrue(CicClientFactory.parseScopes(null).isEmpty());
    }

    @Test
    void parseScopesShouldReturnEmptySetForBlank()
    {
        assertTrue(CicClientFactory.parseScopes("   ").isEmpty());
    }

    @Test
    void parseScopesShouldReturnSingleScope()
    {
        assertEquals(Set.of("read"), CicClientFactory.parseScopes("read"));
    }

    @Test
    void parseScopesShouldSplitOnWhitespace()
    {
        assertEquals(Set.of("read", "write", "admin"), CicClientFactory.parseScopes("read write admin"));
    }

    @Test
    void parseScopesShouldHandleLeadingAndTrailingWhitespace()
    {
        assertEquals(Set.of("read", "write"), CicClientFactory.parseScopes("  read  write  "));
    }

    @Test
    void shouldBuildAuthBuilder()
    {
        AuthenticationHttpClient.Builder builder = buildAuth("read");

        assertNotNull(builder);
    }

    @Test
    void shouldCreateAgentService()
    {
        AuthenticationHttpClient.Builder authBuilder = buildAuth(null);

        AgentService service = CicClientFactory.createAgentService(authBuilder, "env-key", "http://localhost");

        assertNotNull(service);
    }

    @Test
    void shouldCreateQnaService()
    {
        AuthenticationHttpClient.Builder authBuilder = buildAuth(null);

        QnaService service = CicClientFactory.createQnaService(authBuilder, "env-key", "http://localhost");

        assertNotNull(service);
    }

    private static AuthenticationHttpClient.Builder buildAuth(String scope)
    {
        return CicClientFactory.buildAuth(
                "http://localhost", "id", "secret", scope,
                MAX_ATTEMPTS, INITIAL_DELAY_MS, MULTIPLIER, MAX_DELAY_MS);
    }
}
