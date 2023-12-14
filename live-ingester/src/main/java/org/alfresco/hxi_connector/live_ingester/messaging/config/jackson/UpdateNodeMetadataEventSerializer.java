/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 Alfresco Software Limited
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

package org.alfresco.hxi_connector.live_ingester.messaging.config.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.domain.model.out.NodeProperty;
import org.alfresco.hxi_connector.live_ingester.domain.model.out.event.UpdateNodeMetadataEvent;
import org.alfresco.hxi_connector.live_ingester.messaging.config.jackson.exception.JsonSerializationException;

@Component
public class UpdateNodeMetadataEventSerializer extends StdSerializer<UpdateNodeMetadataEvent>
{

    public UpdateNodeMetadataEventSerializer()
    {
        this(null);
    }

    public UpdateNodeMetadataEventSerializer(Class<UpdateNodeMetadataEvent> t)
    {
        super(t);
    }

    @Override
    public void serialize(UpdateNodeMetadataEvent event, JsonGenerator jgen, SerializerProvider provider)
    {
        try
        {
            jgen.writeStartObject();

            jgen.writeArrayFieldStart("setProperties");
            event.getMetadataPropertiesToSet().values().forEach(property -> writeProperty(jgen, property));
            jgen.writeEndArray();

            jgen.writeArrayFieldStart("unsetProperties");
            event.getMetadataPropertiesToUnset().forEach(propertyName -> writePropertyName(jgen, propertyName));
            jgen.writeEndArray();

            jgen.writeEndObject();
        }
        catch (Exception e)
        {
            throw new JsonSerializationException("Property serialization failed", e);
        }
    }

    private void writeProperty(JsonGenerator jgen, NodeProperty<?> property)
    {
        try
        {
            jgen.writeStartObject();
            jgen.writeObjectField(property.name(), property.value());
            jgen.writeEndObject();
        }
        catch (IOException e)
        {
            throw new JsonSerializationException("UpdateNodeMetadataEvent serialization failed", e);
        }
    }

    private void writePropertyName(JsonGenerator jgen, String propertyName)
    {
        try
        {
            jgen.writeString(propertyName);
        }
        catch (IOException e)
        {
            throw new JsonSerializationException("UpdateNodeMetadataEvent serialization failed", e);
        }
    }
}
