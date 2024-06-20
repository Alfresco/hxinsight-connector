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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.logging.log4j.util.Strings;
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

    public List<NameValuePair> getTokenRequestBody()
    {
        List<NameValuePair> form = new LinkedList<>();

        form.add(new BasicNameValuePair("grant_type", this.grantType));
        form.add(new BasicNameValuePair("client_id", this.clientId));

        if (Strings.isNotBlank(this.clientSecret))
        {
            form.add(new BasicNameValuePair("client_secret", this.clientSecret));
        }
        if (!CollectionUtils.isEmpty(this.scope))
        {
            form.add(new BasicNameValuePair("scope", String.join(",", this.scope)));
        }
        if (Strings.isNotBlank(this.username))
        {
            form.add(new BasicNameValuePair("username", this.username));
        }
        if (Strings.isNotBlank(this.password))
        {
            form.add(new BasicNameValuePair("password", this.password));
        }

        return form;
    }
}
