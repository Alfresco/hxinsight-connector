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

package org.alfresco.hxi_connector.bulk_ingester.util;

import static lombok.AccessLevel.PRIVATE;
import static org.skyscreamer.jsonassert.JSONCompareMode.LENIENT;

import lombok.NoArgsConstructor;
import org.skyscreamer.jsonassert.JSONAssert;

@NoArgsConstructor(access = PRIVATE)
public class JsonEqualityUtils
{
    public static boolean jsonEquals(String expected, String actual)
    {
        try
        {
            JSONAssert.assertEquals(expected, actual, LENIENT);

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
