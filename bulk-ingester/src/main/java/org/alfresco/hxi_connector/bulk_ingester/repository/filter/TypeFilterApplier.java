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
import org.alfresco.hxi_connector.bulk_ingester.processor.mapper.NamespacePrefixMapper;
import org.alfresco.hxi_connector.common.repository.filter.FieldFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class TypeFilterApplier implements AlfrescoNodeFilterApplier
{
    private final NamespacePrefixMapper predefinedNamespacePrefixMapper;

    @Override
    public boolean applyFilter(AlfrescoNode alfrescoNode, NodeFilterConfig filterConfig)
    {
        final String nodeType = predefinedNamespacePrefixMapper.toPrefixedName(alfrescoNode.getType());
        final List<String> allowed = filterConfig.type().allow();
        final List<String> denied = filterConfig.type().deny();
        log.atDebug().log("Applying type filters on Alfresco node of id: {}", alfrescoNode.getId());
        return FieldFilter.filter(nodeType, allowed, denied);
    }
}
