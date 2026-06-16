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
package org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.tomakehurst.wiremock.verification.LoggedRequest;

/**
 * Pure-function utility that extracts data from captured WireMock {@link LoggedRequest} bodies. Stateless — all methods are static.
 */
public final class SyncResponseExtractor
{
    private static final Pattern EXTERNAL_USER_ID_PATTERN = Pattern.compile("\"externalUserId\"\\s*:\\s*\"([^\"]+)\"");

    private static final Pattern EXTERNAL_GROUP_ID_PATTERN = Pattern.compile("\"externalGroupId\"\\s*:\\s*\"([^\"]+)\"");

    private static final Pattern MEMBERSHIP_PAIR_PATTERN = Pattern.compile(
            "\\{[^}]*?\"externalGroupId\"\\s*:\\s*\"([^\"]+)\"[^}]*?\"memberExternalUserId\"\\s*:\\s*\"([^\"]+)\"[^}]*?}"
                    + "|"
                    + "\\{[^}]*?\"memberExternalUserId\"\\s*:\\s*\"([^\"]+)\"[^}]*?\"externalGroupId\"\\s*:\\s*\"([^\"]+)\"[^}]*?}");

    private SyncResponseExtractor()
    {}

    /**
     * Extract the Alfresco-side user IDs that were mapped, by parsing the POST bodies sent to {@code /user-mappings}. The Alfresco id lives in {@code externalUserId}.
     */
    public static Set<String> extractMappedUserIds(List<LoggedRequest> requests)
    {
        Set<String> userIds = new HashSet<>();
        for (LoggedRequest request : requests)
        {
            String body = request.getBodyAsString();
            Matcher matcher = EXTERNAL_USER_ID_PATTERN.matcher(body);
            while (matcher.find())
            {
                userIds.add(matcher.group(1));
            }
        }
        return userIds;
    }

    /**
     * Extract the Alfresco-side group IDs that were posted, by parsing the POST bodies sent to {@code /groups}. The Alfresco id lives in {@code externalGroupId}.
     */
    public static Set<String> extractGroupIds(List<LoggedRequest> requests)
    {
        Set<String> groupIds = new HashSet<>();
        for (LoggedRequest request : requests)
        {
            String body = request.getBodyAsString();
            Matcher matcher = EXTERNAL_GROUP_ID_PATTERN.matcher(body);
            while (matcher.find())
            {
                groupIds.add(matcher.group(1));
            }
        }
        return groupIds;
    }

    /**
     * Extract every {@code (externalGroupId, memberExternalUserId)} pair captured in POST {@code /group-members} bodies.
     */
    public static Set<String> extractMemberships(List<LoggedRequest> requests)
    {
        Set<String> pairs = new HashSet<>();
        for (LoggedRequest request : requests)
        {
            String body = request.getBodyAsString();
            Matcher matcher = MEMBERSHIP_PAIR_PATTERN.matcher(body);
            while (matcher.find())
            {
                String groupId = matcher.group(1) != null ? matcher.group(1) : matcher.group(4);
                String userId = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
                pairs.add(membershipKey(groupId, userId));
            }
        }
        return pairs;
    }

    public static String membershipKey(String groupId, String userId)
    {
        return groupId + "::" + userId;
    }
}
