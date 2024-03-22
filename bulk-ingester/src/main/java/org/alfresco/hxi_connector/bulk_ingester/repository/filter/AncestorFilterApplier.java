/*-
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
package org.alfresco.hxi_connector.bulk_ingester.repository.filter;

import static java.time.Instant.ofEpochMilli;
import static java.time.ZonedDateTime.ofInstant;

import static org.alfresco.elasticsearch.db.connector.ParentChildAssociationOrdinality.PRIMARY;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.springframework.stereotype.Component;

import org.alfresco.elasticsearch.db.connector.AlfrescoMetadataRepository;
import org.alfresco.elasticsearch.db.connector.ChildAssocParams;
import org.alfresco.elasticsearch.db.connector.ParentChildAssociationOrdinality;
import org.alfresco.elasticsearch.db.connector.model.AlfrescoNode;
import org.alfresco.elasticsearch.db.connector.model.ChildAssocMetaData;
import org.alfresco.hxi_connector.common.repository.filter.CollectionFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class AncestorFilterApplier implements AlfrescoNodeFilterApplier
{
    private final AlfrescoMetadataRepository metadataRepository;

    @Override
    public boolean applyFilter(AlfrescoNode alfrescoNode, NodeFilterConfig filterConfig)
    {
        final List<String> allowed = filterConfig.path().allow();
        final List<String> denied = filterConfig.path().deny();
        if (allowed.isEmpty() && denied.isEmpty())
        {
            // return fast so that there is no need to pull primary hierarchy from DB
            log.atDebug().log("Ancestor filters will not be applied on Alfresco node of id: {} because there are no filters defined.", alfrescoNode.getId());
            return true;
        }
        final List<String> primaryHierarchy = getPrimaryHierarchy(alfrescoNode);
        log.atDebug().log("Applying primary ancestor filters on Alfresco node of id: {}", alfrescoNode.getId());
        return CollectionFilter.filter(primaryHierarchy, allowed, denied);
    }

    private List<String> getPrimaryHierarchy(AlfrescoNode alfrescoNode)
    {
        final Set<ParentChildAssociationOrdinality> ordinalities = Set.of(PRIMARY);
        final ZonedDateTime nodeTimestamp = ofInstant(ofEpochMilli(alfrescoNode.getTimestamp()), ZoneId.systemDefault());
        final Deque<String> primaryHierarchy = new ArrayDeque<>();
        ChildAssocMetaData primaryParentAssociation = alfrescoNode.getPrimaryParentAssociation();
        while (primaryParentAssociation != null)
        {
            primaryHierarchy.add(primaryParentAssociation.getParentUuid());
            ChildAssocParams childAssocParams = new ChildAssocParams(ordinalities, primaryParentAssociation.getParentId(), nodeTimestamp);
            Set<ChildAssocMetaData> childAssocMetaData = SetUtils.emptyIfNull(metadataRepository.getChildAssocMetaData(childAssocParams));
            primaryParentAssociation = childAssocMetaData.isEmpty() ? null : childAssocMetaData.iterator().next();
        }
        return primaryHierarchy.stream().toList();
    }
}
