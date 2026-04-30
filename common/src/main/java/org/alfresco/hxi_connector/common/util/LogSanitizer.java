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
package org.alfresco.hxi_connector.common.util;

import static lombok.AccessLevel.PRIVATE;

import lombok.NoArgsConstructor;

/**
 * Utility for neutralizing untrusted data before it is written to log output, mitigating CWE-117
 * (Improper Output Neutralization for Logs / log forging).
 * <p>
 * Implementation follows Veracode's published guidance for CWE-117
 * (<a href="https://community.veracode.com/s/article/How-to-Fix-CWE-117-Improper-Output-Neutralization-for-Logs">"How to Fix CWE-117"</a>):
 * <ul>
 *   <li>Sanitize at the application code level - logging-framework configuration alone (e.g.
 *       Logback {@code %replace}) is not detected by static analysis and will not clear the flaw.</li>
 *   <li>Strip carriage-return / line-feed and other control characters that could be used to forge
 *       new log entries. We use {@link String#replaceAll(String, String)} - one of the
 *       neutralizer patterns the Veracode engine recognises in its taint-flow analysis.</li>
 * </ul>
 * Apply this at the trust boundary (where untrusted data first enters the application) so every
 * downstream log statement is automatically safe.
 */
@NoArgsConstructor(access = PRIVATE)
public final class LogSanitizer
{
    private static final String CONTROL_CHAR_REGEX = "\\p{Cntrl}";
    private static final String REPLACEMENT = "_";

    public static String sanitize(String value)
    {
        if (value == null || value.isEmpty())
        {
            return value;
        }
        return value.replaceAll(CONTROL_CHAR_REGEX, REPLACEMENT);
    }

    public static String sanitize(Object value)
    {
        return value == null ? null : sanitize(value.toString());
    }
}
