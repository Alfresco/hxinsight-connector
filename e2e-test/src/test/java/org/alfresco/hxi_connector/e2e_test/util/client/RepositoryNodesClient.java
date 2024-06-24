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

import java.io.File;
import java.io.InputStream;

import lombok.RequiredArgsConstructor;

import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.util.client.model.NodeEntry;

@RequiredArgsConstructor
public class RepositoryNodesClient
{
    private static final String URL_PATTERN = "%s/alfresco/api/-default-/public/alfresco/versions/1/nodes/%s";

    private final String baseUrl;
    private final String username;
    private final String password;

    public Node getNode(String nodeId)
    {
        String uri = URL_PATTERN.formatted(baseUrl, nodeId);
        return given().auth().preemptive().basic(username, password)
                .contentType("application/json")
                .when().get(uri)
                .then().extract().response()
                .as(NodeEntry.class).node();
    }

    public Node createNodeWithContent(String parentNodeId, String filename, InputStream fileContent, String mimeType)
    {
        String uri = URL_PATTERN.formatted(baseUrl, parentNodeId) + "/children";
        return given().auth().preemptive().basic(username, password)
                .contentType("multipart/form-data")
                .multiPart("filedata", filename, fileContent, mimeType)
                .when().post(uri)
                .then().extract().response()
                .as(NodeEntry.class).node();
    }

    public Node createNodeWithContent(String parentId, File content)
    {
        String uri = URL_PATTERN.formatted(baseUrl, parentId) + "/children";
        return given().auth().preemptive().basic(username, password)
                .contentType("multipart/form-data")
                .multiPart("filedata", content)
                .when().post(uri)
                .then().extract().response()
                .as(NodeEntry.class).node();
    }

    public void deleteNode(String nodeId)
    {
        String uri = URL_PATTERN.formatted(baseUrl, nodeId);
        given().auth().preemptive().basic(username, password)
                .contentType("application/json")
                .when().delete(uri);
    }
}
