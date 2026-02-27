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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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
import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.AlfrescoUnavailableException;
import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.SyncException;
import org.alfresco.hxi_connector.nucleus_sync.services.orchestration.exceptions.SyncInProgressException;
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
    private final UserGroupMembershipService userGrpMembershipService;
    private final UserMappingSyncProcessor userMappingSyncProcessor;
    private final GroupMappingSyncProcessor groupMappingSyncProcessor;
    private final UserGroupMembershipSyncProcessor userGroupMembershipSyncProcessor;

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncOrchestrationService.class);

    private final AtomicBoolean isSyncInProgress = new AtomicBoolean(false);
    private volatile SyncStatus lastSyncStatus = SyncStatus.neverRun();

    public Map<String, Object> getSyncStatus()
    {
        return Map.of(
                "syncInProgress", isSyncInProgress.get(),
                "lastSyncTime", lastSyncStatus.syncTime(),
                "lastSyncResult", lastSyncStatus.result(),
                "alfrescoStatus", lastSyncStatus.alfrescoHealth(),
                "nucleusStatus", lastSyncStatus.nucleusHealth());
    }

    public String performFullSync()
    {
        if (!isSyncInProgress.compareAndSet(false, true))
        {
            throw new SyncInProgressException();
        }

        try
        {
            LOGGER.info("Sync starting ...");
            SyncResult syncResult = executeSync();
            lastSyncStatus = syncResult.toStatus();
            LOGGER.atInfo()
                    .setMessage("Sync complete: {}.")
                    .addArgument(syncResult.summary())
                    .log();
            return syncResult.summary();
        }
        catch (SyncException e)
        {
            lastSyncStatus = SyncStatus.failed(e.getMessage(), lastSyncStatus);
            LOGGER.atError()
                    .setMessage("Sync failed: {}")
                    .addArgument(e.getMessage())
                    .setCause(e)
                    .log();
            throw e;
        }
        finally
        {
            isSyncInProgress.set(false);
        }
    }

    private SyncResult executeSync()
    {
        LocalDateTime syncStartTime = LocalDateTime.now();

        // Identify Alfresco users with non-unique emails
        Set<String> unsyncableAlfrescoUserIds = findUnsyncableAlfrescoUserIds();

        // Load the data
        List<AlfrescoUser> alfrescoUsers = loadAlfrescoUsers();
        List<IamUser> nucleusIamUsers = loadNucleusIamUsers();
        List<NucleusUserMappingOutput> currentUserMappings = loadNucleusUserMappings();
        List<NucleusGroupOutput> currentNucleusGroups = loadNucleusGroups();
        List<NucleusGroupMembershipOutput> currentMemberships = loadNucleusMemberships();

        // Sync Users
        List<UserMapping> userMappings = userMappingSyncProcessor.syncUserMappings(
                alfrescoUsers,
                nucleusIamUsers,
                currentUserMappings,
                unsyncableAlfrescoUserIds);
        LOGGER.info("User sync completed successfully.");

        // Build user group membership
        Map<String, List<String>> userGroupMemberships = userGrpMembershipService
                .buildUserGroupMemberships(userMappings);
        LOGGER.debug("Fresh user-group membership cache built successfully.");

        // Sync Groups
        groupMappingSyncProcessor.syncGroupMappings(currentNucleusGroups, userGroupMemberships);
        LOGGER.info("Group sync completed successfully.");

        // Sync User-Group Memberships
        userGroupMembershipSyncProcessor.syncUserGroupMemberships(
                userMappings,
                currentMemberships,
                userGroupMemberships);
        LOGGER.info("User-Group Membership sync completed successfully.");

        return SyncResult.success("HEALTHY", "HEALTHY", syncStartTime);
    }

    private Set<String> findUnsyncableAlfrescoUserIds()
    {
        List<AlfrescoUser> alfrescoUsers = loadAlfrescoUsers();

        Set<String> unsyncableAlfrescoUserIds = alfrescoUsers.stream()
                .filter(u -> u.email() != null && !u.email().isEmpty())
                .collect(groupingBy(AlfrescoUser::email, toSet()))
                .values().stream()
                .filter(users -> users.size() > 1)
                .flatMap(Set::stream)
                .map(AlfrescoUser::id)
                .collect(toSet());

        if (!unsyncableAlfrescoUserIds.isEmpty())
        {
            LOGGER.atWarn()
                    .setMessage("Skipping Alfresco users with non-unique email addresses: {}")
                    .addArgument(() -> String.join(", ", unsyncableAlfrescoUserIds))
                    .log();
        }

        return unsyncableAlfrescoUserIds;
    }

    private List<AlfrescoUser> loadAlfrescoUsers()
    {
        try
        {
            List<AlfrescoUser> users = alfrescoClient.getAllUsers();
            LOGGER.atDebug()
                    .setMessage("Found {} alfresco users.")
                    .addArgument(users.size())
                    .log();
            return users;
        }
        catch (Exception e)
        {
            LOGGER.atError()
                    .setMessage("Alfresco unavailable: {}")
                    .addArgument(e.getMessage())
                    .setCause(e)
                    .log();
            throw new AlfrescoUnavailableException(e.getMessage(), e);
        }
    }

    private List<IamUser> loadNucleusIamUsers()
    {
        List<IamUser> users = nucleusClient.getAllIamUsers();
        LOGGER.atDebug()
                .setMessage("Found {} IAM users.")
                .addArgument(users.size())
                .log();
        return users;
    }

    private List<NucleusUserMappingOutput> loadNucleusUserMappings()
    {
        List<NucleusUserMappingOutput> mappings = nucleusClient.getCurrentUserMappings();
        LOGGER.atDebug()
                .setMessage("Found {} user mappings.")
                .addArgument(mappings.size())
                .log();
        return mappings;
    }

    private List<NucleusGroupOutput> loadNucleusGroups()
    {
        List<NucleusGroupOutput> groups = nucleusClient.getAllExternalGroups();
        LOGGER.atDebug()
                .setMessage("Found {} alfresco groups.")
                .addArgument(groups.size())
                .log();
        return groups;
    }

    private List<NucleusGroupMembershipOutput> loadNucleusMemberships()
    {
        List<NucleusGroupMembershipOutput> memberships = nucleusClient.getCurrentGroupMemberships();
        LOGGER.atDebug()
                .setMessage("Found {} memberships.")
                .addArgument(memberships.size())
                .log();
        return memberships;
    }

    record SyncResult(
            boolean success, String message, String alfrescoHealth,
            String nucleusHealth, LocalDateTime checkTime)
    {
        static SyncResult success(String alfrescoHealth, String nucleusHealth,
                LocalDateTime checkTime)
        {
            return new SyncResult(true, "Sync completed successfully",
                    alfrescoHealth, nucleusHealth, checkTime);
        }

        String summary()
        {
            return message;
        }

        SyncStatus toStatus()
        {
            return new SyncStatus(
                    LocalDateTime.now(),
                    message,
                    alfrescoHealth,
                    nucleusHealth);
        }
    }

    record SyncStatus(
            LocalDateTime syncTime,
            String result,
            String alfrescoHealth,
            String nucleusHealth)
    {
        static SyncStatus neverRun()
        {
            return new SyncStatus(
                    LocalDateTime.MIN,
                    "Never Synced",
                    "UNKNOWN",
                    "UNKNOWN");
        }

        static SyncStatus failed(String error, SyncStatus previous)
        {
            return new SyncStatus(
                    LocalDateTime.now(),
                    "Failed: " + error,
                    previous.alfrescoHealth,
                    previous.nucleusHealth);
        }
    }
}
