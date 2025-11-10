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
package org.alfresco.hxi_connector.nucleus_sync.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "user_group_memberships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"alfresco_group_id", "alfresco_user_id"}))
@NoArgsConstructor
@Getter
@Setter
public class UserGroupMembership
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String alfrescoGroupId;

    @Column(nullable = false)
    private String alfrescoUserId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDateTime lastSynced;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    private Boolean isActive;

    public UserGroupMembership(
            String alfrescoGroupId,
            String alfrescoUserId,
            String email,
            LocalDateTime lastSynced,
            Boolean isActive)
    {
        this.alfrescoGroupId = alfrescoGroupId;
        this.alfrescoUserId = alfrescoUserId;
        this.email = email;
        this.lastSynced = lastSynced;
        this.isActive = isActive;
    }
}
