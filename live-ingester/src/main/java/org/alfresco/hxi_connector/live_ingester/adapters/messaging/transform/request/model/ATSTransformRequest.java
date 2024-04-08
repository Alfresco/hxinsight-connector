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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.request.model;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.alfresco.hxi_connector.live_ingester.adapters.config.jackson.ClientDataSerializer;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.model.ClientData;

/**
 * Model used for Transform Request Events.
 *
 * This is a mirror of org.alfresco.repo.rendition2.TransformRequest in Alfresco Repository project.
 */
public record ATSTransformRequest(String requestId,
        String nodeRef,
        String targetMediaType,
        @JsonSerialize(using = ClientDataSerializer.class) ClientData clientData,
        Map<String, String> transformOptions,
        String replyQueue) implements Serializable
{

    private static final String WORKSPACE_SPACES_STORE = "workspace://SpacesStore/";
    private static final String TIMEOUT_KEY = "timeout";

    public ATSTransformRequest(String nodeRef, String targetMediaType, ClientData clientData, int timeout, String replyQueue)
    {
        this(
                UUID.randomUUID().toString(),
                WORKSPACE_SPACES_STORE + nodeRef,
                targetMediaType,
                clientData,
                Map.of(TIMEOUT_KEY, String.valueOf(timeout)),
                replyQueue);
    }
}
