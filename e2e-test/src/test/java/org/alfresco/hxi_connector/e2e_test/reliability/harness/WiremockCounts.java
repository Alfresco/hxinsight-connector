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
package org.alfresco.hxi_connector.e2e_test.reliability.harness;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import lombok.experimental.UtilityClass;

/**
 * Typed wrappers around Wiremock's {@code findAll} for the HX Insight endpoints exercised by the connector. Returning plain {@code int} counts keeps reliability assertions terse and grep-friendly:
 *
 * <pre>{@code
 * assertThat(WiremockCounts.ingestionEventsFor(nodeId)).isGreaterThanOrEqualTo(2);
 * }</pre>
 *
 * <p>
 * Assumes a global {@code WireMock.configureFor(...)} has been performed by the test environment (matches the existing pattern in {@code ReliabilityEnvironment#start}).
 */
@UtilityClass
public final class WiremockCounts
{
    public static final String PRESIGNED_URLS_PATH = "/presigned-urls";
    public static final String INGESTION_EVENTS_PATH = "/ingestion-events";
    public static final String SYSTEM_ID = "-dummy-system-id";
    public static final String USER_MAPPINGS_PATH = "/system-integrations/systems/" + SYSTEM_ID + "/user-mappings";
    public static final String GROUPS_PATH = "/system-integrations/systems/" + SYSTEM_ID + "/groups";
    public static final String GROUP_MEMBERS_PATH = "/system-integrations/systems/" + SYSTEM_ID + "/group-members";

    public static int presignedUrlRequests()
    {
        return findAll(postRequestedFor(urlEqualTo(PRESIGNED_URLS_PATH))).size();
    }

    public static int ingestionEvents()
    {
        return findAll(postRequestedFor(urlEqualTo(INGESTION_EVENTS_PATH))).size();
    }

    public static int userMappingRequests()
    {
        return findAll(postRequestedFor(urlEqualTo(USER_MAPPINGS_PATH))).size();
    }

    public static int groupMappingRequests()
    {
        return findAll(postRequestedFor(urlEqualTo(GROUPS_PATH))).size();
    }

    public static int groupMembersRequests()
    {
        return findAll(postRequestedFor(urlEqualTo(GROUP_MEMBERS_PATH))).size();
    }

    public static int ingestionEventsFor(String objectId)
    {
        String objectIdMarker = "\"objectId\":\"" + objectId + "\"";
        return findAll(postRequestedFor(urlEqualTo(INGESTION_EVENTS_PATH))
                .withRequestBody(containing(objectIdMarker))).size();
    }

    /**
     * Number of {@code POST /ingestion-events} requests for the given node that carry the post-rendition content envelope ({@code cm:content.file.id}). The connector emits this exactly when a rendition has been uploaded successfully — so a zero here pins "no content event ever fired" regardless of how many metadata events ACS pushed (Created, Updated, …) for the same node. Use to distinguish "rendition path landed" from "rendition path skipped or silently dropped".
     */
    public static int contentEventsFor(String objectId)
    {
        String objectIdMarker = "\"objectId\":\"" + objectId + "\"";
        String contentFileIdMarker = "\"cm:content\":{\"file\":{\"id\":";
        return findAll(postRequestedFor(urlEqualTo(INGESTION_EVENTS_PATH))
                .withRequestBody(containing(objectIdMarker))
                .withRequestBody(containing(contentFileIdMarker))).size();
    }
}
