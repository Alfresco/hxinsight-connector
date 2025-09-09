/*-
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
package org.alfresco.hxi_connector.nucleus_sync.services.sync.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.alfresco.hxi_connector.nucleus_sync.client.AlfrescoClient;
import org.alfresco.hxi_connector.nucleus_sync.entity.UserGroupMembership;
import org.alfresco.hxi_connector.nucleus_sync.entity.UserMapping;
import org.alfresco.hxi_connector.nucleus_sync.services.domain.UserGroupMembershipService;

@Service
@RequiredArgsConstructor
public class UserGroupMembershipCacheBuilderService
{
    private final AlfrescoClient alfrescoClient;
    private final UserGroupMembershipService userGroupMembershipService;
    private static final Logger logger = LoggerFactory.getLogger(UserGroupMembershipCacheBuilderService.class);

    /**
     * Creates a 'cache' of users and their corresponding groups from alfresco.
     *
     * @param localUserMappings
     *            the List of user mappings
     * @return a map of alfresco user id and it's corresponding group ids
     * @throws Exception
     *             on timeout or if it's not able to fetch from alfresco
     */
    public Map<String, List<String>> buildCacheFromAlfresco(List<UserMapping> localUserMappings)
    {

        Map<String, List<String>> cache = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = localUserMappings.stream()
                .map(
                        userMapping -> CompletableFuture.runAsync(
                                () -> {
                                    try
                                    {
                                        List<String> groups = alfrescoClient.getUserGroups(
                                                userMapping
                                                        .getAlfrescoUserId());
                                        cache.put(
                                                userMapping.getAlfrescoUserId(),
                                                groups);
                                    }
                                    catch (Exception e)
                                    {
                                        logger.warn(
                                                "Failed to get groups for user: {}"
                                                        + " - {}",
                                                userMapping.getAlfrescoUserId(),
                                                e.getMessage());
                                    }
                                }))
                .toList();

        try
        {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(10, TimeUnit.MINUTES);
        }
        catch (Exception e)
        {
            futures.forEach(f -> f.cancel(true));
            logger.error("Timeout or error while building cache from Alfresco: {}", e.getMessage());
        }
        return cache;
    }

    /**
     * Creates a 'cache' of users and their corresponding groups from local db.
     *
     * @param userMappings
     *            the List of user mappings
     * @return a map of alfresco user id and it's corresponding group ids
     */
    public Map<String, List<String>> buildCacheFromLocalState(List<UserMapping> userMappings)
    {
        logger.debug("Building user-group membership cache from local state.");

        List<String> mappedEmails = userMappings.stream().map(UserMapping::getEmail).toList();
        List<UserGroupMembership> localMemberships = userGroupMembershipService.getMembershipsForUser(mappedEmails);

        Map<String, List<String>> cache = new HashMap<>();

        Map<String, List<UserGroupMembership>> membershipByUser = localMemberships.stream()
                .filter(UserGroupMembership::getIsActive)
                .collect(Collectors.groupingBy(UserGroupMembership::getAlfrescoUserId));

        for (UserMapping mapping : userMappings)
        {
            String alfrescoUserId = mapping.getAlfrescoUserId();
            List<UserGroupMembership> memberships = membershipByUser.getOrDefault(alfrescoUserId, List.of());

            List<String> groupIds = memberships.stream().map(UserGroupMembership::getAlfrescoGroupId).toList();

            if (!groupIds.isEmpty())
            {
                cache.put(alfrescoUserId, groupIds);
            }
        }

        logger.debug("Cache built with {} users having group memberships.", cache.size());
        return cache;
    }
}
