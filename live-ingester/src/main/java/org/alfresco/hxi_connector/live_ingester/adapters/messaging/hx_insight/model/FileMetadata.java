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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.ContentProperty;

@Getter
@JsonInclude(NON_NULL)
public class FileMetadata
{
    private String id;
    @JsonProperty("content-type")
    private String contentType;
    @JsonProperty("content-metadata")
    private ContentMetadata contentMetadata;

    public FileMetadata(ContentProperty contentProperty)
    {
        id = contentProperty.id();
        contentType = contentProperty.mimeType();
        if (contentProperty.sourceMimeType() != null || contentProperty.sourceSizeInBytes() != null || contentProperty.sourceFileName() != null)
        {
            contentMetadata = new ContentMetadata(contentProperty.sourceMimeType(), contentProperty.sourceSizeInBytes(), contentProperty.sourceFileName());
        }
    }
}
