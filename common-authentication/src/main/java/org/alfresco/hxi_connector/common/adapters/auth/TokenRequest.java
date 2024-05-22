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

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.ToString;
import org.apache.logging.log4j.util.Strings;
import org.springframework.util.CollectionUtils;

@AllArgsConstructor
@ToString
public class TokenRequest
{
    private String grantType;
    private String clientId;
    private String clientSecret;
    private Set<String> scope;
    private String username;
    private String password;

    public String getTokenRequestBody()
    {
        StringBuilder body = new StringBuilder();
        body.append("grant_type=").append(encode(this.grantType, UTF_8)).append("&client_id=").append(encode(clientId, UTF_8));
        if (Strings.isNotBlank(this.clientSecret))
        {
            body.append("&client_secret=").append(encode(this.clientSecret, UTF_8));
        }
        if (!CollectionUtils.isEmpty(this.scope))
        {
            body.append("&scope=").append(encode(String.join(",", this.scope), UTF_8));
        }
        if (Strings.isNotBlank(this.username))
        {
            body.append("&username=").append(encode(this.username, UTF_8));
        }
        if (Strings.isNotBlank(this.password))
        {
            body.append("&password=").append(encode(this.password, UTF_8));
        }

        return body.toString();
    }

    public static TokenRequestBuilder builder()
    {
        return new TokenRequestBuilder();
    }

    public static class TokenRequestBuilder
    {
        private String grantType;
        private String clientId;
        private String clientSecret;
        private Set<String> scope;
        private String username;
        private String password;

        TokenRequestBuilder()
        {}

        public TokenRequestBuilder grantType(String grantType)
        {
            this.grantType = grantType;
            return this;
        }

        public TokenRequestBuilder clientId(String clientId)
        {
            this.clientId = clientId;
            return this;
        }

        public TokenRequestBuilder clientSecret(String clientSecret)
        {
            if (Strings.isNotBlank(clientSecret))
            {
                this.clientSecret = clientSecret;
            }
            return this;
        }

        public TokenRequestBuilder scope(Set<String> scope)
        {
            if (!CollectionUtils.isEmpty(scope))
            {
                this.scope = scope;
            }
            return this;
        }

        public TokenRequestBuilder username(String username)
        {
            if (Strings.isNotBlank(username))
            {
                this.username = username;
            }
            return this;
        }

        public TokenRequestBuilder password(String password)
        {
            if (Strings.isNotBlank(password))
            {
                this.password = password;
            }
            return this;
        }

        public TokenRequest build()
        {
            return new TokenRequest(this.grantType, this.clientId, this.clientSecret, this.scope, this.username, this.password);
        }
    }
}
