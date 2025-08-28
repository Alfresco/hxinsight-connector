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
package org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.property;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.AuthorityInfo;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.PermissionsProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.property.PropertyResolver;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PermissionsMetadataUpdated extends PropertyDelta<List<AuthorityInfo>> {

    private final List<AuthorityInfo> allowAccess;
    private final List<AuthorityInfo> denyAccess;

    public PermissionsMetadataUpdated(String propertyName, List<AuthorityInfo> allowAccess, List<AuthorityInfo> denyAccess) {
        super(propertyName);
        this.allowAccess = allowAccess;
        this.denyAccess = denyAccess;
    }

    public List<AuthorityInfo> getAllowAccess() {
        return allowAccess;
    }

    public List<AuthorityInfo> getDenyAccess() {
        return denyAccess;
    }

    @Override
    public void applyOn(UpdateNodeEvent event) {
        event.addPermissionsInstruction(new PermissionsProperty(getPropertyName(), allowAccess, denyAccess));
    }

    @Override
    public <R> Optional<PropertyDelta<R>> resolveWith(PropertyResolver<R> resolver) {
        return Optional.empty();
    }

    public static PermissionsMetadataUpdatedBuilder builder(String propertyName) {
        return new PermissionsMetadataUpdatedBuilder(propertyName);
    }

    public static class PermissionsMetadataUpdatedBuilder {
        private final String propertyName;
        private List<AuthorityInfo> allowAccess;
        private List<AuthorityInfo> denyAccess;

        public PermissionsMetadataUpdatedBuilder(String propertyName) {
            this.propertyName = propertyName;
        }

        public PermissionsMetadataUpdatedBuilder read(List<AuthorityInfo> allowAccess) {
            this.allowAccess = allowAccess;
            return this;
        }

        public PermissionsMetadataUpdatedBuilder deny(List<AuthorityInfo> denyAccess) {
            this.denyAccess = denyAccess;
            return this;
        }

        public PermissionsMetadataUpdated build() {
            return new PermissionsMetadataUpdated(propertyName, allowAccess, denyAccess);
        }
    }
}
