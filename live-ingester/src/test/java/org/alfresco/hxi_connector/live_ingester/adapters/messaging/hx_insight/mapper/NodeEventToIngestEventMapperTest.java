/*
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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.mapper;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.ANCESTORS_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.ASPECT_NAMES_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CONTENT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_BY_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.MODIFIED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.MODIFIED_BY_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.NAME_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.TYPE_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE_OR_UPDATE;

import java.util.List;
import java.util.Map;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.ingest.object.IngestEvent;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.AuthorityInfo;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.AuthorityTypeResolver;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.ContentProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.DeleteNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.PermissionsProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType;

class NodeEventToIngestEventMapperTest
{
    private static final String NODE_ID = "node-id";
    private static final String SOURCE_ID = "dummy-source-id";
    private static final long TIMESTAMP = 1_724_225_729_830L;

    private final NodeEventToIngestEventMapper mapper = new NodeEventToIngestEventMapper();

    @Test
    void shouldMapEmptyUpdateEvent() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP);

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {}
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapDeleteEvent() throws JSONException
    {
        DeleteNodeEvent event = new DeleteNodeEvent(NODE_ID, SOURCE_ID, TIMESTAMP);

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "delete",
                  "sourceTimestamp": 1724225729830,
                  "properties": {}
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapAnnotatedMetadataProperties() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>(CREATED_AT_PROPERTY, "2024-02-19T07:56:50.034Z"))
                .addMetadataInstruction(new NodeProperty<>(MODIFIED_BY_PROPERTY, "000-000-000"))
                .addMetadataInstruction(new NodeProperty<>(MODIFIED_AT_PROPERTY, "2025-02-19T07:56:50.034Z"));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "createdAt": {"value": "2024-02-19T07:56:50.034Z", "annotation": "dateCreated"},
                    "modifiedAt": {"value": "2025-02-19T07:56:50.034Z", "annotation": "dateModified"},
                    "modifiedBy": {"value": "000-000-000", "annotation": "modifiedBy"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapTypedProperty() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("colours", List.of("red", "yellow", "green", "blue")));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "colours": {
                      "value": ["red", "yellow", "green", "blue"],
                      "type": "string"
                    }
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapContentProperty() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addContentInstruction(new ContentProperty(CONTENT_PROPERTY, "content-id", "application/pdf",
                        "application/msword", 123L, "something.doc"));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "cm:content": {
                      "file": {
                        "id": "content-id",
                        "content-type": "application/pdf",
                        "content-metadata": {
                          "content-type": "application/msword",
                          "size": 123,
                          "name": "something.doc"
                        }
                      }
                    }
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapPermissionsProperty() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>(CREATED_BY_PROPERTY, "admin"))
                .addPermissionsInstruction(new PermissionsProperty(
                        "PERMISSIONS",
                        List.of(new AuthorityInfo("user1", AuthorityTypeResolver.AuthorityType.USER)),
                        List.of(new AuthorityInfo("group1", AuthorityTypeResolver.AuthorityType.GROUP))))
                .addMetadataInstruction(new NodeProperty<>(MODIFIED_BY_PROPERTY, "user2"));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "createdBy": {"value": "admin", "annotation": "createdBy"},
                    "modifiedBy": {"value": "user2", "annotation": "modifiedBy"},
                    "PERMISSIONS": {
                      "value": {
                        "read": [{"id": "user1", "type": "USER"}],
                        "deny": [{"id": "group1", "type": "GROUP"}],
                        "principalsType": "effective"
                      },
                      "annotation": "principals"
                    }
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapNullPropertyValues() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>(CREATED_BY_PROPERTY, null))
                .addMetadataInstruction(new NodeProperty<>(MODIFIED_BY_PROPERTY, null));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "createdBy": {"value": null, "annotation": "createdBy"},
                    "modifiedBy": {"value": null, "annotation": "modifiedBy"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapObjectProperty() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("grandparent", Map.of("parent", Map.of("child", "some-data"))));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "grandparent": {
                      "value": {
                        "parent": {
                          "child": "some-data"
                        }
                      },
                      "type": "object"
                    }
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapCollectionOfFloatsToDoubleArray() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("scores", List.of(1.5f, 2.5f)));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "scores": {"value": [1.5, 2.5], "type": "float"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldIgnoreEmptyCollection() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("emptyList", List.of()))
                .addMetadataInstruction(new NodeProperty<>("nonEmptyList", List.of("something")));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "nonEmptyList": {"value": ["something"], "type": "string"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapContentPropertyWithPartialMetadata() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addContentInstruction(new ContentProperty(CONTENT_PROPERTY, null, null,
                        "application/msword", null, null));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "cm:content": {
                      "file": {
                        "content-metadata": {
                          "content-type": "application/msword"
                        }
                      }
                    }
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldThrowOnUnsupportedEventType()
    {
        NodeEvent unsupported = new NodeEvent() {
            @Override
            public String getObjectId()
            {
                return NODE_ID;
            }

            @Override
            public String getSourceId()
            {
                return SOURCE_ID;
            }

            @Override
            public long getTimestamp()
            {
                return TIMESTAMP;
            }

            @Override
            public EventType getEventType()
            {
                return CREATE_OR_UPDATE;
            }
        };

        assertThrows(IllegalArgumentException.class, () -> mapper.map(unsupported));
    }

    @Test
    void shouldMapRemainingAnnotations() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>(NAME_PROPERTY, "test.txt"))
                .addMetadataInstruction(new NodeProperty<>(TYPE_PROPERTY, "cm:content"))
                .addMetadataInstruction(new NodeProperty<>(ASPECT_NAMES_PROPERTY, List.of("cm:titled", "cm:auditable")))
                .addMetadataInstruction(new NodeProperty<>(ANCESTORS_PROPERTY, List.of("parent-id", "grandparent-id")));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "cm:name": {"value": "test.txt", "annotation": "name"},
                    "type": {"value": "cm:content", "annotation": "type"},
                    "aspectsNames": {"value": ["cm:titled", "cm:auditable"], "annotation": "aspects"},
                    "ancestors": {"value": ["parent-id", "grandparent-id"], "annotation": "hierarchy"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapBooleanProperty() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("isActive", true));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "isActive": {"value": true, "type": "boolean"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapIntegerProperty() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("count", 42));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "count": {"value": 42, "type": "integer"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapLongProperty() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("bigNumber", 9_999_999_999L));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "bigNumber": {"value": 9999999999, "type": "integer"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapDoubleProperty() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("ratio", 3.14));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "ratio": {"value": 3.14, "type": "float"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapFloatAsSingleValue() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("weight", 2.5f));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "weight": {"value": 2.5, "type": "float"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapCollectionOfIntegers() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("numbers", List.of(1, 2, 3)));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "numbers": {"value": [1, 2, 3], "type": "integer"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapCollectionOfLongs() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("timestamps", List.of(1_000_000_000L, 2_000_000_000L)));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "timestamps": {"value": [1000000000, 2000000000], "type": "integer"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapCollectionOfBooleans() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("flags", List.of(true, false, true)));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "flags": {"value": [true, false, true], "type": "boolean"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapCollectionOfMaps() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("items",
                        List.of(Map.of("key", "value1"), Map.of("key", "value2"))));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "items": {
                      "value": [{"key": "value1"}, {"key": "value2"}],
                      "type": "object"
                    }
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapContentPropertyWithIdOnly() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addContentInstruction(new ContentProperty(CONTENT_PROPERTY, "content-id", null,
                        null, null, null));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "cm:content": {
                      "file": {
                        "id": "content-id"
                      }
                    }
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapPermissionsWithEmptyLists() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addPermissionsInstruction(new PermissionsProperty("PERMISSIONS", List.of(), List.of()));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "PERMISSIONS": {
                      "value": {
                        "read": [],
                        "deny": [],
                        "principalsType": "effective"
                      },
                      "annotation": "principals"
                    }
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapCombinedMetadataContentAndPermissions() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>(NAME_PROPERTY, "report.pdf"))
                .addContentInstruction(new ContentProperty(CONTENT_PROPERTY, "file-id", "application/pdf",
                        null, 4096L, "report.pdf"))
                .addPermissionsInstruction(new PermissionsProperty("PERMISSIONS",
                        List.of(new AuthorityInfo("admin", AuthorityTypeResolver.AuthorityType.USER)),
                        List.of()));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "cm:name": {"value": "report.pdf", "annotation": "name"},
                    "cm:content": {
                      "file": {
                        "id": "file-id",
                        "content-type": "application/pdf",
                        "content-metadata": {
                          "size": 4096,
                          "name": "report.pdf"
                        }
                      }
                    },
                    "PERMISSIONS": {
                      "value": {
                        "read": [{"id": "admin", "type": "USER"}],
                        "deny": [],
                        "principalsType": "effective"
                      },
                      "annotation": "principals"
                    }
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldFallbackToStringForUnknownType() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("custom", java.time.LocalDate.of(2024, 1, 15)));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "custom": {"value": "2024-01-15", "type": "string"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }
}
