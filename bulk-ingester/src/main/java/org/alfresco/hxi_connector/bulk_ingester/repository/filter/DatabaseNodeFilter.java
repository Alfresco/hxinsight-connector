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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.alfresco.database.connector.NodeParams;
import org.alfresco.database.connector.model.QName;
import org.alfresco.hxi_connector.bulk_ingester.processor.mapper.NamespacePrefixMapper;

/**
 * Narrows a node query in the database using the aspect and type allow lists, so that sparse matches in a large repository are not paged through one empty page at a time.
 * <p>
 * This only reduces what the database returns: the per-node filters still run on every result, and deny and path filters are not pushed down.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseNodeFilter
{
    private final NamespacePrefixMapper namespacePrefixMapper;
    private final NodeFilterConfig nodeFilterConfig;

    public NodeParams apply(NodeParams nodeParams)
    {
        final NodeParams withAspects = resolve("aspect", nodeFilterConfig.aspect().allow())
                .map(nodeParams::withAspects)
                .orElse(nodeParams);
        return resolve("type", nodeFilterConfig.type().allow())
                .map(withAspects::withTypes)
                .orElse(withAspects);
    }

    private Optional<Set<QName>> resolve(String kind, List<String> allowed)
    {
        if (allowed.isEmpty())
        {
            return Optional.empty();
        }

        final Set<QName> qnames = new HashSet<>();
        for (String name : allowed)
        {
            final Optional<QName> qname = namespacePrefixMapper.toQName(name);
            if (qname.isEmpty())
            {
                log.warn("Allowed {} '{}' has an unknown namespace prefix, so the {} filter is not applied in the database", kind, name, kind);
                return Optional.empty();
            }
            qnames.add(qname.get());
        }
        return Optional.of(qnames);
    }
}
