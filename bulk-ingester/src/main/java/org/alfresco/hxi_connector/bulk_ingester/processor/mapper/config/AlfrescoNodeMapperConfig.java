/*
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

package org.alfresco.hxi_connector.bulk_ingester.processor.mapper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.alfresco.hxi_connector.bulk_ingester.processor.mapper.AlfrescoPropertyMapper;
import org.alfresco.hxi_connector.bulk_ingester.processor.mapper.AlfrescoPropertyMapperFactory;
import org.alfresco.hxi_connector.bulk_ingester.processor.mapper.NamespacePrefixMapper;
import org.alfresco.hxi_connector.bulk_ingester.processor.mapper.TimeProvider;

@Configuration
public class AlfrescoNodeMapperConfig
{

    @Bean
    public AlfrescoPropertyMapperFactory alfrescoPropertyMapperFactory(NamespacePrefixMapper namespacePrefixMapper)
    {
        return (node, propertyName) -> new AlfrescoPropertyMapper(namespacePrefixMapper, node, propertyName);
    }

    @Bean
    public TimeProvider timeProvider()
    {
        return System::currentTimeMillis;
    }
}
