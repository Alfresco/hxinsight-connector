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

package org.alfresco.hxi_connector.live_ingester.util.camel;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.alfresco.hxi_connector.live_ingester.adapters.auth.AuthenticationService;
import org.alfresco.hxi_connector.live_ingester.domain.ports.uuid.UUIDProvider;
import org.alfresco.hxi_connector.live_ingester.util.IntegrationTest;

@IntegrationTest
@EnableAutoConfiguration
@SpringBootTest(properties = {
        "logging.level.org.alfresco=DEBUG"
})
public class CamelTestBase
{
    public static final String TEST_UUID = "1bde77d8-39c0-4c5d-81c5-7593b3c8e087";

    @MockBean
    private UUIDProvider uuidProvider;

    @Autowired
    public CamelTest camelTest;

    @BeforeEach
    void setUp()
    {
        camelTest.reset();
        when(uuidProvider.random()).thenReturn(TEST_UUID);
    }

    @Configuration
    public static class IntegrationPropertiesTestConfig
    {
        @Bean
        public AuthenticationService authenticationService()
        {
            Authentication authentication = mock();

            doReturn(Set.of((GrantedAuthority) () -> "OAUTH2_USER")).when(authentication).getAuthorities();
            doReturn(true).when(authentication).isAuthenticated();

            SecurityContextHolder.getContext().setAuthentication(authentication);
            return mock();
        }
    }
}
