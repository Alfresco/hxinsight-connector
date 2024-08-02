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

package org.alfresco.hxi_connector.hxi_extension.rest.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.hxi_extension.service.HxInsightClient;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.FileBinaryResource;

@ExtendWith(MockitoExtension.class)
public class AvatarRelationTest
{
    private static final String AGENT_ID = "agent-id";
    private static FileBinaryResource sampleImage;

    @Mock
    private HxInsightClient mockHxInsightClient;
    @InjectMocks
    private AvatarRelation avatarRelation;

    @BeforeAll
    static void setUp() throws IOException
    {
        sampleImage = new FileBinaryResource(File.createTempFile("image", ".png"), null);
    }

    @Test
    public void shouldThrowNotFoundExceptionIfAvatarIdIsDifferentThanDefault()
    {
        // given
        String avatarId = "avatarId";

        // when
        assertThrows(NotFoundException.class, () -> avatarRelation.readById(AGENT_ID, avatarId, null));
    }

    @Test
    public void shouldReturnAvatarForDefaultId()
    {
        // given
        String avatarId = "-default-";
        given(mockHxInsightClient.getAvatar(AGENT_ID)).willReturn(sampleImage);

        // when
        BinaryResource binaryResource = avatarRelation.readById(AGENT_ID, avatarId, null);

        // then
        assertEquals(binaryResource, sampleImage);
    }

}
