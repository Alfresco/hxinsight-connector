/*-
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
package org.alfresco.hxi_connector.live_ingester.subsystem;

import java.util.Map;
import java.util.Set;

import org.alfresco.hxi_connector.live_ingester.subsystem.Exceptions.IamSyncException;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

/**
 * Constants and small helpers for IAM event handling.
 */
public final class AuthorizationConstants
{
    public static final String PERSON_TYPE = "cm:person";
    public static final String USER_TYPE = "usr:user";
    public static final String GROUP_TYPE = "cm:authorityContainer";

    public static final String MAIL_ATTRIBUTE = "cm:email";
    public static final String EMAIL_PROPERTY = "cm:email";

    public static final String EVENT_UPDATED_TYPE = "org.alfresco.event.node.Updated";
    public static final String EVENT_DELETE_TYPE = "org.alfresco.event.node.Deleted";
    public static final String EVENT_CREATED_TYPE = "org.alfresco.event.node.Created";

    public static final String USERNAME_PROPERTY_1 = "cm:userName";
    public static final String USERNAME_PROPERTY_2 = "usr:username";

    public static final Set<String> USER_TYPES = Set.of(PERSON_TYPE, USER_TYPE);
    public static final Set<String> UPDATE_OR_DELETE = Set.of(EVENT_UPDATED_TYPE, EVENT_DELETE_TYPE);

    private AuthorizationConstants()
    {
        // utility class
    }

    /**
     * Reads the user id from a repo event, supporting both {@code cm:userName} and {@code usr:username}.
     * Falls back to {@code resourceBefore} for delete events where the live resource is null.
     *
     * @throws IamSyncException if no username property is present.
     */
    public static String fetchUserId(RepoEvent<DataAttributes<NodeResource>> event)
    {
        Map<String, ?> props = propertiesOf(event);
        if (props != null)
        {

            Object value = props.get(USERNAME_PROPERTY_1);
            if (value == null)
            {
                value = props.get(USERNAME_PROPERTY_2);
            }
            if(value != null){
                String userName = value.toString();
                if(!userName.isEmpty()){
                    return userName;
                }
            }
        }
        throw new IamSyncException("Username property not found or empty in event "
                + (event != null ? event.getId() : "<null>"));
    }

    private static Map<String, ?> propertiesOf(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (event == null || event.getData() == null)
        {
            return null;
        }
        NodeResource resource = event.getData().getResource();
        if (resource == null || resource.getProperties() == null)
        {
            resource = event.getData().getResourceBefore();
        }
        return resource == null ? null : resource.getProperties();
    }
}
