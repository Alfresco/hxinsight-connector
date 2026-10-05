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

package org.alfresco.hxi_connector.bulk_ingester.processor.mapper;

import java.util.Optional;

import org.alfresco.database.connector.model.PropertyKey;
import org.alfresco.database.connector.model.QName;

public interface NamespacePrefixMapper
{

    default String toPrefixedName(QName qname)
    {
        return toPrefixedName(qname.getUri(), qname.getLocalName());
    }

    default String toPrefixedName(PropertyKey propertyKey)
    {
        return toPrefixedName(propertyKey.getUri(), propertyKey.getLocalName());
    }

    String toPrefixedName(String uri, String localName);

    /**
     * Inverse of {@link #toPrefixedName(String, String)}.
     *
     * @return the qname, or empty if the name has no prefix or the prefix is unknown.
     */
    Optional<QName> toQName(String prefixedName);

}
