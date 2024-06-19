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
package org.alfresco.hxi_connector.prediction_applier.rest.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link PredictionModel} deserialization. */
public class PredictionModelDeserializationTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldThrowWhenIdSpecified()
    {
        // given
        String predictionModelSerialized = """
                {
                    "_id": "86553f9b-e382-4a1b-b07d-52adae3e96e8",
                    "property": "property",
                    "predictionDateTime": "2023-01-01T00:00:00Z",
                    "confidenceLevel": 0.8,
                    "modelId": "modelId",
                    "predictionValue": "predictionValue",
                    "updateType": "AUTOFILL",
                    "reviewStatus": "UNREVIEWED"
                }
                """;

        // when, then
        assertThrows(UnrecognizedPropertyException.class, () -> objectMapper.readValue(predictionModelSerialized, PredictionModel.class));
    }

    @Test
    void shouldThrowWhenPreviousValueSpecified()
    {
        // given
        String predictionModelSerialized = """
                {
                    "property": "property",
                    "predictionDateTime": "2023-01-01T00:00:00Z",
                    "confidenceLevel": 0.8,
                    "modelId": "modelId",
                    "predictionValue": "predictionValue",
                    "_previousValue": "previousValue",
                    "updateType": "AUTOFILL",
                    "reviewStatus": "UNREVIEWED"
                }
                """;

        // when, then
        assertThrows(UnrecognizedPropertyException.class, () -> objectMapper.readValue(predictionModelSerialized, PredictionModel.class));
    }

    @Test
    @SneakyThrows
    void shouldDeserializePredictionModel()
    {
        // given
        String predictionModelSerialized = """
                {
                    "property": "property",
                    "predictionDateTime": "2023-01-01T00:00:00Z",
                    "confidenceLevel": 0.8,
                    "modelId": "modelId",
                    "predictionValue": "predictionValue",
                    "updateType": "AUTOFILL",
                    "reviewStatus": "UNREVIEWED"
                }
                """;

        // when
        PredictionModel predictionModel = objectMapper.readValue(predictionModelSerialized, PredictionModel.class);

        // then
        assertEquals("property", predictionModel.getProperty());
    }
}
