/*-
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

package org.alfresco.hxi_connector.nucleus_sync.client;

/**
 * Inventory Provides Metrices for the Nucleus Sync with all the counter names and their descriptions.
 */
public final class NucleusSyncMetrices
{

    private NucleusSyncMetrices()
    {}

    /** Common tag keys used across client failure counters. */
    public static final class Tags
    {
        /** Logical operation that failed, e.g. "getAllUsers", "createGroups". */
        public static final String OPERATION = "operation";
        /** HTTP method involved, e.g. "GET", "POST", "DELETE". */
        public static final String METHOD = "method";
        /**
         * Classification of the failure. One of: server_error, client_error, timeout, network, parsing, serialization, auth, unknown.
         */
        public static final String ERROR_TYPE = "error.type";

        // Error-type values
        public static final String ERR_SERVER = "server_error";
        public static final String ERR_CLIENT = "client_error";
        public static final String ERR_TIMEOUT = "timeout";
        public static final String ERR_NETWORK = "network";
        public static final String ERR_PARSING = "parsing";
        public static final String ERR_SERIALIZATION = "serialization";
        public static final String ERR_AUTH = "auth";
        public static final String ERR_UNKNOWN = "unknown";

        private Tags()
        {}
    }

    // Alfresco Client related metrics
    public static final class AlfrescoMetrices
    {
        public static final String CONNECTION_ISSUE = "alfresco_connection_issue_total";
        public static final String CONNECTION_ISSUE_DESCRIPTION = "Failed requests to Alfresco (tagged by operation, method, error.type)";

        private AlfrescoMetrices()
        {}
    }

    // Nucleus Client Related Metrices
    public static final class NucleusClientMetrics
    {
        public static final String CONNECTION_ISSUE = "nucleus_connection_issue_total";
        public static final String CONNECTION_ISSUE_DESCRIPTION = "Failed requests to Nucleus (tagged by operation, method, error.type)";

        private NucleusClientMetrics()
        {}
    }
}
