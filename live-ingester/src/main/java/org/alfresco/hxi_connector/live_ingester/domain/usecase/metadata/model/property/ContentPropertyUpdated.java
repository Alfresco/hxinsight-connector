/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2024 Alfresco Software Limited
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

import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.ContentProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.PropertyDelta;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.property.PropertyResolver;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ContentPropertyUpdated extends PropertyDelta<String>
{
    private String id;
    private String mimeType;
    private String sourceMimeType;
    private Long sourceSizeInBytes;
    private String sourceFileName;

    public ContentPropertyUpdated(String propertyName, String id, String mimeType, String sourceMimeType, Long sourceSizeInBytes, String sourceFileName)
    {
        super(propertyName);
        this.id = id;
        this.mimeType = mimeType;
        this.sourceMimeType = sourceMimeType;
        this.sourceSizeInBytes = sourceSizeInBytes;
        this.sourceFileName = sourceFileName;
    }

    @Override
    public void applyOn(UpdateNodeEvent event)
    {
        ContentProperty contentProperty = new ContentProperty(getPropertyName(), id, mimeType, sourceMimeType, sourceSizeInBytes, sourceFileName);
        event.addContentInstruction(contentProperty);
    }

    @Override
    public <R> Optional<PropertyDelta<R>> resolveWith(PropertyResolver<R> resolver)
    {
        return Optional.empty();
    }

    public static ContentPropertyUpdatedBuilder builder(String propertyName)
    {
        return new ContentPropertyUpdatedBuilder(propertyName);
    }

    @RequiredArgsConstructor
    public static class ContentPropertyUpdatedBuilder
    {
        private final String propertyName;
        private String id;
        private String mimeType;
        private String sourceMimeType;
        private Long sourceSizeInBytes;
        private String sourceFileName;

        public ContentPropertyUpdatedBuilder id(String id)
        {
            this.id = id;
            return this;
        }

        public ContentPropertyUpdatedBuilder mimeType(String mimeType)
        {
            this.mimeType = mimeType;
            return this;
        }

        public ContentPropertyUpdatedBuilder sourceMimeType(String sourceMimeType)
        {
            this.sourceMimeType = sourceMimeType;
            return this;
        }

        public ContentPropertyUpdatedBuilder sourceSizeInBytes(Long sourceSizeInBytes)
        {
            this.sourceSizeInBytes = sourceSizeInBytes;
            return this;
        }

        public ContentPropertyUpdatedBuilder sourceFileName(String sourceFileName)
        {
            this.sourceFileName = sourceFileName;
            return this;
        }

        public ContentPropertyUpdated build()
        {
            return new ContentPropertyUpdated(propertyName, id, mimeType, sourceMimeType, sourceSizeInBytes, sourceFileName);
        }
    }
}
