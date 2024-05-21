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
package org.alfresco.hxi_connector.common.adapters.auth.util;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.time.temporal.ChronoUnit;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.test.context.DynamicPropertyRegistry;

import org.alfresco.hxi_connector.common.adapters.auth.AuthenticationResult;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthUtils
{
    private static final String CLIENT_NAME = "Dummy Client";
    private static final String CLIENT_ID = "dummy-client";
    private static final String CLIENT_SECRET = "dummy's-client-dummy-secret";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyNTE0RTNGREJENEJDMTM5OUE1RjA3NjUxQzU1MTNGQ0Y0RjhDMjUiLCJ4NXQiOiJFbEZPUDl2VXZCT1pwZkIyVWNWUlA4OVBqQ1UiLCJ0eXAiOiJhdCtqd3QifQ.eyJpc3MiOiJodHRwczovL2F1dGguaWFtLnN0YWdpbmcuZXhwZXJpZW5jZS5oeWxhbmQuY29tL2lkcCIsIm5iZiI6MTcwNzQwNjM2MiwiaWF0IjoxNzA3NDA2MzYyLCJleHAiOjE3MDc0MDk5NjIsImF1ZCI6Imh4cCIsInNjb3BlIjpbImh4cC5pbnRlZ3JhdGlvbnMiXSwiYW1yIjpbInVybjpoeWxhbmQ6cGFyYW1zOm9hdXRoOmdyYW50LXR5cGU6YXBpLWNyZWRlbnRpYWxzIl0sImNsaWVudF9pZCI6IjZmODcwYmViLTUwNGEtNDBjZi1iMjlmLWVjY2I2MjAyYjM3YSIsInN1YiI6IjgwOGQ1YzE2LTg0MDQtNGZiYi1hMTczLTUyNmExYzdhM2M0MiIsImF1dGhfdGltZSI6MTcwNzQwNjM2MiwiaWRwIjoibG9jYWwifQ.ghydijmyuwWemWiG0WYeEYDcPeClEcGFhko8EAkZjZmcmfs-rqmy4QFQM8ineOqK8CCHMbdJptxcyEAii7bvjzjh2syc-m0RHuDCpEyT1grMqdJ_uA0t72Edi-azDVsBOtKCI_tW7a_wdcylG1kXuORovGgMmZjeMorJO_JrBzxSQltUlCGZw-yNByRFi9fJgX_UONHc8J4-igeHYRtkgZK1t0c9cJKuOD6Cdp33a5bwibQbdo4nnGuXkXZ9kFE45g2uuzHvCiAYZQIzJ79kGxjFM6Ke-YG2VjEFaHzonqAgPKVkNO4v_vwlaL1Bsuwrj1ul9qHUQ48JyK84G-RNjA";
    private static final int EXPIRES_IN = 3600;
    private static final String TOKEN_TYPE = "Bearer";
    private static final String AUTH_GRAND_TYPE = "auth_grand_type";
    private static final String SCOPE = "hxp.integrations";
    private static final String AUTH_RESPONSE_BODY_PATTERN = """
                {
                    "access_token": "%s",
                    "expires_in": %s,
                    "token_type": "%s",
                    "scope": "%s"
                }
            """;
    private static final String AUTH_REQUEST_BODY_PATTERN = "grant_type=%s&client_id=%s&client_secret=%s&scope=%s";
    public static final String TOKEN_PATH = "/token";
    public static final String AUTH_HEADER = TOKEN_TYPE + " " + ACCESS_TOKEN;

    public static String createAuthResponseBody()
    {
        return AUTH_RESPONSE_BODY_PATTERN.formatted(ACCESS_TOKEN, EXPIRES_IN, TOKEN_TYPE, SCOPE);
    }

    public static String createAuthRequestBody()
    {
        return AUTH_REQUEST_BODY_PATTERN.formatted(
                encode(AUTH_GRAND_TYPE, UTF_8),
                encode(CLIENT_ID, UTF_8),
                encode(CLIENT_SECRET, UTF_8),
                encode(String.join(",", SCOPE), UTF_8));
    }

    public static ClientRegistration creatClientRegistration(String tokenUri)
    {
        return ClientRegistration.withRegistrationId("hx-auth-mock")
                .tokenUri(tokenUri)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .clientName(CLIENT_NAME)
                .authorizationGrantType(new AuthorizationGrantType(AUTH_GRAND_TYPE))
                .scope(SCOPE)
                .build();
    }

    public static AuthenticationResult createExpectedAuthResult()
    {
        return new AuthenticationResult(ACCESS_TOKEN, EXPIRES_IN, ChronoUnit.SECONDS, TOKEN_TYPE, SCOPE, 200);
    }

    public static String createAuthorizationHeader()
    {
        return TOKEN_TYPE + " " + ACCESS_TOKEN;
    }

    public static void overrideAuthProperties(DynamicPropertyRegistry registry, String mockServerBaseUrl)
    {
        registry.add("spring.security.oauth2.client.registration.hyland-experience-auth.client-id", () -> CLIENT_ID);
        registry.add("spring.security.oauth2.client.registration.hyland-experience-auth.client-secret", () -> CLIENT_SECRET);
        registry.add("spring.security.oauth2.client.registration.hyland-experience-auth.client-name", () -> CLIENT_NAME);
        registry.add("spring.security.oauth2.client.registration.hyland-experience-auth.authorization-grant-type", () -> AUTH_GRAND_TYPE);
        registry.add("spring.security.oauth2.client.registration.hyland-experience-auth.scope", () -> SCOPE);
        registry.add("spring.security.oauth2.client.provider.hyland-experience-auth.token-uri", () -> mockServerBaseUrl + TOKEN_PATH);
    }
}
