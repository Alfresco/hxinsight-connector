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

package org.alfresco.hxi_connector.live_ingester.domain.model.out;

import static org.alfresco.hxi_connector.live_ingester.domain.utils.EnsureUtils.ensureThat;

import java.util.Set;

import lombok.Getter;

@Getter
public class PredefinedNodeProperty<V>
{
    public static final PredefinedNodeProperty<String> NAME = new PredefinedNodeProperty<>("name");
    public static final PredefinedNodeProperty<String> PRIMARY_ASSOC_Q_NAME = new PredefinedNodeProperty<>("primaryAssocQName");
    public static final PredefinedNodeProperty<String> TYPE = new PredefinedNodeProperty<>("type");
    public static final PredefinedNodeProperty<String> CREATED_BY_USER_WITH_ID = new PredefinedNodeProperty<>("createdByUserWithId");
    public static final PredefinedNodeProperty<String> MODIFIED_BY_USER_WITH_ID = new PredefinedNodeProperty<>("modifiedByUserWithId");
    public static final PredefinedNodeProperty<Set<String>> ASPECTS_NAMES = new PredefinedNodeProperty<>("aspectsNames");
    public static final PredefinedNodeProperty<Boolean> IS_FOLDER = new PredefinedNodeProperty<>("isFolder");
    public static final PredefinedNodeProperty<Boolean> IS_FILE = new PredefinedNodeProperty<>("isFile");
    public static final PredefinedNodeProperty<Long> CREATED_AT = new PredefinedNodeProperty<>("createdAt");

    private final String name;

    private PredefinedNodeProperty(String name)
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
