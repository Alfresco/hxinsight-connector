/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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
package org.alfresco.hxi_connector.live_ingester.adapters.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.alfresco.hxi_connector.live_ingester.adapters.config.jackson.UpdateNodeMetadataEventSerializer;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeMetadataEvent;
import org.alfresco.repo.event.databind.ObjectMapperFactory;

@Configuration
public class MappingConfig
{

    @Bean
    public ObjectMapper objectMapper()
    {
        ObjectMapper objectMapper = ObjectMapperFactory.createInstance();
        registerCustomSerializers(objectMapper);

        return objectMapper;
    }

    private void registerCustomSerializers(ObjectMapper objectMapper)
    {
        SimpleModule module = new SimpleModule();
        module.addSerializer(UpdateNodeMetadataEvent.class, new UpdateNodeMetadataEventSerializer());
        objectMapper.registerModule(module);
    }
}
