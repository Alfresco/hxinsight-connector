/*
 *
 *  * #%L
 *  * Alfresco HX Insight Connector
 *  * %%
 *  * Copyright (C) 2023 Alfresco Software Limited
 *  * %%
 *  * This file is part of the Alfresco software.
 *  * If the software was purchased under a paid Alfresco license, the terms of
 *  * the paid license agreement will prevail.  Otherwise, the software is
 *  * provided under the following open source license terms:
 *  *
 *  * Alfresco is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU Lesser General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * Alfresco is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 *  * #L%
 *
 *
 */

package org.alfresco.hxi_connector.live_ingester.messaging.in.utils;

import static lombok.AccessLevel.PRIVATE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import lombok.NoArgsConstructor;

import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;

@SuppressWarnings({"PMD.TestClassWithoutTestCases"})
@NoArgsConstructor(access = PRIVATE)
public class RepoEventTestUtils
{

    public static void setTime(RepoEvent<DataAttributes<NodeResource>> event, long timestamp)
    {
        given(event.getTime()).willReturn(dateFromTimestamp(timestamp));
    }

    public static ZonedDateTime dateFromTimestamp(long timestamp)
    {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC"));
    }

    public static void setType(RepoEvent<DataAttributes<NodeResource>> event, EventType type)
    {
        given(event.getType()).willReturn(type.getType());
    }

    public static void setNodeResource(RepoEvent<DataAttributes<NodeResource>> event, NodeResource nodeResource)
    {
        DataAttributes<NodeResource> data = mockData(event);

        given(data.getResource()).willReturn(nodeResource);
    }

    public static void setNodeResourceBefore(RepoEvent<DataAttributes<NodeResource>> event, NodeResource nodeResourceBefore)
    {
        DataAttributes<NodeResource> data = mockData(event);

        given(data.getResourceBefore()).willReturn(nodeResourceBefore);
    }

    private static DataAttributes<NodeResource> mockData(RepoEvent<DataAttributes<NodeResource>> event)
    {
        if (event.getData() != null)
        {
            return event.getData();
        }

        DataAttributes<NodeResource> data = mock();

        given(event.getData()).willReturn(data);

        return data;
    }
}
