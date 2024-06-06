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
    private static final String URI_PATTERN = "%s/alfresco/api/-default-/public/alfresco/versions/1/nodes/%s";

    private final String baseUri;
    private final String username;
    private final String password;

    public Node createFileNode(String parentNodeId, String filename, InputStream fileContent, String mimeType)
    {
        String uri = URI_PATTERN.formatted(baseUri, parentNodeId) + "/children";
        return given().auth().preemptive().basic(username, password)
            .contentType("multipart/form-data")
            .multiPart("filedata", filename, fileContent, mimeType)
            .when().post(uri)
            .then().extract().response()
            .as(NodeEntry.class).node();
    }

    public Node getNode(String nodeId)
    {
        String uri = URI_PATTERN.formatted(baseUri, nodeId);
        return given().auth().preemptive().basic(username, password)
            .contentType("application/json")
            .when().get(uri)
            .then().extract().response()
            .as(NodeEntry.class).node();
    }

    public Node uploadExistingFile(String parentId, String pathName)
    {
        String uri = URI_PATTERN.formatted(baseUri, parentId) + "/children";
        return given().auth().preemptive().basic(username, password)
            .contentType("multipart/form-data")
            .multiPart("filedata", new File(pathName))
            .when().post(uri)
            .then().extract().response()
            .as(NodeEntry.class).node();
    }
}
