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
package org.alfresco.hxi_connector.common.test.docker.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class DockerTagsTest
{

    @Test
    void testGetNonExistingProperty()
    {
        // given
        String nonExistingProperty = "non.existing.property";

        // when
        Throwable thrown = catchThrowable(() -> DockerTags.getProperty(nonExistingProperty));

        // then
        assertThat(thrown)
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(nonExistingProperty);
    }

    @Test
    void testGetDefaultForNonExistingProperty()
    {
        // given
        String nonExistingProperty = "non.existing.property";
        String defaultValue = "default";

        // when
        String property = DockerTags.getOrDefault(nonExistingProperty, defaultValue);

        // then
        assertThat(property).isEqualTo(defaultValue);
    }

    @Test
    void testKeySet()
    {
        assertThat(DockerTags.keySet()).isNotNull();
    }

    @Test
    void testKeySetWhenPropertiesNotYetLoaded() throws Exception
    {
        Field propertiesField = DockerTags.class.getDeclaredField("properties");
        propertiesField.setAccessible(true);
        Properties original = (Properties) propertiesField.get(null);

        try
        {
            propertiesField.set(null, null);
            assertThat(DockerTags.keySet()).isNotNull();
        }
        finally
        {
            propertiesField.set(null, original);
        }
    }

    @Test
    void testGetPropertyWhenPropertiesNotYetLoaded() throws Exception
    {
        Field propertiesField = DockerTags.class.getDeclaredField("properties");
        propertiesField.setAccessible(true);
        Properties original = (Properties) propertiesField.get(null);

        try
        {
            propertiesField.set(null, null);
            Throwable thrown = catchThrowable(() -> DockerTags.getProperty("non.existing.property"));
            assertThat(thrown).isInstanceOf(NoSuchElementException.class);
        }
        finally
        {
            propertiesField.set(null, original);
        }
    }

    @Test
    void testGetPropertyThrowsForUnresolvedValue() throws Exception
    {
        Field propertiesField = DockerTags.class.getDeclaredField("properties");
        propertiesField.setAccessible(true);
        Properties original = (Properties) propertiesField.get(null);

        Properties mockProperties = new Properties();
        mockProperties.setProperty("unresolved.prop", "@unresolved.prop@");

        try
        {
            propertiesField.set(null, mockProperties);
            assertThatThrownBy(() -> DockerTags.getProperty("unresolved.prop"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not resolved");
        }
        finally
        {
            propertiesField.set(null, original);
        }
    }
}
