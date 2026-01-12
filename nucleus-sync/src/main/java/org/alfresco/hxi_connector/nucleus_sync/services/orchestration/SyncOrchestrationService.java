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
package org.alfresco.hxi_connector.nucleus_sync.services.orchestration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.alfresco.hxi_connector.nucleus_sync.client.AlfrescoClient;
import org.alfresco.hxi_connector.nucleus_sync.client.NucleusClient;
import org.alfresco.hxi_connector.nucleus_sync.dto.AlfrescoUser;
import org.alfresco.hxi_connector.nucleus_sync.dto.IamUser;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupMembershipOutput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusGroupOutput;
import org.alfresco.hxi_connector.nucleus_sync.dto.NucleusUserMappingOutput;
import org.alfresco.hxi_connector.nucleus_sync.model.UserMapping;
import org.alfresco.hxi_connector.nucleus_sync.services.processors.GroupMappingSyncProcessor;
import org.alfresco.hxi_connector.nucleus_sync.services.processors.UserGroupMembershipSyncProcessor;
import org.alfresco.hxi_connector.nucleus_sync.services.processors.UserMappingSyncProcessor;
import org.alfresco.hxi_connector.nucleus_sync.services.util.UserGroupMembershipService;

@Service
@RequiredArgsConstructor
public class SyncOrchestrationService
{
    private final AlfrescoClient alfrescoClient;
    private final NucleusClient nucleusClient;
    private final UserGroupMembershipService cacheBuilderService;
    private final UserMappingSyncProcessor userMappingSyncProcessor;
    private final GroupMappingSyncProcessor groupMappingSyncProcessor;
    private final UserGroupMembershipSyncProcessor userGroupMembershipSyncProcessor;

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncOrchestrationService.class);

    private final AtomicBoolean isSyncInProgress = new AtomicBoolean(false);

    private final AtomicReference<SyncStatusSnapshot> syncStatusSnapshot = new AtomicReference<>(
            new SyncStatusSnapshot(
                    LocalDateTime.MIN,
                    "Never Synced",
                    "UNKNOWN",
                    "UNKNOWN",
                    LocalDateTime.MIN,
                    LocalDateTime.MIN));

    public Map<String, Object> getSyncStatus()
    {
        SyncStatusSnapshot status = syncStatusSnapshot.get();
        return Map.of(
                "syncInProgress", isSyncInProgress.get(),
                "lastSyncTime", status.lastSyncTime,
                "lastSyncResult", status.lastSyncStatus,
                "alfrescoStatus", status.alfrescoStatus,
                "nucleusStatus", status.nucleusStatus,
                "lastAlfrescoSync", status.lastAlfrescoSync,
                "lastNucleusSync", status.lastNucleusSync);
    }

    public String performFullSync()
    {
        if (!isSyncInProgress.compareAndSet(false, true))
        {
            return "Sync already in progress. Please wait for the current sync to complete.";
        }

        LocalDateTime syncStartTime = LocalDateTime.now();

        try
        {
            LOGGER.atInfo()
                    .setMessage("Sync starting at: {}")
                    .addArgument(syncStartTime)
                    .log();
            String syncResult = executeSync();

            syncStatusSnapshot.updateAndGet(oldStatus -> new SyncStatusSnapshot(
                    syncStartTime,
                    syncResult,
                    oldStatus.alfrescoStatus,
                    oldStatus.nucleusStatus,
                    oldStatus.lastAlfrescoSync,
                    oldStatus.lastNucleusSync));

            LOGGER.atInfo()
                    .setMessage("Sync complete at {}.")
                    .addArgument(LocalDateTime.now())
                    .log();
            return syncResult;
        }
        catch (Exception e)
        {
            syncStatusSnapshot.updateAndGet(oldStatus -> new SyncStatusSnapshot(
                    syncStartTime,
                    "FAILED: " + e.getMessage(),
                    oldStatus.alfrescoStatus,
                    oldStatus.nucleusStatus,
                    oldStatus.lastAlfrescoSync,
                    oldStatus.lastNucleusSync));

            LOGGER.atError()
                    .setMessage("Sync failed: {}")
                    .addArgument(e.getMessage())
                    .setCause(e)
                    .log();
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

        SystemData systemData = getSystemData(result);

        if (!systemData.alfrescoAvailable)
        {
            return "Alfresco unavailable. Sync aborted. " + result.toString();
        }

        if (!systemData.nucleusAvailable)
        {
            return "Nucleus unavailable. Sync aborted. " + result.toString();
        }

        // Sync Users
        List<UserMapping> updatedUserMappings;
        try
        {
            updatedUserMappings = userMappingSyncProcessor.syncUserMappings(
                    systemData.alfrescoUsers,
                    systemData.nucleusIamUsers,
                    systemData.currentUserMappings);
            result.append("User sync: SUCCESS. ");
            LOGGER.atInfo()
                    .setMessage("User sync completed successfully.");
        }
        catch (Exception e)
        {
            result.append("User sync: FAILED. ");
            LOGGER.atError()
                    .setMessage("User sync failed: {}")
                    .addArgument(e.getMessage())
                    .setCause(e)
                    .log();

            return result.toString().trim();
        }

        // Build user group membership cache
        Map<String, List<String>> userGroupMembershipCache;
        try
        {
            userGroupMembershipCache = cacheBuilderService.buildUserGroupMemberships(updatedUserMappings);
            result.append("Fresh cache build: SUCCESS. ");
            LOGGER.atDebug()
                    .setMessage("Fresh user-group membership cache built successfully.");
        }
        catch (Exception e)
        {
            LOGGER.atError()
                    .setMessage("Failed to build user group memberships cache from Alfresco: {}")
                    .addArgument(e.getMessage())
                    .setCause(e)
                    .log();
            result.append("User-Group Cache: FAILED. ");

            return result.toString().trim();
        }

        // Sync Groups
        List<String> updatedGroupMappings;
        try
        {
            updatedGroupMappings = groupMappingSyncProcessor.syncGroupMappings(
                    systemData.currentNucleusGroups,
                    userGroupMembershipCache);
            result.append("Group sync: SUCCESS. ");
            LOGGER.atInfo()
                    .setMessage("Group sync completed successfully.");
        }
        catch (Exception e)
        {
            result.append("Group sync: FAILED. ");
            LOGGER.atError()
                    .setMessage("Group sync failed: {}")
                    .addArgument(e.getMessage())
                    .setCause(e)
                    .log();

            return result.toString().trim();
        }

        // Sync User-Group Memberships
        try
        {
            userGroupMembershipSyncProcessor.syncUserGroupMemberships(
                    updatedUserMappings,
                    updatedGroupMappings,
                    systemData.currentMemberships,
                    userGroupMembershipCache);
            result.append("User-Group Membership sync: SUCCESS. ");
            LOGGER.atInfo()
                    .setMessage("User-Group Membership sync completed successfully.");
        }
        catch (Exception e)
        {
            result.append("User-Group Membership sync: FAILED. ");
            LOGGER.atError()
                    .setMessage("User-Group Membership sync failed: {}")
                    .addArgument(e.getMessage())
                    .setCause(e)
                    .log();
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
            data.alfrescoAvailable = true;
            updateAlfrescoStatus("HEALTHY", LocalDateTime.now());
            result.append("Alfresco: SUCCESS. ");
            LOGGER.atDebug()
                    .setMessage("Found {} alfresco users.")
                    .addArgument(data.alfrescoUsers.size())
                    .log();
        }
        catch (Exception e)
        {
            data.alfrescoAvailable = false;
            data.alfrescoUsers = new ArrayList<>();
            updateAlfrescoStatus("UNAVAILABLE: " + e.getMessage(), LocalDateTime.now());
            result.append("Alfresco: FAILED. ");
            LOGGER.atError()
                    .setMessage("Alfresco unavailable: {}")
                    .addArgument(e.getMessage())
                    .setCause(e)
                    .log();
        }

        // Try Nucleus
        try
        {
            data.nucleusIamUsers = nucleusClient.getAllIamUsers();
            data.currentUserMappings = nucleusClient.getCurrentUserMappings();
            data.currentNucleusGroups = nucleusClient.getAllExternalGroups();
            data.currentMemberships = nucleusClient.getCurrentGroupMemberships();
            data.nucleusAvailable = true;
            updateNucleusStatus("HEALTHY", LocalDateTime.now());
            result.append("Nucleus: SUCCESS. ");
            LOGGER.atDebug()
                    .setMessage("Found {} IAM users.")
                    .addArgument(data.nucleusIamUsers.size())
                    .log();
            LOGGER.atDebug()
                    .setMessage("Found {} user mappings for alfresco from nucleus.")
                    .addArgument(data.currentUserMappings.size())
                    .log();
            LOGGER.atDebug()
                    .setMessage("Found {} alfresco groups from nucleus.")
                    .addArgument(data.currentNucleusGroups.size())
                    .log();
            LOGGER.atDebug()
                    .setMessage("Current group memberships info obtained.");
        }
        catch (Exception e)
        {
            data.nucleusAvailable = false;
            data.nucleusIamUsers = new ArrayList<>();
            data.currentUserMappings = new ArrayList<>();
            data.currentNucleusGroups = new ArrayList<>();
            data.currentMemberships = new ArrayList<>();
            updateNucleusStatus("UNAVAILABLE: " + e.getMessage(), LocalDateTime.now());
            result.append("Nucleus: FAILED.");
            LOGGER.atError()
                    .setMessage("Nucleus unavailable: {}")
                    .addArgument(e.getMessage())
                    .setCause(e)
                    .log();
        }

        return data;
    }

    private void updateAlfrescoStatus(String status, LocalDateTime syncTime)
    {
        syncStatusSnapshot.updateAndGet(current -> new SyncStatusSnapshot(
                current.lastSyncTime,
                current.lastSyncStatus,
                status,
                current.nucleusStatus,
                syncTime,
                current.lastNucleusSync));
    }

    private void updateNucleusStatus(String status, LocalDateTime syncTime)
    {
        syncStatusSnapshot.updateAndGet(current -> new SyncStatusSnapshot(
                current.lastSyncTime,
                current.lastSyncStatus,
                current.alfrescoStatus,
                status,
                current.lastAlfrescoSync,
                syncTime));
    }

    private static class SyncStatusSnapshot
    {
        final LocalDateTime lastSyncTime;
        final String lastSyncStatus;
        final String alfrescoStatus;
        final String nucleusStatus;
        final LocalDateTime lastAlfrescoSync;
        final LocalDateTime lastNucleusSync;

        SyncStatusSnapshot(
                LocalDateTime lastSyncTime,
                String lastSyncStatus,
                String alfrescoStatus,
                String nucleusStatus,
                LocalDateTime lastAlfrescoSync,
                LocalDateTime lastNucleusSync)
        {
            this.lastSyncTime = lastSyncTime;
            this.lastSyncStatus = lastSyncStatus;
            this.alfrescoStatus = alfrescoStatus;
            this.nucleusStatus = nucleusStatus;
            this.lastAlfrescoSync = lastAlfrescoSync;
            this.lastNucleusSync = lastNucleusSync;
        }
    }

    private static class SystemData
    {
        boolean alfrescoAvailable = false;
        boolean nucleusAvailable = false;
        List<AlfrescoUser> alfrescoUsers = new ArrayList<>();
        List<IamUser> nucleusIamUsers = new ArrayList<>();
        List<NucleusUserMappingOutput> currentUserMappings = new ArrayList<>();
        List<NucleusGroupOutput> currentNucleusGroups = new ArrayList<>();
        List<NucleusGroupMembershipOutput> currentMemberships = new ArrayList<>();
    }
}
