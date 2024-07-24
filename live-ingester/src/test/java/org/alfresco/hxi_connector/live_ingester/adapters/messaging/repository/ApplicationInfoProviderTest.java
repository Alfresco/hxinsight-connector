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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.api.DiscoveryApiClient;

@ExtendWith({MockitoExtension.class, SystemStubsExtension.class})
class ApplicationInfoProviderTest
{
    @SystemStub
    private SystemProperties systemProperties;

    @Mock
    private DiscoveryApiClient discoveryApiMock;
    @Mock
    private IntegrationProperties integrationPropertiesMock;

    @InjectMocks
    private ApplicationInfoProvider objectUnderTest;

    @Test
    void givenNoUserDataYetFetched_whenGetUserAgentData_thenCallAcsApiAndCalculateData()
    {
        given(integrationPropertiesMock.application()).willReturn(mock(IntegrationProperties.Application.class));
        given(integrationPropertiesMock.application().version()).willReturn("1.0.0");
        given(discoveryApiMock.getRepositoryVersion()).willReturn(Optional.of("23.2.0"));
        systemProperties.set("os.name", "Windows");
        systemProperties.set("os.version", "10");
        systemProperties.set("os.arch", "amd64");
        // Expected User Agent Format: "ACS HXI Connector/[applicationVersion] ACS/[repositoryVersion] ([osName] [osVersion] [osArch])"
        String expectedUserAgentData = "ACS HXI Connector/1.0.0 ACS/23.2.0 (Windows 10 amd64)";

        // when
        String actualUserAgentData = objectUnderTest.getUserAgentData();

        // then
        assertEquals(expectedUserAgentData, actualUserAgentData);
        then(discoveryApiMock).should().getRepositoryVersion();
    }

    @Test
    void givenUserDataFetched_whenGetUserAgentData_thenGetDataWithoutCalculation()
    {
        ReflectionTestUtils.setField(objectUnderTest, "applicationInfo", "ACS HXI Connector/1.0.0 ACS/23.2.0 (Windows 10 amd64)");
        // Expected User Agent Format: "ACS HXI Connector/[applicationVersion] ACS/[repositoryVersion] ([osName] [osVersion] [osArch])"
        String expectedUserAgentData = "ACS HXI Connector/1.0.0 ACS/23.2.0 (Windows 10 amd64)";

        // when
        String actualUserAgentData = objectUnderTest.getUserAgentData();

        // then
        assertEquals(expectedUserAgentData, actualUserAgentData);
        then(integrationPropertiesMock).shouldHaveNoInteractions();
        then(discoveryApiMock).shouldHaveNoInteractions();
    }

    @Test
    void givenNoUserDataYetFetchedAndRepositoryIsOff_whenGetUserAgentDat_thenGetVersionFromAcsPropertiesAndCalculateData()
    {
        IntegrationProperties.Alfresco alfresco = mock(IntegrationProperties.Alfresco.class, RETURNS_DEEP_STUBS);

        given(integrationPropertiesMock.application()).willReturn(mock(IntegrationProperties.Application.class));
        given(integrationPropertiesMock.alfresco()).willReturn(alfresco);
        given(integrationPropertiesMock.application().version()).willReturn("1.0.0");
        given(discoveryApiMock.getRepositoryVersion()).willReturn(Optional.empty());
        given(alfresco.repository().version()).willReturn("23.2.0");
        systemProperties.set("os.name", "Windows");
        systemProperties.set("os.version", "10");
        systemProperties.set("os.arch", "amd64");
        // Expected User Agent Format: "ACS HXI Connector/[applicationVersion] ACS/[repositoryVersion] ([osName] [osVersion] [osArch])"
        String expectedUserAgentData = "ACS HXI Connector/1.0.0 ACS/23.2.0 (Windows 10 amd64)";

        // when
        String actualUserAgentData = objectUnderTest.getUserAgentData();

        // then
        assertEquals(expectedUserAgentData, actualUserAgentData);
        then(discoveryApiMock).should().getRepositoryVersion();
    }

    @Test
    void givenRepositoryIsOffAndNotConfiguredAcsVersion_whenGetUserAgentDat_thenThrownAnError()
    {
        given(integrationPropertiesMock.application()).willReturn(mock(IntegrationProperties.Application.class));
        given(integrationPropertiesMock.alfresco()).willReturn(mock(IntegrationProperties.Alfresco.class));
        given(integrationPropertiesMock.application().version()).willReturn(null);
        given(discoveryApiMock.getRepositoryVersion()).willReturn(Optional.empty());

        // when, then
        assertThrows(IllegalStateException.class, () -> objectUnderTest.getUserAgentData());
    }

}
