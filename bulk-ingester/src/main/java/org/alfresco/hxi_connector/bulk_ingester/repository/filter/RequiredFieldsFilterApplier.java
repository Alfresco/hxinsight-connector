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
package org.alfresco.hxi_connector.bulk_ingester.repository.filter;

import java.util.Map;
import java.util.function.Supplier;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import org.alfresco.database.connector.model.AlfrescoNode;

@Component
@Slf4j
public class RequiredFieldsFilterApplier implements AlfrescoNodeFilterApplier
{
    @Override
    public boolean applyFilter(AlfrescoNode alfrescoNode, NodeFilterConfig filterConfig)
    {
        log.atDebug().log("Filtering :: Applying required-fields filter on node id: {}", alfrescoNode.getId());
        Map<String, Supplier<Object>> fieldAccessors = Map.of(
                "name", () -> getNameProperty(alfrescoNode),
                "type", alfrescoNode::getType,
                "createdAt", alfrescoNode::getCreatedAt,
                "createdBy", alfrescoNode::getCreator,
                "modifiedAt", alfrescoNode::getModifiedAt,
                "modifiedBy", alfrescoNode::getModifier);
        return filterConfig.requiredFields().stream()
                .allMatch(field -> {
                    Supplier<Object> accessor = fieldAccessors.get(field);
                    if (accessor == null)
                    {
                        log.atWarn().log("Filtering :: Unknown required field '{}', skipping", field);
                        return true;
                    }
                    return accessor.get() != null;
                });
    }

    private Object getNameProperty(AlfrescoNode alfrescoNode)
    {
        if (CollectionUtils.isEmpty(alfrescoNode.getNodeProperties()))
        {
            return null;
        }
        return alfrescoNode.getNodeProperties().stream()
                .filter(p -> p.getPropertyKey() != null && "name".equals(p.getPropertyKey().getLocalName()))
                .findFirst()
                .orElse(null);
    }
}
