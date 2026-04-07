/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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

package org.alfresco.hxi_connector.live_ingester.domain.ports.repository;

import org.alfresco.hxi_connector.live_ingester.domain.usecase.content.model.File;

/**
 * Port interface for downloading content directly from Alfresco repository. Used when content does not require transformation (source and target MIME types match).
 */
@FunctionalInterface
public interface RepositoryContentStorage
{
    /**
     * Downloads content for a node directly from the Alfresco repository.
     *
     * @param nodeId
     *            the node ID (UUID format, without workspace://SpacesStore/ prefix)
     * @return the file content
     */
    File downloadContent(String nodeId);
}
