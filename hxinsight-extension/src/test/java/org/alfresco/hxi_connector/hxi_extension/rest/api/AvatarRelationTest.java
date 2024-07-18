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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.FileBinaryResource;

public class AvatarRelationTest
{
    private static final String AGENT_ID = "agent-id";
    private static FileBinaryResource sampleImage;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final AvatarRelation avatarRelation = new AvatarRelation(httpClient);

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
        AvatarRelation avatarRelationSpy = spy(AvatarRelation.class);
        doReturn(sampleImage).when(avatarRelationSpy).getSampleAvatar();

        // when
        BinaryResource binaryResource = avatarRelationSpy.readById(AGENT_ID, avatarId, null);

        // then
        assertEquals(binaryResource, sampleImage);
    }

}
