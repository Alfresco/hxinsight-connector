/*-
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
package org.alfresco.hxi_connector.e2e_test.util.client;

import static io.restassured.RestAssured.given;

import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import lombok.SneakyThrows;

import org.alfresco.hxi_connector.e2e_test.util.client.model.S3Bucket;
import org.alfresco.hxi_connector.e2e_test.util.client.model.S3Object;

public class AwsS3Client
{
    private final XmlMapper xmlMapper = new XmlMapper();
    private final String baseUrl;
    private final String bucketName;

    public AwsS3Client(String host, Integer port, String bucketName)
    {
        this.baseUrl = "http://%s:%s".formatted(host, port);
        this.bucketName = bucketName;
    }

    static
    {
        RestAssured.defaultParser = Parser.JSON;
    }

    @SneakyThrows
    public List<S3Object> listS3Content()
    {
        S3Bucket s3Bucket = xmlMapper.readValue(given()
                .contentType("application/xml")
                .when()
                .get("%s/%s/".formatted(baseUrl, bucketName))
                .then()
                .extract().response()
                .asString(), S3Bucket.class);

        return s3Bucket.content();
    }

    public InputStream getS3ObjectContent(String objectKey)
    {
        return given()
                .when()
                .get("%s/%s/%s".formatted(baseUrl, bucketName, objectKey))
                .then()
                .extract().response()
                .asInputStream();
    }
}
