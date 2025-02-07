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

import static lombok.AccessLevel.PRIVATE;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
@SuppressWarnings({"PMD.PrematureDeclaration", "PMD.SimplifyBooleanReturns"})
public final class ContentUtils
{
    public static String generateDigestIdentifier(DigestIdentifierParams params)
    {
        String input = params.getNodeId() + "-" + params.getPropertyName() + "-" + params.getVersionNumber();
        try
        {
            MessageDigest digest = MessageDigest.getInstance(params.getDigestAlgorithm());
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalArgumentException("Invalid digest identifier algorithm: " + params.getDigestAlgorithm(), e);
        }
    }
}
