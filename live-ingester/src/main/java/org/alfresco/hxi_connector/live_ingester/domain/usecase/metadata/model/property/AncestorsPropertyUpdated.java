/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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
package org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.property;

import java.util.List;
import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.AncestorsProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.property.PropertyResolver;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AncestorsPropertyUpdated extends PropertyDelta<List<String>>
{

    private final String parentId;
    private final List<String> ancestorIds;

    public AncestorsPropertyUpdated(String propertyName, String parentId, List<String> ancestorIds)
    {
        super(propertyName);
        this.parentId = parentId;
        this.ancestorIds = ancestorIds;
    }

    public String getParentId()
    {
        return parentId;
    }

    public List<String> getAncestorIds()
    {
        return ancestorIds;
    }

    @Override
    public void applyOn(UpdateNodeEvent event)
    {
        event.addAncestorInstruction(new AncestorsProperty(getPropertyName(), parentId, ancestorIds));

    }

    @Override
    public <R> Optional<PropertyDelta<R>> resolveWith(PropertyResolver<R> resolver)
    {
        return Optional.empty();
    }

    public static AncestorsPropertyUpdatedBuilder builder(String propertyName)
    {
        return new AncestorsPropertyUpdatedBuilder(propertyName);
    }

    public static class AncestorsPropertyUpdatedBuilder
    {
        private final String propertyName;
        private String parentId;
        private List<String> ancestorIds;

        public AncestorsPropertyUpdatedBuilder(String propertyName)
        {
            this.propertyName = propertyName;
        }

        public AncestorsPropertyUpdatedBuilder parentId(String parentId)
        {
            this.parentId = parentId;
            return this;
        }

        public AncestorsPropertyUpdatedBuilder ancestorIds(List<String> ancestorIds)
        {
            this.ancestorIds = ancestorIds;
            return this;
        }

        public AncestorsPropertyUpdated build()
        {
            return new AncestorsPropertyUpdated(propertyName, parentId, ancestorIds);
        }
    }
}
