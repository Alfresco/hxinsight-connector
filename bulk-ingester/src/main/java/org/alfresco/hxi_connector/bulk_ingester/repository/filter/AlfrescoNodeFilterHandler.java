/*
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

package org.alfresco.hxi_connector.bulk_ingester.repository.filter;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.database.connector.model.AlfrescoNode;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlfrescoNodeFilterHandler
{

    private final List<AlfrescoNodeFilterApplier> alfrescoNodeFilterAppliers;
    private final NodeFilterConfig nodeFilterConfig;

    public boolean filterNode(AlfrescoNode alfrescoNode)
    {

        return alfrescoNodeFilterAppliers.stream()
                .peek(f -> log.atDebug().log("Applying filters {} to repo node of id: {}", nodeFilterConfig, alfrescoNode.getId()))
                .allMatch(f -> f.applyFilter(alfrescoNode, nodeFilterConfig));
    }
}
