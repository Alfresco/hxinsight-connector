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

package org.alfresco.hxi_connector.bulk_ingester.processor.mapper.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import org.alfresco.database.connector.model.PropertyKey;
import org.alfresco.database.connector.model.QName;
import org.alfresco.hxi_connector.bulk_ingester.exception.BulkIngesterRuntimeException;

@EnableAutoConfiguration
@SpringBootTest(
        classes = PredefinedNamespacePrefixMapper.class,
        properties = "alfresco.bulk.ingest.namespace-prefixes-mapping=classpath:test-namespace-prefixes.json")
class PredefinedNamespacePrefixMapperIntegrationTest
{

    @Autowired
    private PredefinedNamespacePrefixMapper namespacePrefixMapper;

    @Test
    void shouldMapKnownNamespaceToPrefix()
    {
        // given
        String namespace = "http://www.alfresco.org";
        String propertyName = "someProperty";

        // when
        String propertyWithPrefix = namespacePrefixMapper.toPrefixedName(namespace, propertyName);

        // then
        assertEquals("alf:someProperty", propertyWithPrefix);
    }

    @Test
    void shouldMapKnownNamespaceToPrefix_qname()
    {
        // given
        QName property = QName.newTransientInstance("http://www.alfresco.org", "someProperty");

        // when
        String propertyWithPrefix = namespacePrefixMapper.toPrefixedName(property);

        // then
        assertEquals("alf:someProperty", propertyWithPrefix);
    }

    @Test
    void shouldMapKnownNamespaceToPrefix_propertyKey()
    {
        // given
        PropertyKey propertyKey = new PropertyKey();
        propertyKey.setUri("http://www.alfresco.org");
        propertyKey.setLocalName("someProperty");

        // when
        String propertyWithPrefix = namespacePrefixMapper.toPrefixedName(propertyKey);

        // then
        assertEquals("alf:someProperty", propertyWithPrefix);
    }

    @Test
    void shouldThrowIfNamespaceIsUnknown()
    {
        // given
        String namespace = "http://www.random_namespace.org";
        String propertyName = "someProperty";

        // then
        assertThrows(BulkIngesterRuntimeException.class, () -> namespacePrefixMapper.toPrefixedName(namespace, propertyName));
    }

    @Test
    void shouldThrowIfNamespaceIsUnknown_qname()
    {
        // given
        QName property = QName.newTransientInstance("http://www.random_namespace.org", "someProperty");

        // then
        assertThrows(BulkIngesterRuntimeException.class, () -> namespacePrefixMapper.toPrefixedName(property));
    }

    @Test
    void shouldThrowIfNamespaceIsUnknown_propertyKey()
    {
        // given
        PropertyKey propertyKey = new PropertyKey();
        propertyKey.setUri("http://www.random_namespace.org");
        propertyKey.setLocalName("someProperty");

        // then
        assertThrows(BulkIngesterRuntimeException.class, () -> namespacePrefixMapper.toPrefixedName(propertyKey));
    }
}
