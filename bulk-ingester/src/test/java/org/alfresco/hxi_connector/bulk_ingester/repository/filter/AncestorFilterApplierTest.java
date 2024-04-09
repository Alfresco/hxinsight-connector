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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import static org.alfresco.elasticsearch.db.connector.ParentChildAssociationOrdinality.PRIMARY;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.elasticsearch.db.connector.AlfrescoMetadataRepository;
import org.alfresco.elasticsearch.db.connector.ChildAssocParams;
import org.alfresco.elasticsearch.db.connector.ParentChildAssociationOrdinality;
import org.alfresco.elasticsearch.db.connector.model.AlfrescoNode;
import org.alfresco.elasticsearch.db.connector.model.ChildAssocMetaData;

@ExtendWith(MockitoExtension.class)
class AncestorFilterApplierTest
{

    private static final String DENIED_NODE_REF = "denied-node-ref";
    private static final String PARENT_NODE_REF = "parent-node-ref";
    private static final String GRANDPARENT_NODE_REF = "grandparent-node-ref";
    private static final String NODE_REF = "node-ref";
    private static final String NODE_REF2 = "node-ref2";

    private static final int CHILD_ID = 3;
    private static final int PARENT_ID = 2;
    private static final int GRANDPARENT_ID = 1;
    @Mock
    private AlfrescoMetadataRepository mockMetadataRepository;
    @Mock
    private AlfrescoNode mockAlfrescoNode;
    @Mock
    private NodeFilterConfig mockFilter;
    @Mock
    private NodeFilterConfig.Path mockPath;

    @InjectMocks
    private AncestorFilterApplier objectUnderTest;

    @BeforeEach
    void mockBasicData()
    {
        given(mockFilter.path()).willReturn(mockPath);
    }

    @Test
    void givenNoFiltersDefined_whenFilterAppliedOnAnyNode_thenAllowNode()
    {
        given(mockPath.allow()).willReturn(emptyList());
        given(mockPath.deny()).willReturn(emptyList());

        // when
        boolean result = objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter);

        then(mockMetadataRepository).shouldHaveNoInteractions();
        assertTrue(result);
    }

    @Test
    void givenEmptyAllowedAndNonEmptyDenied_whenParentAndNodeNotInDenied_thenAllowNode()
    {
        given(mockPath.allow()).willReturn(emptyList());
        given(mockPath.deny()).willReturn(List.of(DENIED_NODE_REF));
        given(mockAlfrescoNode.getId()).willReturn(Long.valueOf(CHILD_ID));
        given(mockAlfrescoNode.getNodeRef()).willReturn(NODE_REF);
        final ZonedDateTime nodeTimestamp = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        given(mockAlfrescoNode.getTimestamp()).willReturn(nodeTimestamp.toInstant().toEpochMilli());
        ChildAssocMetaData parentAssoc = new ChildAssocMetaData(CHILD_ID, PARENT_ID, null, null);
        parentAssoc.setParentUuid(PARENT_NODE_REF);
        given(mockAlfrescoNode.getPrimaryParentAssociation()).willReturn(parentAssoc);
        given(mockMetadataRepository.getChildAssocMetaData(any())).willReturn(emptySet());

        // when
        boolean result = objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter);

        then(mockMetadataRepository).should(times(1)).getChildAssocMetaData(any());
        then(mockMetadataRepository).shouldHaveNoMoreInteractions();
        assertTrue(result);
    }

    @Test
    void givenEmptyAllowedAndNonEmptyDenied_whenNoneFromPathInDenied_thenAllowNode()
    {
        given(mockPath.allow()).willReturn(emptyList());
        given(mockPath.deny()).willReturn(List.of(DENIED_NODE_REF));
        given(mockAlfrescoNode.getId()).willReturn(Long.valueOf(CHILD_ID));
        given(mockAlfrescoNode.getNodeRef()).willReturn(NODE_REF);
        final ZonedDateTime nodeTimestamp = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        given(mockAlfrescoNode.getTimestamp()).willReturn(nodeTimestamp.toInstant().toEpochMilli());
        ChildAssocMetaData parentAssoc = new ChildAssocMetaData(CHILD_ID, PARENT_ID, null, null);
        parentAssoc.setParentUuid(PARENT_NODE_REF);
        given(mockAlfrescoNode.getPrimaryParentAssociation()).willReturn(parentAssoc);
        final Set<ParentChildAssociationOrdinality> ordinalities = Set.of(PRIMARY);
        ChildAssocParams childAssocParams = new ChildAssocParams(ordinalities, PARENT_ID, nodeTimestamp);
        ChildAssocMetaData grandParentAssoc = new ChildAssocMetaData(PARENT_ID, GRANDPARENT_ID, null, null);
        grandParentAssoc.setParentUuid(GRANDPARENT_NODE_REF);
        given(mockMetadataRepository.getChildAssocMetaData(childAssocParams)).willReturn(Set.of(grandParentAssoc));

        // when
        boolean result = objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter);

        then(mockMetadataRepository).should(times(2)).getChildAssocMetaData(any());
        then(mockMetadataRepository).shouldHaveNoMoreInteractions();
        assertTrue(result);
    }

    @Test
    void givenEmptyAllowedAndNonEmptyDenied_whenParentInDenied_thenDenyNode()
    {
        given(mockPath.allow()).willReturn(emptyList());
        given(mockPath.deny()).willReturn(List.of(DENIED_NODE_REF));
        given(mockAlfrescoNode.getId()).willReturn(Long.valueOf(CHILD_ID));
        given(mockAlfrescoNode.getNodeRef()).willReturn(NODE_REF);
        final ZonedDateTime nodeTimestamp = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        given(mockAlfrescoNode.getTimestamp()).willReturn(nodeTimestamp.toInstant().toEpochMilli());
        ChildAssocMetaData parentAssoc = new ChildAssocMetaData(CHILD_ID, PARENT_ID, null, null);
        parentAssoc.setParentUuid(DENIED_NODE_REF);
        given(mockAlfrescoNode.getPrimaryParentAssociation()).willReturn(parentAssoc);
        final Set<ParentChildAssociationOrdinality> ordinalities = Set.of(PRIMARY);
        ChildAssocParams childAssocParams = new ChildAssocParams(ordinalities, PARENT_ID, nodeTimestamp);
        ChildAssocMetaData grandParentAssoc = new ChildAssocMetaData(PARENT_ID, GRANDPARENT_ID, null, null);
        grandParentAssoc.setParentUuid(GRANDPARENT_NODE_REF);
        given(mockMetadataRepository.getChildAssocMetaData(childAssocParams)).willReturn(Set.of(grandParentAssoc));

        // when
        boolean result = objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter);

        then(mockMetadataRepository).should(times(2)).getChildAssocMetaData(any());
        then(mockMetadataRepository).shouldHaveNoMoreInteractions();
        Assertions.assertFalse(result);
    }

    @Test
    void givenEmptyAllowedAndNonEmptyDenied_whenAncestorInDenied_thenDenyNode()
    {
        given(mockPath.allow()).willReturn(emptyList());
        given(mockPath.deny()).willReturn(List.of(DENIED_NODE_REF));
        given(mockAlfrescoNode.getId()).willReturn(Long.valueOf(CHILD_ID));
        given(mockAlfrescoNode.getNodeRef()).willReturn(NODE_REF);
        final ZonedDateTime nodeTimestamp = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        given(mockAlfrescoNode.getTimestamp()).willReturn(nodeTimestamp.toInstant().toEpochMilli());
        ChildAssocMetaData parentAssoc = new ChildAssocMetaData(CHILD_ID, PARENT_ID, null, null);
        parentAssoc.setParentUuid(PARENT_NODE_REF);
        given(mockAlfrescoNode.getPrimaryParentAssociation()).willReturn(parentAssoc);
        final Set<ParentChildAssociationOrdinality> ordinalities = Set.of(PRIMARY);
        ChildAssocParams childAssocParams = new ChildAssocParams(ordinalities, PARENT_ID, nodeTimestamp);
        ChildAssocMetaData grandParentAssoc = new ChildAssocMetaData(PARENT_ID, GRANDPARENT_ID, null, null);
        grandParentAssoc.setParentUuid(DENIED_NODE_REF);
        given(mockMetadataRepository.getChildAssocMetaData(childAssocParams)).willReturn(Set.of(grandParentAssoc));

        // when
        boolean result = objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter);

        then(mockMetadataRepository).should(times(2)).getChildAssocMetaData(any());
        then(mockMetadataRepository).shouldHaveNoMoreInteractions();
        Assertions.assertFalse(result);
    }

    @Test
    void givenEmptyAllowedAndNonEmptyDenied_whenNodeRefInDenied_thenDenyNode()
    {
        given(mockPath.allow()).willReturn(emptyList());
        given(mockPath.deny()).willReturn(List.of(DENIED_NODE_REF));
        given(mockAlfrescoNode.getId()).willReturn(Long.valueOf(CHILD_ID));
        given(mockAlfrescoNode.getNodeRef()).willReturn(DENIED_NODE_REF);
        final ZonedDateTime nodeTimestamp = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        given(mockAlfrescoNode.getTimestamp()).willReturn(nodeTimestamp.toInstant().toEpochMilli());
        ChildAssocMetaData parentAssoc = new ChildAssocMetaData(CHILD_ID, PARENT_ID, null, null);
        parentAssoc.setParentUuid(PARENT_NODE_REF);
        given(mockAlfrescoNode.getPrimaryParentAssociation()).willReturn(parentAssoc);
        final Set<ParentChildAssociationOrdinality> ordinalities = Set.of(PRIMARY);
        ChildAssocParams childAssocParams = new ChildAssocParams(ordinalities, PARENT_ID, nodeTimestamp);
        ChildAssocMetaData grandParentAssoc = new ChildAssocMetaData(PARENT_ID, GRANDPARENT_ID, null, null);
        grandParentAssoc.setParentUuid(GRANDPARENT_NODE_REF);
        given(mockMetadataRepository.getChildAssocMetaData(childAssocParams)).willReturn(Set.of(grandParentAssoc));

        // when
        boolean result = objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter);

        then(mockMetadataRepository).should(times(2)).getChildAssocMetaData(any());
        then(mockMetadataRepository).shouldHaveNoMoreInteractions();
        Assertions.assertFalse(result);
    }

    @Test
    void givenNonEmptyAllowedAndEmptyDenied_whenNodeRefInAllowed_thenAllowNode()
    {
        given(mockPath.allow()).willReturn(List.of(NODE_REF));
        given(mockPath.deny()).willReturn(emptyList());
        given(mockAlfrescoNode.getId()).willReturn(Long.valueOf(CHILD_ID));
        given(mockAlfrescoNode.getNodeRef()).willReturn(NODE_REF);
        final ZonedDateTime nodeTimestamp = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        given(mockAlfrescoNode.getTimestamp()).willReturn(nodeTimestamp.toInstant().toEpochMilli());
        ChildAssocMetaData parentAssoc = new ChildAssocMetaData(CHILD_ID, PARENT_ID, null, null);
        parentAssoc.setParentUuid(PARENT_NODE_REF);
        given(mockAlfrescoNode.getPrimaryParentAssociation()).willReturn(parentAssoc);
        final Set<ParentChildAssociationOrdinality> ordinalities = Set.of(PRIMARY);
        ChildAssocParams childAssocParams = new ChildAssocParams(ordinalities, PARENT_ID, nodeTimestamp);
        ChildAssocMetaData grandParentAssoc = new ChildAssocMetaData(PARENT_ID, GRANDPARENT_ID, null, null);
        grandParentAssoc.setParentUuid(GRANDPARENT_NODE_REF);
        given(mockMetadataRepository.getChildAssocMetaData(childAssocParams)).willReturn(Set.of(grandParentAssoc));

        // when
        boolean result = objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter);

        then(mockMetadataRepository).should(times(2)).getChildAssocMetaData(any());
        then(mockMetadataRepository).shouldHaveNoMoreInteractions();
        Assertions.assertTrue(result);
    }

    @Test
    void givenNonEmptyAllowedAndEmptyDenied_whenNoneFromPathInAllowed_thenDenyNode()
    {
        given(mockPath.allow()).willReturn(List.of(NODE_REF2));
        given(mockPath.deny()).willReturn(emptyList());
        given(mockAlfrescoNode.getId()).willReturn(Long.valueOf(CHILD_ID));
        given(mockAlfrescoNode.getNodeRef()).willReturn(NODE_REF);
        final ZonedDateTime nodeTimestamp = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        given(mockAlfrescoNode.getTimestamp()).willReturn(nodeTimestamp.toInstant().toEpochMilli());
        ChildAssocMetaData parentAssoc = new ChildAssocMetaData(CHILD_ID, PARENT_ID, null, null);
        parentAssoc.setParentUuid(PARENT_NODE_REF);
        given(mockAlfrescoNode.getPrimaryParentAssociation()).willReturn(parentAssoc);
        final Set<ParentChildAssociationOrdinality> ordinalities = Set.of(PRIMARY);
        ChildAssocParams childAssocParams = new ChildAssocParams(ordinalities, PARENT_ID, nodeTimestamp);
        ChildAssocMetaData grandParentAssoc = new ChildAssocMetaData(PARENT_ID, GRANDPARENT_ID, null, null);
        grandParentAssoc.setParentUuid(GRANDPARENT_NODE_REF);
        given(mockMetadataRepository.getChildAssocMetaData(childAssocParams)).willReturn(Set.of(grandParentAssoc));

        // when
        boolean result = objectUnderTest.applyFilter(mockAlfrescoNode, mockFilter);

        then(mockMetadataRepository).should(times(2)).getChildAssocMetaData(any());
        then(mockMetadataRepository).shouldHaveNoMoreInteractions();
        assertFalse(result);
    }

}
