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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.filter;

import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Filter;
import org.alfresco.repo.event.v1.model.NodeResource;

public interface RepoEventFilterApplier
{
    /**
     * @param nodeResource
     *            Current node resource
     * @param filter
     *            configuration
     * @return If node is allowed by supplied filter configuration
     */
    boolean isNodeAllowed(NodeResource nodeResource, Filter filter);

    /**
     * Default implementation can be used to filter based on properties that are always present in the "before" resource. It is not suitable if the property is omitted when unchanged and therefore needs a specific implementation.
     *
     * @param currentlyAllowed
     *            If current version of the node is allowed
     * @param nodeResourceBefore
     *            Previous version of the resource
     * @param filter
     *            configuration
     * @return whether previous version of a node is allowed by supplied filter configuration
     */
    default boolean isNodeBeforeAllowed(boolean currentlyAllowed, NodeResource nodeResourceBefore, Filter filter)
    {
        return isNodeAllowed(nodeResourceBefore, filter);
    }
}
