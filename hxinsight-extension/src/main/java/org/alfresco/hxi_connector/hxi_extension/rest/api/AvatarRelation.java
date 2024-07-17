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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.alfresco.hxi_connector.hxi_extension.rest.api.exception.AgentAvatarException;
import org.alfresco.rest.framework.WebApiDescription;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.RelationshipResourceNotFoundException;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.content.BinaryResource;
import org.alfresco.rest.framework.resource.content.FileBinaryResource;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.util.TempFileProvider;

@Slf4j
@AllArgsConstructor
@RelationshipResource(name = "avatars", title = "Avatars of agents", entityResource = AgentsEntityResource.class)
public class AvatarRelation implements RelationshipResourceAction.ReadById<BinaryResource>
{
    private HttpClient client;

    @Override
    @WebApiDescription(title = "Get Agent Avatar image")
    public BinaryResource readById(String agentId, String avatarId, Parameters parameters) throws RelationshipResourceNotFoundException
    {
        if (!avatarId.equals("-default-"))
        {
            log.info("Avatar id is different than -default-");
            throw new NotFoundException(String.format("Avatar with id=%s not found", avatarId));
        }

        return getSampleAvatar();
    }

    private FileBinaryResource getSampleAvatar()
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.wikipedia.org/portal/wikipedia.org/assets/img/Wikipedia-logo-v2@2x.png"))
                .GET()
                .build();

        try
        {
            HttpResponse<InputStream> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            File file = TempFileProvider.createTempFile(httpResponse.body(), "RenditionsApi-", ".png");
            return new FileBinaryResource(file, null);
        }
        catch (IOException e)
        {
            log.error("Error getting avatar image.", e);
            throw new AgentAvatarException("Avatar image cannot be received.");
        }
        catch (InterruptedException e)
        {
            log.error("Error getting avatar image.", e);
            throw new AgentAvatarException("Avatar image could not be received due to an interruption in the operation.");
        }
        catch (Exception e)
        {
            log.error("Failed to create temp file.", e);
            throw new AgentAvatarException("Failed to create the avatar temp file.");
        }
    }

}
