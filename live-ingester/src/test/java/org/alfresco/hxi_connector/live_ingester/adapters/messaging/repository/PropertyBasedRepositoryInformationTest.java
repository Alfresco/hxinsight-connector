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

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.common.exception.ValidationException;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Repository;

@ExtendWith(MockitoExtension.class)
class PropertyBasedRepositoryInformationTest
{

    @Mock
    private IntegrationProperties integrationPropertiesMock;

    @InjectMocks
    private PropertyBasedRepositoryInformation objectUnderTest;

    @Test
    void givenVersionOverrideIsPresent_whenGetRepositoryVersion_thenReturnVersionOverride()
    {
        given(integrationPropertiesMock.alfresco()).willReturn(mock(IntegrationProperties.Alfresco.class));
        given(integrationPropertiesMock.alfresco().repository()).willReturn(mock(Repository.class));
        String versionOverride = "23.3.0";
        given(integrationPropertiesMock.alfresco().repository().versionOverride()).willReturn(versionOverride);

        String actualVersion = objectUnderTest.getRepositoryVersion();

        assertEquals(versionOverride, actualVersion);
    }

    @Test
    void givenVersionOverrideIsNotPresent_whenGetRepositoryVersion_thenThrowException()
    {
        given(integrationPropertiesMock.alfresco()).willReturn(mock(IntegrationProperties.Alfresco.class));
        given(integrationPropertiesMock.alfresco().repository()).willReturn(mock(Repository.class));
        given(integrationPropertiesMock.alfresco().repository().versionOverride()).willReturn(null);

        assertThrows(ValidationException.class, objectUnderTest::getRepositoryVersion);
    }
}
