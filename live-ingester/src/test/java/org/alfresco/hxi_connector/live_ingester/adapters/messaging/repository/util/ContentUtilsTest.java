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

package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = {ContentUtils.class})
class ContentUtilsTest
{
    @Value("${hyland-experience.storage.digest-algorithm}")
    private String digestAlgorithm;

    @Test
    void shouldThrowExceptionForInvalidAlgorithm()
    {
        // given
        String invalidAlgorithm = "INVALID-11";
        String nodeId = "02acf462-533d-4e1b-9825-05fa934140da";
        String propertyName = "cm:content";
        String versionNumber = "1.0";
        DigestIdentifierParams params = new DigestIdentifierParams(invalidAlgorithm, nodeId, propertyName, versionNumber);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            ContentUtils.generateDigestIdentifier(params);
        });
    }

    @Test
    void shouldGenerateDigestIdentifier()
    {
        // given
        String nodeId = "02acf462-533d-4e1b-9825-05fa934140da";
        String propertyName = "cm:content";
        String versionNumber = "1.0";
        DigestIdentifierParams params = new DigestIdentifierParams(digestAlgorithm, nodeId, propertyName, versionNumber);

        // when
        String generatedDigestIdentifier = ContentUtils.generateDigestIdentifier(params);
        String expectedDigestIdentifier = "AKQ0EIuMVIcdLj9vXd/7YJ7WmX6/lMpFIwuW2VsNo6s=";

        // then
        assertEquals(expectedDigestIdentifier, generatedDigestIdentifier);
    }
}
