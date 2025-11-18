/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

class TokenRequestTest
{
    @Test
    void testGetTokenRequestBody_withAllFields()
    {
        // given
        TokenRequest request = TokenRequest.builder()
                .grantType("password")
                .clientId("test-client")
                .clientSecret("test-secret")
                .scope(Set.of("read", "write"))
                .username("testuser")
                .password("testpass")
                .build();

        // when
        String body = request.getTokenRequestBody();

        // then
        assertThat(body).contains("grant_type=password");
        assertThat(body).contains("client_id=test-client");
        assertThat(body).contains("client_secret=test-secret");
        assertThat(body).contains("username=testuser");
        assertThat(body).contains("password=testpass");
        assertThat(body).contains("scope=");
        assertThat(body).contains("read", "write");
    }

    @Test
    void testGetTokenRequestBody_withRequiredFieldsOnly()
    {
        // given
        TokenRequest request = TokenRequest.builder()
                .grantType("client_credentials")
                .clientId("test-client")
                .build();

        // when
        String body = request.getTokenRequestBody();

        // then
        assertThat(body).isEqualTo("grant_type=client_credentials&client_id=test-client");
    }

    @Test
    void testGetTokenRequestBody_urlEncodesSpecialCharacters()
    {
        // given
        TokenRequest request = TokenRequest.builder()
                .grantType("password")
                .clientId("client@test")
                .clientSecret("secret&key=value")
                .username("user name")
                .password("p@ss w0rd!")
                .build();

        // when
        String body = request.getTokenRequestBody();

        // then
        assertThat(body).contains("client_id=client%40test");
        assertThat(body).contains("client_secret=secret%26key%3Dvalue");
        assertThat(body).contains("username=user+name");
        assertThat(body).contains("password=p%40ss+w0rd%21");
    }

    @Test
    void testGetTokenRequestBody_withBlankClientSecret()
    {
        // given
        TokenRequest request = TokenRequest.builder()
                .grantType("client_credentials")
                .clientId("test-client")
                .clientSecret("   ")
                .build();

        // when
        String body = request.getTokenRequestBody();

        // then
        assertThat(body).doesNotContain("client_secret");
    }

    @Test
    void testGetTokenRequestBody_withEmptyScope()
    {
        // given
        TokenRequest request = TokenRequest.builder()
                .grantType("client_credentials")
                .clientId("test-client")
                .scope(Set.of())
                .build();

        // when
        String body = request.getTokenRequestBody();

        // then
        assertThat(body).doesNotContain("scope");
    }

    @Test
    void testGetTokenRequestBody_scopesAreSeparatedBySpace()
    {
        // given
        TokenRequest request = TokenRequest.builder()
                .grantType("client_credentials")
                .clientId("test-client")
                .scope(Set.of("openid", "evolution"))
                .build();

        // when
        String body = request.getTokenRequestBody();

        // then
        assertThat(body).containsPattern("scope=(openid\\+evolution|evolution\\+openid)");
    }
}
