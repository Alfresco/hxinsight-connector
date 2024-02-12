/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 Alfresco Software Limited
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

package org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model;

import static org.alfresco.hxi_connector.live_ingester.domain.utils.EnsureUtils.ensureThat;

import java.util.Set;

import lombok.Getter;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeProperty;

@Getter
public class PredefinedNodeMetadataProperty<V>
{
    public static final PredefinedNodeMetadataProperty<String> TYPE = new PredefinedNodeMetadataProperty<>("type");
    public static final PredefinedNodeMetadataProperty<String> CREATED_BY_USER_WITH_ID = new PredefinedNodeMetadataProperty<>("createdByUserWithId");
    public static final PredefinedNodeMetadataProperty<String> MODIFIED_BY_USER_WITH_ID = new PredefinedNodeMetadataProperty<>("modifiedByUserWithId");
    public static final PredefinedNodeMetadataProperty<Set<String>> ASPECTS_NAMES = new PredefinedNodeMetadataProperty<>("aspectsNames");

    private final String name;

    private PredefinedNodeMetadataProperty(String name)
    {
        ensureThat(!name.contains(":"),
                "Predefined properties names should not contain the ':' character, as this may cause a collision with the client's custom property.");
        this.name = name;
    }

    public NodeProperty<V> withValue(V value)
    {
        return new NodeProperty<>(name, value);
    }
}
