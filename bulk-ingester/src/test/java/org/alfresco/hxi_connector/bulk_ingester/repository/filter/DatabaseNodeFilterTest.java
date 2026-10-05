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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.database.connector.NodeParams;
import org.alfresco.database.connector.model.QName;
import org.alfresco.hxi_connector.bulk_ingester.processor.mapper.NamespacePrefixMapper;

@ExtendWith(MockitoExtension.class)
class DatabaseNodeFilterTest
{
    private static final String CM = "http://www.alfresco.org/model/content/1.0";
    private static final QName VERSIONABLE = QName.newTransientInstance(CM, "versionable");
    private static final QName AUDITABLE = QName.newTransientInstance(CM, "auditable");
    private static final QName CONTENT = QName.newTransientInstance(CM, "content");
    private static final NodeParams PARAMS = NodeParams.searchByIdRange(0, 10);

    @Mock
    private NamespacePrefixMapper prefixMapper;

    @Test
    void shouldNotNarrowTheQueryWithoutAllowLists()
    {
        DatabaseNodeFilter filter = filterWith(List.of(), List.of(), List.of("cm:versionable"), List.of("cm:content"));

        assertEquals(PARAMS, filter.apply(PARAMS));
    }

    @Test
    void shouldNarrowTheQueryToTheAllowedAspects()
    {
        given(prefixMapper.toQName("cm:versionable")).willReturn(Optional.of(VERSIONABLE));
        given(prefixMapper.toQName("cm:auditable")).willReturn(Optional.of(AUDITABLE));
        DatabaseNodeFilter filter = filterWith(List.of("cm:versionable", "cm:auditable"), List.of(), List.of(), List.of());

        assertEquals(PARAMS.withAspects(Set.of(VERSIONABLE, AUDITABLE)), filter.apply(PARAMS));
    }

    @Test
    void shouldNarrowTheQueryToTheAllowedTypes()
    {
        given(prefixMapper.toQName("cm:content")).willReturn(Optional.of(CONTENT));
        DatabaseNodeFilter filter = filterWith(List.of(), List.of("cm:content"), List.of(), List.of());

        assertEquals(PARAMS.withType(CONTENT), filter.apply(PARAMS));
    }

    @Test
    void shouldCombineAspectAndTypeFilters()
    {
        given(prefixMapper.toQName("cm:versionable")).willReturn(Optional.of(VERSIONABLE));
        given(prefixMapper.toQName("cm:content")).willReturn(Optional.of(CONTENT));
        DatabaseNodeFilter filter = filterWith(List.of("cm:versionable"), List.of("cm:content"), List.of(), List.of());

        assertEquals(PARAMS.withAspect(VERSIONABLE).withType(CONTENT), filter.apply(PARAMS));
    }

    @Test
    void shouldLeaveTheQueryAloneIfAnAllowedNameCannotBeResolved()
    {
        given(prefixMapper.toQName("cm:versionable")).willReturn(Optional.of(VERSIONABLE));
        given(prefixMapper.toQName("unknown:aspect")).willReturn(Optional.empty());
        DatabaseNodeFilter filter = filterWith(List.of("cm:versionable", "unknown:aspect"), List.of(), List.of(), List.of());

        assertEquals(PARAMS, filter.apply(PARAMS));
    }

    private DatabaseNodeFilter filterWith(List<String> allowedAspects, List<String> allowedTypes, List<String> deniedAspects, List<String> deniedTypes)
    {
        NodeFilterConfig config = new NodeFilterConfig(
                new NodeFilterConfig.Aspect(allowedAspects, deniedAspects),
                new NodeFilterConfig.Type(allowedTypes, deniedTypes),
                new NodeFilterConfig.Path(List.of(), List.of()));
        return new DatabaseNodeFilter(prefixMapper, config);
    }
}
