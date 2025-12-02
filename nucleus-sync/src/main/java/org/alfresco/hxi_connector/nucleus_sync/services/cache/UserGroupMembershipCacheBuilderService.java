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
package org.alfresco.hxi_connector.nucleus_sync.services.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.alfresco.hxi_connector.nucleus_sync.client.AlfrescoClient;
import org.alfresco.hxi_connector.nucleus_sync.model.UserMapping;

@Service
@RequiredArgsConstructor
public class UserGroupMembershipCacheBuilderService
{
    private final AlfrescoClient alfrescoClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserGroupMembershipCacheBuilderService.class);

    /**
     * Creates a 'cache' of users and their corresponding groups from alfresco.
     *
     * @param localUserMappings
     *            the List of user mappings
     * @return a map of alfresco user id and it's corresponding group ids
     * @throws RuntimeException
     *             if any user's group fetch fails, or if timeout occurs
     */
    public Map<String, List<String>> buildCacheFromAlfresco(List<UserMapping> localUserMappings)
    {
        LOGGER.atInfo()
                .setMessage("Building user-group membership cache for {} users")
                .addArgument(localUserMappings.size())
                .log();

        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        try
        {
            Map<String, CompletableFuture<List<String>>> futures = localUserMappings.stream()
                    .collect(Collectors.toMap(
                            UserMapping::alfrescoUserId,
                            userMapping -> CompletableFuture.supplyAsync(() -> {
                                try
                                {
                                    return alfrescoClient.getUserGroups(userMapping.alfrescoUserId());
                                }
                                catch (Exception e)
                                {
                                    LOGGER.error(
                                            "Failed to get groups for user: {} - {}",
                                            userMapping.alfrescoUserId(),
                                            e.getMessage(),
                                            e);
                                    throw new RuntimeException(
                                            "Failed to fetch groups for user: " + userMapping.alfrescoUserId(), e);
                                }
                            }, executor)));

            Map<String, List<String>> cache = new ConcurrentHashMap<>();

            for (Map.Entry<String, CompletableFuture<List<String>>> entry : futures.entrySet())
            {
                cache.put(entry.getKey(), entry.getValue().join());
            }

            LOGGER.atInfo()
                    .setMessage("Successfully built user-group membership cache for {} users")
                    .addArgument(cache.size())
                    .log();

            return cache;
        }
        finally
        {
            executor.shutdown();
        }
    }
}
