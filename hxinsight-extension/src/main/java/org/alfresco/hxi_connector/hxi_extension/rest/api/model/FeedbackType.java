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
package org.alfresco.hxi_connector.hxi_extension.rest.api.model;

import static org.alfresco.hxi_connector.hxi_extension.service.model.FeedbackType.BAD;
import static org.alfresco.hxi_connector.hxi_extension.service.model.FeedbackType.GOOD;

public enum FeedbackType
{
    LIKE(GOOD), DISLIKE(BAD);

    private final org.alfresco.hxi_connector.hxi_extension.service.model.FeedbackType serviceModel;

    FeedbackType(org.alfresco.hxi_connector.hxi_extension.service.model.FeedbackType serviceModel)
    {
        this.serviceModel = serviceModel;
    }

    public org.alfresco.hxi_connector.hxi_extension.service.model.FeedbackType toServiceModel()
    {
        return this.serviceModel;
    }
}
