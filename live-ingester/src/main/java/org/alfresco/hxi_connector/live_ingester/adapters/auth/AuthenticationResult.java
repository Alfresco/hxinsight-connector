/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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
package org.alfresco.hxi_connector.live_ingester.adapters.auth;

import static java.util.Objects.requireNonNullElse;

import static org.alfresco.hxi_connector.live_ingester.adapters.auth.HxAuthenticationClient.EXPECTED_STATUS_CODE;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;

@Validated
public record AuthenticationResult(
        @NotBlank @JsonProperty("access_token") String accessToken,
        @Positive @JsonProperty("expires_in") int expiresIn,
        TemporalUnit temporalUnit,
        @NotBlank @JsonProperty("token_type") String tokenType,
        @NotBlank String scope,
        Integer statusCode)
{
    public AuthenticationResult
    {
        temporalUnit = ChronoUnit.SECONDS;
        statusCode = requireNonNullElse(statusCode, EXPECTED_STATUS_CODE);
    }
}
