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

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import org.springframework.util.CollectionUtils;

@AllArgsConstructor
@ToString
@Builder
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
        final StringBuilder body = new StringBuilder();
        body.append("grant_type=").append(encode(this.grantType, UTF_8)).append("&client_id=").append(encode(clientId, UTF_8));
        if (isNotBlank(this.clientSecret))
        {
            body.append("&client_secret=").append(encode(this.clientSecret, UTF_8));
        }
        if (!CollectionUtils.isEmpty(this.scope))
        {
            body.append("&scope=").append(encode(String.join(" ", this.scope), UTF_8));
        }
        if (isNotBlank(this.username))
        {
            body.append("&username=").append(encode(this.username, UTF_8));
        }
        if (isNotBlank(this.password))
        {
            body.append("&password=").append(encode(this.password, UTF_8));
        }

        return body.toString();
    }
}
