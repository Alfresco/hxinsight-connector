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
package org.alfresco.hxi_connector.hxi_extension.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;

@ExtendWith(MockitoExtension.class)
class AcsRepositoryInformationTest
{

    @Mock
    private DescriptorService descriptorServiceMock;

    @InjectMocks
    private AcsRepositoryInformation objectUnderTest;

    @Test
    void testGetRepositoryVersion()
    {
        // given
        given(descriptorServiceMock.getServerDescriptor()).willReturn(mock(Descriptor.class));
        given(descriptorServiceMock.getServerDescriptor().getVersionMajor()).willReturn("23");
        given(descriptorServiceMock.getServerDescriptor().getVersionMinor()).willReturn("2");
        given(descriptorServiceMock.getServerDescriptor().getVersionRevision()).willReturn("2");

        // when
        String result = objectUnderTest.getRepositoryVersion();

        // then
        assertEquals("23.2.2", result);
    }

}
