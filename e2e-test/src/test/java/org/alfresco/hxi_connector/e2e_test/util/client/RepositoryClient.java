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

import lombok.AllArgsConstructor;

import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.util.client.model.NodeEntry;
import org.alfresco.hxi_connector.e2e_test.util.client.model.User;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Visibility;

@AllArgsConstructor
public class RepositoryClient
{
    public static final User ADMIN_USER = new User("admin", "admin");
    private static final String API_PATH = "%s/alfresco/api/-default-/public/alfresco/versions/1";
    private static final String NODES_URL_PATTERN = API_PATH + "/nodes/%s";
    private static final String SITES_BASE_URL = API_PATH + "/sites";

    private final String baseUrl;
    private User user;

    public void createUser(User userToCreate)
    {
        String uri = API_PATH.formatted(baseUrl) + "/people";

        given().auth().preemptive().basic(user.username(), user.password())
                .contentType("application/json")
                .body("""
                            {
                              "id": "%s",
                              "firstName": "%s",
                              "email": "test@example.com",
                              "password": "%s"
                            }
                        """.formatted(userToCreate.username(), userToCreate.username(), userToCreate.password()))
                .when().post(uri);
    }

    public String createSite(String title, Visibility visibility)
    {
        String uri = SITES_BASE_URL.formatted(baseUrl);

        return given().auth().preemptive().basic(user.username(), user.password())
                .contentType("application/json")
                .body("""
                            {
                              "title": "%s",
                              "visibility": "%s"
                            }
                        """.formatted(title, visibility.name()))
                .when().post(uri)
                .body().jsonPath().get("entry.id");
    }

    public String getSiteDocumentLibraryId(String siteId)
    {
        String uri = SITES_BASE_URL.formatted(baseUrl) + "/" + siteId + "/containers/documentLibrary";

        return given().auth().preemptive().basic(user.username(), user.password())
                .when().get(uri)
                .body().jsonPath().get("entry.id");
    }

    public Node getNode(String nodeId)
    {
        String uri = NODES_URL_PATTERN.formatted(baseUrl, nodeId);
        return given().auth().preemptive().basic(user.username(), user.password())
                .contentType("application/json")
                .when().get(uri)
                .then().extract().response()
                .as(NodeEntry.class).node();
    }

    public Node createNodeWithContent(String parentNodeId, String filename, InputStream fileContent, String mimeType)
    {
        String uri = NODES_URL_PATTERN.formatted(baseUrl, parentNodeId) + "/children";
        return given().auth().preemptive().basic(user.username(), user.password())
                .contentType("multipart/form-data")
                .multiPart("filedata", filename, fileContent, mimeType)
                .multiPart("autoRename", "true")
                .when().post(uri)
                .then().extract().response()
                .as(NodeEntry.class).node();
    }

    public Node createNodeWithContent(String parentId, File content)
    {
        String uri = NODES_URL_PATTERN.formatted(baseUrl, parentId) + "/children";
        return given().auth().preemptive().basic(user.username(), user.password())
                .contentType("multipart/form-data")
                .multiPart("filedata", content)
                .multiPart("autoRename", "true")
                .when().post(uri)
                .then().extract().response()
                .as(NodeEntry.class).node();
    }

    public Node updateNodeWithContent(String nodeId, String updateBody)
    {
        String uri = NODES_URL_PATTERN.formatted(baseUrl, nodeId);
        return given().auth().preemptive().basic(user.username(), user.password())
                .contentType("application/json")
                .body(updateBody)
                .when().put(uri)
                .then().extract().response()
                .as(NodeEntry.class).node();
    }

    public void deleteNode(String nodeId)
    {
        String uri = NODES_URL_PATTERN.formatted(baseUrl, nodeId);
        given().auth().preemptive().basic(user.username(), user.password())
                .contentType("application/json")
                .when().delete(uri);
    }
}
