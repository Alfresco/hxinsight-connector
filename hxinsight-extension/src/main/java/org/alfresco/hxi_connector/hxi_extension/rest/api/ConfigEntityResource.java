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
package org.alfresco.hxi_connector.hxi_extension.rest.api;

import static java.lang.String.format;

import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensureNotBlank;
import static org.alfresco.hxi_connector.common.util.EnsureUtils.ensureThat;

import org.apache.commons.validator.routines.UrlValidator;

import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;

@EntityResource(name = "config", title = "Hyland Experience Insight Configuration")
public class ConfigEntityResource implements EntityResourceAction.ReadById<ConfigEntityResource.HxIConfig>
{
    private final HxIConfig config;

    public ConfigEntityResource(String knowledgeRetrievalUrl)
    {
        this.config = new HxIConfig(knowledgeRetrievalUrl);
    }

    @Override
    public HxIConfig readById(String id, Parameters parameters) throws EntityNotFoundException
    {
        if (!id.equals("-default-"))
        {
            throw new EntityNotFoundException(format("%s (you should use id '-default-')", id));
        }

        return config;
    }

    public record HxIConfig(String knowledgeRetrievalUrl)
    {
        public HxIConfig
        {
            ensureNotBlank(knowledgeRetrievalUrl, "Knowledge retrieval url must not be blank.");
            ensureThat(new UrlValidator().isValid(knowledgeRetrievalUrl), "Knowledge retrieval url must be a valid URL.");
        }
    }
}
