/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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
package org.alfresco.hxi_connector.bulk_ingester.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Properties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

/**
 * Allows to access properties from 'docker-tags.properties' file. It's required to run: `mvn package` before accessing the properties file, to allow Maven filter out and replace variables.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DockerTags
{
    private static final String PROPERTIES_FILE = "docker-tags.properties";
    private static final String PROPERTIES_FILE_PATH = "target/test-classes/" + PROPERTIES_FILE;

    private static Properties properties;

    public static String getOrDefault(String propertyKey, String defaultValue)
    {
        if (properties == null)
        {
            loadProperties(false);
        }

        String value = defaultValue;
        if (properties != null)
        {
            String property = properties.getProperty(propertyKey);
            if (property != null && !property.startsWith("@") && !property.endsWith("@"))
            {
                value = property;
            }
        }
        return value;
    }

    @SneakyThrows
    private static void loadProperties(boolean failOnMissingFile)
    {
        File file = new File(PROPERTIES_FILE_PATH);
        if (file.exists())
        {
            properties = new Properties();
            properties.load(Files.newInputStream(file.toPath()));
        }
        else if (failOnMissingFile)
        {
            throw new NoSuchFileException("File: '" + file + "' not found");
        }
    }
}
