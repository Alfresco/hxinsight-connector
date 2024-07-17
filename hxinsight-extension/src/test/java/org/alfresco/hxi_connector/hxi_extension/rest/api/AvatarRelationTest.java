package org.alfresco.hxi_connector.hxi_extension.rest.api;

import static org.junit.Assert.*;

import java.net.http.HttpClient;

import org.junit.Test;

import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.resource.content.BinaryResource;

public class AvatarRelationTest
{
    private static final String AGENT_ID = "agent-id";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final AvatarRelation avatarRelation = new AvatarRelation(httpClient);

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

        // when
        BinaryResource binaryResource = avatarRelation.readById(AGENT_ID, avatarId, null);

        // then
        assertNotNull(binaryResource);
    }

}
