/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 Alfresco Software Limited
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
package org.alfresco.hxi_connector.bulk_ingester;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import org.alfresco.elasticsearch.db.connector.config.DatabaseConfig;
import org.alfresco.hxi_connector.bulk_ingester.processor.BulkIngestionProcessor;

@RequiredArgsConstructor
@SpringBootApplication
@ImportAutoConfiguration(DatabaseConfig.class)
@ConfigurationPropertiesScan("org.alfresco.hxi_connector.bulk_ingester")
@SuppressWarnings("PMD.UseUtilityClass")
public class BulkIngesterApplication implements ApplicationRunner
{

    private final BulkIngestionProcessor bulkIngestionProcessor;

    public static void main(String[] args)
    {
        SpringApplication.run(BulkIngesterApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args)
    {
        bulkIngestionProcessor.process();
    }

}
