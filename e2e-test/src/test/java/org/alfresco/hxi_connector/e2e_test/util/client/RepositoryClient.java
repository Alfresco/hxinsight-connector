/*-
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
package org.alfresco.hxi_connector.e2e_test.util.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import org.alfresco.hxi_connector.e2e_test.util.client.model.Node;
import org.alfresco.hxi_connector.e2e_test.util.client.model.NodeEntry;
import org.alfresco.hxi_connector.e2e_test.util.client.model.User;
import org.alfresco.hxi_connector.e2e_test.util.client.model.Visibility;

@AllArgsConstructor
public class RepositoryClient
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final User ADMIN_USER = new User("admin", "admin");
    private static final String API_PATH = "%s/alfresco/api/-default-/public/alfresco/versions/1";
    private static final String GS_API_PATH = "%s/alfresco/api/-default-/public/gs/versions/1";
    private static final String NODES_URL_PATTERN = API_PATH + "/nodes/%s";
    private static final String SITES_BASE_URL = API_PATH + "/sites";
    private static final String SECURITY_GROUPS_BASE_URL = GS_API_PATH + "/security-groups";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String baseUrl;
    private User user;

    @SneakyThrows
    public void createUser(User userToCreate)
    {
        String uri = API_PATH.formatted(baseUrl) + "/people";

        send(requestBuilder(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                          "id": "%s",
                          "firstName": "%s",
                          "email": "test@example.com",
                          "password": "%s"
                        }
                        """.formatted(userToCreate.username(), userToCreate.username(), userToCreate.password()), StandardCharsets.UTF_8))
                .build());
    }

    @SneakyThrows
    public String createSite(String title, Visibility visibility)
    {
        String uri = SITES_BASE_URL.formatted(baseUrl);

        return extractId(send(requestBuilder(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                          "title": "%s",
                          "visibility": "%s"
                        }
                        """.formatted(title, visibility.name()), StandardCharsets.UTF_8))
                .build()).body());
    }

    @SneakyThrows
    public String getSiteDocumentLibraryId(String siteId)
    {
        String uri = SITES_BASE_URL.formatted(baseUrl) + "/" + siteId + "/containers/documentLibrary";

        return extractId(send(requestBuilder(uri).GET().build()).body());
    }

    @SneakyThrows
    public String createSecurityGroup(String name)
    {
        String uri = SECURITY_GROUPS_BASE_URL.formatted(baseUrl);

        return extractId(send(requestBuilder(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                          "groupName": "%s",
                          "groupType": "user_requires_all"
                        }
                        """.formatted(name), StandardCharsets.UTF_8))
                .build()).body());
    }

    @SneakyThrows
    public String createSecurityMark(String securityGroupId, String name)
    {
        String uri = (SECURITY_GROUPS_BASE_URL + "/%s/security-marks").formatted(baseUrl, securityGroupId);

        return extractId(send(requestBuilder(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {
                          "name": "%s"
                        }
                        """.formatted(name), StandardCharsets.UTF_8))
                .build()).body());
    }

    @SneakyThrows
    public Node getNode(String nodeId)
    {
        String uri = NODES_URL_PATTERN.formatted(baseUrl, nodeId);
        return readNodeEntry(send(requestBuilder(uri)
                .header("Accept", "application/json")
                .GET()
                .build()).body()).node();
    }

    @SneakyThrows
    public Node createNodeWithContent(String parentNodeId, String filename, InputStream fileContent, String mimeType)
    {
        String uri = NODES_URL_PATTERN.formatted(baseUrl, parentNodeId) + "/children";
        MultipartBody multipartBody = createMultipartBody(filename, fileContent, mimeType);
        return readNodeEntry(send(requestBuilder(uri)
                .header("Content-Type", "multipart/form-data; boundary=" + multipartBody.boundary())
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(multipartBody.content()))
                .build()).body()).node();
    }

    @SneakyThrows
    public Node createNodeWithContent(String parentId, File content)
    {
        try (InputStream fileContent = Files.newInputStream(content.toPath()))
        {
            String mimeType = Files.probeContentType(content.toPath());
            return createNodeWithContent(parentId, content.getName(), fileContent, mimeType != null ? mimeType : "application/octet-stream");
        }
    }

    @SneakyThrows
    public Node updateNodeWithContent(String nodeId, String updateBody)
    {
        String uri = NODES_URL_PATTERN.formatted(baseUrl, nodeId);
        return readNodeEntry(send(requestBuilder(uri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(updateBody, StandardCharsets.UTF_8))
                .build()).body()).node();
    }

    @SneakyThrows
    public void deleteNode(String nodeId)
    {
        String uri = NODES_URL_PATTERN.formatted(baseUrl, nodeId);
        send(requestBuilder(uri)
                .header("Content-Type", "application/json")
                .DELETE()
                .build());
    }

    @SneakyThrows
    public void secureNode(String nodeId, String securityGroupId, String securityMarkId)
    {
        String uri = (GS_API_PATH + "/secured-nodes/%s/securing-marks").formatted(baseUrl, nodeId);

        send(requestBuilder(uri)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                        [
                          {
                            "id": "%s",
                            "groupId": "%s",
                            "op": "ADD"
                          }
                        ]
                        """.formatted(securityMarkId, securityGroupId), StandardCharsets.UTF_8))
                .build());
    }

    @SneakyThrows
    public void setReadAccess(String nodeId, String allowedUserId, String deniedUserId)
    {
        String uri = NODES_URL_PATTERN.formatted(baseUrl, nodeId);

        send(requestBuilder(uri)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString("""
                        {
                          "permissions":
                            {
                              "locallySet":
                                [
                                  {"authorityId": "%s", "name": "Consumer", "accessStatus":"ALLOWED"},
                                  {"authorityId": "%s", "name": "Read", "accessStatus":"DENIED"}
                                ]
                            }
                        }
                        """.formatted(allowedUserId, deniedUserId), StandardCharsets.UTF_8))
                .build());
    }

    @SneakyThrows
    private NodeEntry readNodeEntry(String responseBody)
    {
        return OBJECT_MAPPER.readValue(responseBody, NodeEntry.class);
    }

    @SneakyThrows
    private String extractId(String responseBody)
    {
        JsonNode root = OBJECT_MAPPER.readTree(responseBody);
        return root.path("entry").path("id").asText();
    }

    private HttpRequest.Builder requestBuilder(String uri)
    {
        return HttpRequest.newBuilder(URI.create(uri))
                .header("Authorization", basicAuthorizationHeader(user));
    }

    @SneakyThrows
    private MultipartBody createMultipartBody(String filename, InputStream fileContent, String mimeType)
    {
        String boundary = "----HxInsightConnectorBoundary" + UUID.randomUUID();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] newLine = "\r\n".getBytes(StandardCharsets.UTF_8);

        outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write("Content-Disposition: form-data; name=\"filedata\"; filename=\"%s\"\r\n".formatted(filename).getBytes(StandardCharsets.UTF_8));
        outputStream.write("Content-Type: %s\r\n\r\n".formatted(mimeType != null ? mimeType : "application/octet-stream").getBytes(StandardCharsets.UTF_8));
        fileContent.transferTo(outputStream);
        outputStream.write(newLine);

        outputStream.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        outputStream.write("Content-Disposition: form-data; name=\"autoRename\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        outputStream.write("true".getBytes(StandardCharsets.UTF_8));
        outputStream.write(newLine);
        outputStream.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        return new MultipartBody(boundary, outputStream.toByteArray());
    }

    @SneakyThrows
    private HttpResponse<String> send(HttpRequest request)
    {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() >= 400)
        {
            throw new IllegalStateException("Request to %s failed with status %s and body: %s"
                    .formatted(request.uri(), response.statusCode(), response.body()));
        }
        return response;
    }

    private String basicAuthorizationHeader(User user)
    {
        String credentials = user.username() + ":" + user.password();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private record MultipartBody(String boundary, byte[] content)
    {}
}
