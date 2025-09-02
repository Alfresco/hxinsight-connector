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
package org.alfresco.hxi_connector.nucleus_sync.services.sync.orchestration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.alfresco.hxi_connector.nucleus_sync.client.AlfrescoClient;
import org.alfresco.hxi_connector.nucleus_sync.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_sync.dto.AlfrescoGroup;
import org.alfresco.hxi_connector.nucleus_sync.dto.AlfrescoUser;
import org.alfresco.hxi_connector.nucleus_sync.dto.IamUser;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMembershipOutput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupOutput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusUserMappingOutput;
import org.alfresco.hxi_connector.nucleus_sync.entity.GroupMapping;
import org.alfresco.hxi_connector.nucleus_sync.entity.UserMapping;
import org.alfresco.hxi_connector.nucleus_sync.services.domain.GroupSyncService;
import org.alfresco.hxi_connector.nucleus_sync.services.domain.UserSyncService;
import org.alfresco.hxi_connector.nucleus_sync.services.sync.cache.UserGroupMembershipCacheBuilderService;
import org.alfresco.hxi_connector.nucleus_sync.services.sync.processors.GroupMappingSyncProcessor;
import org.alfresco.hxi_connector.nucleus_sync.services.sync.processors.UserGroupMembershipSyncProcessor;
import org.alfresco.hxi_connector.nucleus_sync.services.sync.processors.UserMappingSyncProcessor;

@Service
@RequiredArgsConstructor
public class SyncOrchestrationService
{
    private final AlfrescoClient alfrescoClient;
    private final NucleusClient nucleusClient;
    private final UserSyncService userSyncService;
    private final GroupSyncService groupSyncService;
    private final UserGroupMembershipCacheBuilderService cacheBuilderService;
    private final UserMappingSyncProcessor userMappingSyncProcessor;
    private final GroupMappingSyncProcessor groupMappingSyncProcessor;
    private final UserGroupMembershipSyncProcessor userGroupMembershipSyncProcessor;

    private static final Logger logger = LoggerFactory.getLogger(SyncOrchestrationService.class);
    private final AtomicBoolean isSyncInProgress = new AtomicBoolean(false);
    private LocalDateTime lastSyncTime = LocalDateTime.MIN;
    private String lastSyncStatus;
    private LocalDateTime lastAlfrescoSync = LocalDateTime.MIN;
    private LocalDateTime lastNucleusSync = LocalDateTime.MIN;
    private String alfrescoStatus = "UNKNOWN";
    private String nucleusStatus = "UNKNOWN";

    public Map<String, Object> getSyncStatus()
    {
        return Map.of(
                "syncInProgress", isSyncInProgress,
                "lastSyncTime", lastSyncTime,
                "lastSyncResult", lastSyncStatus != null ? lastSyncStatus : "Never synced",
                "alfrescoStatus", alfrescoStatus,
                "nucleusStatus", nucleusStatus,
                "lastAlfrescoSync", lastAlfrescoSync,
                "lastNucleusSync", lastNucleusSync);
    }

    public String performFullSync()
    {
        if (!isSyncInProgress.compareAndSet(false, true))
        {
            return "Sync already in progress. Please wait for the current sync to complete.";
        }

        lastSyncTime = LocalDateTime.now();

        try
        {
            logger.info("Sync starting at: {}", lastSyncTime);
            String syncResult = executeSync();
            lastSyncStatus = "SUCCESS: " + syncResult;
            logger.info("Sync complete at {}.", LocalDateTime.now());
            return lastSyncStatus;
        }
        catch (Exception e)
        {
            lastSyncStatus = "FAILED: " + e.getMessage();
            logger.error("Sync failed: {}", e.getMessage(), e);
            throw new RuntimeException("Sync failed", e);
        }
        finally
        {
            isSyncInProgress.set(false);
        }
    }

    private String executeSync()
    {
        StringBuilder result = new StringBuilder();

        // Check health and get data with fallbacks
        SystemData systemData = getSystemData(result);

        if (!systemData.alfrescoAvailable && !systemData.nucleusAvailable)
        {
            return "Both Alfresco and Nucleus are unavailable. Sync aborted. " + result.toString();
        }

        // Local Database - always available
        List<UserMapping> localUserMappings = userSyncService.getAllUserMappings();
        List<GroupMapping> localGroupMappings = groupSyncService.getAllActiveGroups();
        logger.debug("{} local user mappings found.", localUserMappings.size());
        logger.debug("{} local group mappings found.", localGroupMappings.size());

        // Sync Users (if both systems are available)
        List<UserMapping> updatedUserMappings = localUserMappings;
        if (systemData.alfrescoAvailable && systemData.nucleusAvailable)
        {
            try
            {
                updatedUserMappings = userMappingSyncProcessor.syncUserMappings(
                        systemData.alfrescoUsers,
                        systemData.nucleusIamUsers,
                        systemData.currentUserMappings,
                        localUserMappings);
                result.append("User sync: SUCCESS. ");
                logger.info("User sync completed successfully.");
            }
            catch (Exception e)
            {
                result.append("User sync: FAILED. ");
                logger.error("User sync failed: {}", e.getMessage(), e);
            }
        }
        else
        {
            result.append("User sync: SKIPPED due to unavailable system. ");
            logger.warn("User sync skipped due to unavailable system.");
        }

        // Build user group membership cache
        Map<String, List<String>> userGroupMembershipCache = null;
        if (systemData.alfrescoAvailable)
        {
            try
            {
                userGroupMembershipCache = cacheBuilderService.buildCacheFromAlfresco(updatedUserMappings);
                result.append("Fresh cache build: SUCCESS. ");
                logger.debug("Fresh user-group membership cache built successfully.");
            }
            catch (Exception e)
            {
                logger.error(
                        "Failed to build user group memberships cache from Alfresco: {}",
                        e.getMessage());
                userGroupMembershipCache = cacheBuilderService.buildCacheFromLocalState(updatedUserMappings);
                result.append("Cache: LOCAL. ");
            }
        }
        else
        {
            userGroupMembershipCache = cacheBuilderService.buildCacheFromLocalState(updatedUserMappings);
            result.append("Cache: LOCAL (Alfresco not available). ");
            logger.debug("Built user group memberships cache from local database state");
        }

        // Sync Groups
        List<GroupMapping> updatedGroupMappings = localGroupMappings;
        if (systemData.alfrescoAvailable)
        {
            try
            {
                updatedGroupMappings = groupMappingSyncProcessor.syncGroupMappings(
                        systemData.alfrescoGroups,
                        systemData.nucleusAvailable
                                ? systemData.currentNucleusGroups
                                : new ArrayList<>(),
                        localGroupMappings,
                        userGroupMembershipCache);
                result.append("Group sync: SUCCESS. ");
                logger.info("Group sync completed successfully.");
            }
            catch (Exception e)
            {
                result.append("Group sync: FAILED. ");
                logger.error("Group sync failed: {}", e.getMessage(), e);
            }
        }
        else if (systemData.nucleusAvailable)
        {
            try
            {
                updatedGroupMappings = groupMappingSyncProcessor.performNucleusOnlyGroupCleanup(
                        localGroupMappings, systemData.currentNucleusGroups);
                logger.debug("Nucleus-only group cleanup complete.");
                result.append("Group cleanup: SUCCESS. ");
            }
            catch (Exception e)
            {
                result.append("Group sync: FAILED. ");
                logger.error("Nucleus group cleanup failed: {}", e.getMessage(), e);
            }
        }
        else
        {
            result.append("Group sync: SKIPPED (both systems unavailable). ");
            logger.info(
                    "Using existing {} group mappings from local database",
                    localGroupMappings.size());
        }

        // Sync User-Group Memberships
        if (userGroupMembershipCache != null)
        {
            if (systemData.nucleusAvailable)
            {
                try
                {
                    userGroupMembershipSyncProcessor.syncUserGroupMemberships(
                            systemData.alfrescoUsers,
                            updatedUserMappings,
                            updatedGroupMappings,
                            systemData.currentMemberships,
                            userGroupMembershipCache);
                    result.append("User-Group Membership sync: SUCCESS. ");
                    logger.info("User-Group Membership sync completed successfully.");
                }
                catch (Exception e)
                {
                    result.append("User-Group Membership sync: FAILED. ");
                    logger.error("User-Group Membership sync failed: {}", e.getMessage(), e);
                }
            }
            else
            {
                try
                {
                    userGroupMembershipSyncProcessor.performLocalOnlyMembershipOperations(
                            updatedUserMappings, updatedGroupMappings, userGroupMembershipCache);
                    result.append("Local membership update: SUCCESS. ");
                    logger.debug("Local-only membership operations complete.");
                }
                catch (Exception e)
                {
                    logger.error("Local membership operations failed: {}", e.getMessage());
                    result.append("Local membership update: FAILED. ");
                }
            }
        }
        else
        {
            result.append("Membership sync: SKIPPED (no cache available). ");
            logger.warn("User-Group Membership sync skipped (no cache available).");
        }

        return result.toString().trim();
    }

    private SystemData getSystemData(StringBuilder result)
    {
        SystemData data = new SystemData();

        // Try Alfresco
        try
        {
            data.alfrescoUsers = alfrescoClient.getAllUsers();
            data.alfrescoGroups = alfrescoClient.getAllGroups();
            data.alfrescoAvailable = true;
            alfrescoStatus = "HEALTHY";
            lastAlfrescoSync = LocalDateTime.now();
            result.append("Alfresco: SUCCESS. ");
            logger.debug("Found {} alfresco users.", data.alfrescoUsers.size());
            logger.debug("Found {} alfresco groups.", data.alfrescoGroups.size());
        }
        catch (Exception e)
        {
            data.alfrescoAvailable = false;
            alfrescoStatus = "UNAVAILABLE: " + e.getMessage();
            data.alfrescoUsers = new ArrayList<>();
            data.alfrescoGroups = new ArrayList<>();
            result.append("Alfresco: FAILED. ");
            logger.error("Alfresco unavailable: {}", e.getMessage());
        }

        // Try Nucleus
        try
        {
            data.nucleusIamUsers = nucleusClient.getAllIamUsers();
            data.currentUserMappings = nucleusClient.getCurrentUserMappings();
            data.currentNucleusGroups = nucleusClient.getAllExternalGroups();
            data.currentMemberships = nucleusClient.getCurrentGroupMemberships();
            data.nucleusAvailable = true;
            nucleusStatus = "HEALTHY";
            lastNucleusSync = LocalDateTime.now();
            result.append("Nucleus: SUCCESS. ");
            logger.debug("Found {} IAM users.", data.nucleusIamUsers.size());
            logger.debug(
                    "Found {} user mappings for alfresco from nucleus.",
                    data.currentUserMappings.size());
            logger.debug(
                    "Found {} alfresco groups from nucleus.", data.currentNucleusGroups.size());
            logger.debug("Current group memberships info obtained.");
        }
        catch (Exception e)
        {
            data.nucleusAvailable = false;
            nucleusStatus = "UNAVAILABLE: " + e.getMessage();
            data.nucleusIamUsers = new ArrayList<>();
            data.currentUserMappings = new ArrayList<>();
            data.currentNucleusGroups = new ArrayList<>();
            data.currentMemberships = new ArrayList<>();
            result.append("Nucleus: FAILED.");
            logger.error("Nucleus unavailable: {}", e.getMessage());
        }

        return data;
    }

    private static class SystemData
    {
        boolean alfrescoAvailable = false;
        boolean nucleusAvailable = false;
        List<AlfrescoUser> alfrescoUsers = new ArrayList<>();
        List<AlfrescoGroup> alfrescoGroups = new ArrayList<>();
        List<IamUser> nucleusIamUsers = new ArrayList<>();
        List<NucleusUserMappingOutput> currentUserMappings = new ArrayList<>();
        List<NucleusGroupOutput> currentNucleusGroups = new ArrayList<>();
        List<NucleusGroupMembershipOutput> currentMemberships = new ArrayList<>();
    }
}
