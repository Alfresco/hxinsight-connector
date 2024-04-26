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
package org.alfresco.hxi_connector.common.adapters.auth;

import static org.alfresco.hxi_connector.common.adapters.auth.AuthSupport.CLIENT_REGISTRATION_ID;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import org.apache.camel.CamelContext;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.scheduling.DelegatingSecurityContextTaskScheduler;

import org.alfresco.hxi_connector.common.adapters.auth.config.properties.Authentication;
import org.alfresco.hxi_connector.common.adapters.auth.config.properties.Authorization;
import org.alfresco.hxi_connector.common.exception.HxInsightConnectorRuntimeException;

@RequiredArgsConstructor
public class AuthenticationService
{
    private static final int WAIT_FOR_PAUSE_TIME_MILLIS = 100;

    private final OAuth2ClientProperties oAuth2ClientProperties;
    private final Authorization authorizationProperties;
    private final Authentication authenticationProperties;
    private final AuthenticationManager authenticationManager;
    private final TaskScheduler taskScheduler;
    private final CamelContext camelContext;

    @PostConstruct
    public void authenticationSchedule()
    {
        if (AuthSupport.isTokenUriNotBlank(oAuth2ClientProperties))
        {
            SecurityContext securityContext = SecurityContextHolder.getContext();
            DelegatingSecurityContextTaskScheduler delegatingTaskScheduler = new DelegatingSecurityContextTaskScheduler(taskScheduler, securityContext);
            Runnable authenticationTask = () -> {
                waitFor(camelContext::isStarted);
                String clientName = oAuth2ClientProperties.getRegistration().get(CLIENT_REGISTRATION_ID).getClientName();
                AuthSupport.authenticate(clientName, authorizationProperties, authenticationManager);
            };
            delegatingTaskScheduler.scheduleWithFixedDelay(
                    authenticationTask,
                    Duration.ofMinutes(authenticationProperties.refreshDelayMinutes()));
        }
    }

    private static void waitFor(Supplier<Boolean> supplier)
    {
        while (!supplier.get())
        {
            try
            {
                TimeUnit.MILLISECONDS.sleep(WAIT_FOR_PAUSE_TIME_MILLIS);
            }
            catch (InterruptedException e)
            {
                throw new HxInsightConnectorRuntimeException(e);
            }
        }
    }
}
