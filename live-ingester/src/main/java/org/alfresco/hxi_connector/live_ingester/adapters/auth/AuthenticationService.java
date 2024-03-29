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

import static org.apache.hc.core5.http.HttpHeaders.AUTHORIZATION;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.scheduling.DelegatingSecurityContextTaskScheduler;
import org.springframework.stereotype.Service;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Authorization;
import org.alfresco.hxi_connector.live_ingester.domain.exception.LiveIngesterRuntimeException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService
{
    private static final String CLIENT_REGISTRATION_ID = "hyland-experience-auth";
    private static final String APP_NAME_ATTRIBUTE_KEY = "applicationName";
    private static final String SERVICE_USER_ATTRIBUTE_KEY = "serviceUser";
    public static final String ENVIRONMENT_KEY_ATTRIBUTE_KEY = "hxAiEnvironmentKey";
    private static final String ENVIRONMENT_KEY_HEADER = "hxai-environment";
    private static final int WAIT_FOR_PAUSE_TIME_MILLIS = 100;

    private final OAuth2ClientProperties oAuth2ClientProperties;
    private final IntegrationProperties integrationProperties;
    private final AuthenticationManager authenticationManager;
    private final TaskScheduler taskScheduler;
    private final CamelContext camelContext;

    @PostConstruct
    public void authenticationSchedule()
    {
        if (isTokenUriNotBlank())
        {
            SecurityContext securityContext = SecurityContextHolder.getContext();
            DelegatingSecurityContextTaskScheduler delegatingTaskScheduler = new DelegatingSecurityContextTaskScheduler(taskScheduler, securityContext);
            Runnable authenticationTask = () -> {
                waitFor(camelContext::isStarted);
                authenticate();
            };
            delegatingTaskScheduler.scheduleWithFixedDelay(
                    authenticationTask,
                    Duration.ofMinutes(integrationProperties.hylandExperience().authentication().refreshDelayMinutes()));
        }
    }

    public void authenticate()
    {
        String clientName = oAuth2ClientProperties.getRegistration().get(CLIENT_REGISTRATION_ID).getClientName();
        OAuth2AuthenticationToken authenticationToken = createOAuth2AuthenticationToken(clientName);
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static void setAuthorizationToken(Exchange exchange)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2LoginAuthenticationToken authenticationToken)
        {
            OAuth2AccessToken accessToken = authenticationToken.getAccessToken();
            Map<String, Object> principalAttributes = authenticationToken.getPrincipal().getAttributes();

            String authorization = accessToken.getTokenType().getValue() + " " + accessToken.getTokenValue();
            exchange.getIn().setHeaders(Map.of(
                    AUTHORIZATION, authorization,
                    ENVIRONMENT_KEY_HEADER, principalAttributes.get(ENVIRONMENT_KEY_ATTRIBUTE_KEY)));
            log.debug("Authorization :: auth header added");
        }
        else
        {
            log.warn("Spring security context does not contain authentication principal of type " + OAuth2LoginAuthenticationToken.class.getSimpleName());
        }
    }

    private OAuth2AuthenticationToken createOAuth2AuthenticationToken(String clientName)
    {
        Authorization authorizationProperties = integrationProperties.hylandExperience().authorization();
        Map<String, Object> userAttributes = Map.of(
                APP_NAME_ATTRIBUTE_KEY, clientName,
                SERVICE_USER_ATTRIBUTE_KEY, authorizationProperties.serviceUser(),
                ENVIRONMENT_KEY_ATTRIBUTE_KEY, authorizationProperties.environmentKey());
        OAuth2UserAuthority oAuth2UserAuthority = new OAuth2UserAuthority(userAttributes);
        OAuth2User oAuth2User = new DefaultOAuth2User(Set.of(oAuth2UserAuthority), userAttributes, APP_NAME_ATTRIBUTE_KEY);
        return new OAuth2AuthenticationToken(oAuth2User, Set.of(oAuth2UserAuthority), CLIENT_REGISTRATION_ID);
    }

    private boolean isTokenUriNotBlank()
    {
        return oAuth2ClientProperties.getProvider().containsKey(CLIENT_REGISTRATION_ID)
                && StringUtils.isNotBlank(oAuth2ClientProperties.getProvider().get(CLIENT_REGISTRATION_ID).getTokenUri());
    }

    private static void waitFor(Supplier<Boolean> supplier)
    {
        while (!supplier.get())
        {
            try
            {
                Thread.sleep(WAIT_FOR_PAUSE_TIME_MILLIS);
            }
            catch (InterruptedException e)
            {
                throw new LiveIngesterRuntimeException(e);
            }
        }
    }
}
