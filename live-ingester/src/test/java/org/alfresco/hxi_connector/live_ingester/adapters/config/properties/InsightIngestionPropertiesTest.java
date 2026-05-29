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

package org.alfresco.hxi_connector.live_ingester.adapters.config.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InsightIngestionPropertiesTest
{
    @Test
    void givenNoPresignedUrlsCountDefaultsToOne()
    {
        Insight.Ingestion ingestion = new Insight.Ingestion("http://localhost:8001", null, null);

        assertEquals(1, ingestion.presignedUrlsCount());
    }

    @Test
    void givenPresignedUrlsCountUsesProvidedValue()
    {
        Insight.Ingestion ingestion = new Insight.Ingestion("http://localhost:8001", null, 100);

        assertEquals(100, ingestion.presignedUrlsCount());
    }
}
