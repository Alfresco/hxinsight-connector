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
package org.alfresco.hxi_connector.common.adapters.messaging.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

import org.alfresco.hxi_connector.common.config.properties.Application;

@ExtendWith({MockitoExtension.class, SystemStubsExtension.class})
class ApplicationInfoProviderTest
{
    @SystemStub
    private SystemProperties systemProperties;

    @Mock
    private RepositoryInformation repositoryInformationMock;
    @Mock
    private Application applicationPropertiesMock;

    @Test
    void givenNoUserDataYetFetched_whenGetUserAgentData_thenCallAcsApiAndCalculateData()
    {
        given(applicationPropertiesMock.getVersion()).willReturn("1.0.0");
        given(repositoryInformationMock.getRepositoryVersion()).willReturn("23.2.0");
        systemProperties.set("os.name", "Windows");
        systemProperties.set("os.version", "10");
        systemProperties.set("os.arch", "amd64");
        ApplicationInfoProvider objectUnderTest = new ApplicationInfoProvider(repositoryInformationMock, applicationPropertiesMock);
        // Expected User Agent Format: "ACS HXI Connector/[applicationVersion] ACS/[repositoryVersion] ([osName] [osVersion] [osArch])"
        String expectedUserAgentData = "ACS HXI Connector/1.0.0 ACS/23.2.0 (Windows 10 amd64)";

        // when
        String actualUserAgentData = objectUnderTest.getUserAgentData();

        // then
        assertEquals(expectedUserAgentData, actualUserAgentData);
        then(repositoryInformationMock).should().getRepositoryVersion();
    }

    @Test
    void givenUserDataFetched_whenGetUserAgentData_thenGetDataWithoutCalculation()
    {
        ApplicationInfoProvider objectUnderTest = new ApplicationInfoProvider(repositoryInformationMock, applicationPropertiesMock);
        ReflectionTestUtils.setField(objectUnderTest, "applicationInfo", "ACS HXI Connector/1.0.0 ACS/23.2.0 (Windows 10 amd64)");
        // Expected User Agent Format: "ACS HXI Connector/[applicationVersion] ACS/[repositoryVersion] ([osName] [osVersion] [osArch])"
        String expectedUserAgentData = "ACS HXI Connector/1.0.0 ACS/23.2.0 (Windows 10 amd64)";

        // when
        String actualUserAgentData = objectUnderTest.getUserAgentData();

        // then
        assertEquals(expectedUserAgentData, actualUserAgentData);
        then(applicationPropertiesMock).shouldHaveNoInteractions();
        then(repositoryInformationMock).shouldHaveNoInteractions();
    }

}
