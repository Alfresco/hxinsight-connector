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

    public Node getNode(String nodeId)
    {
        String uri = URL_PATTERN.formatted(baseUrl, nodeId);
        return given().auth().preemptive().basic(username, password)
            .contentType("application/json")
            .when().get(uri)
            .then().extract().response()
            .as(NodeEntry.class).node();
    }
}
