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
package org.alfresco.hxi_connector.nucleus_sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.SyncOrchestrationService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NucleusSyncApplicationIntegrationTest
{
    @Autowired
    private ApplicationContext context;

    @Autowired
    private TestRestTemplate restTemplate;

    @MockitoBean
    private SyncOrchestrationService syncOrchestrationService;

    @Test
    void contextLoads()
    {
        assertThat(context).isNotNull();
    }

    @Test
    void applicationStartsSuccessfully()
    {
        assertThat(context.getBean(NucleusSyncApplication.class)).isNotNull();
    }

    @Test
    void actuatorHealthEndpointIsAvailable()
    {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    void syncStatusEndpointIsAvailable()
    {
        // Given
        when(syncOrchestrationService.getSyncStatus())
                .thenReturn(Map.of("syncInProgress", false));

        // When
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/sync/status",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("syncInProgress");
    }

    @Test
    void syncTriggerEndpointIsAvailable()
    {
        // Given
        when(syncOrchestrationService.performFullSync())
                .thenReturn("Sync completed");

        // When
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                "/sync/trigger",
                HttpMethod.POST,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<Map<String, Object>>() {});

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("success");
    }
}
