/*
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
package org.alfresco.hxi_connector.live_ingester.adapters.config;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.hyland.sdk.cic.http.client.mapper.object.CICBlob;
import org.hyland.sdk.cic.ingest.IngestService;
import org.hyland.sdk.cic.ingest.object.IngestEvent;
import org.hyland.sdk.cic.ingest.object.PreSignedUrl;

import org.alfresco.hxi_connector.common.adapters.messaging.repository.ApplicationInfoProvider;

public class LiveIngestService
{
    private final ApplicationInfoProvider applicationInfoProvider;
    private final AtomicReference<IngestService> delegateRef = new AtomicReference<>();

    public LiveIngestService(ApplicationInfoProvider applicationInfoProvider)
    {
        this.applicationInfoProvider = applicationInfoProvider;
    }

    public void setDelegate(IngestService delegate)
    {
        this.delegateRef.set(delegate);
    }

    private IngestService getDelegate()
    {
        IngestService delegate = delegateRef.get();
        if (delegate == null)
        {
            throw new IllegalStateException("IngestService not yet initialized. ACS may not be ready.");
        }
        return delegate;
    }

    public void ingest(IngestEvent event)
    {
        getDelegate().ingest(event);
    }

    public Optional<PreSignedUrl> uploadBlobIfNeeded(String nodeId, CICBlob blob)
    {
        return getDelegate().uploadBlobIfNeeded(applicationInfoProvider.getSourceId(), nodeId, blob);
    }
}
