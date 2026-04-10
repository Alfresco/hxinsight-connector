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
package org.alfresco.hxi_connector.prediction_applier.hx_insight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

import static org.alfresco.hxi_connector.common.model.prediction.UpdateType.AUTOFILL;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.exception.HxInsightConnectorRuntimeException;
import org.alfresco.hxi_connector.common.model.prediction.PredictionModel;
import org.alfresco.hxi_connector.prediction_applier.model.prediction.Prediction;
import org.alfresco.hxi_connector.prediction_applier.model.prediction.PredictionEntry;

public class PredictionMapperTest
{
    private static final String OBJECT_ID = "object-id";
    private static final String MODEL_ID = "model-123";
    private static final String FIELD = "cm:description";
    private static final float CONFIDENCE = 0.95f;
    private static final String VALUE = "predicted-value";

    private PredictionMapper predictionMapper;

    @BeforeEach
    void setUp()
    {
        predictionMapper = mock(PredictionMapper.class, CALLS_REAL_METHODS);
    }

    @Test
    void testMap_nullEntry_shouldReturnNull()
    {
        // when
        PredictionModel result = predictionMapper.map(null);

        // then
        assertThat(result).isNull();
    }

    @Test
    void testMap_zeroPredictions_shouldThrow()
    {
        // given
        PredictionEntry entry = new PredictionEntry(OBJECT_ID, MODEL_ID, AUTOFILL, Collections.emptyList());

        // when
        Throwable thrown = catchThrowable(() -> predictionMapper.map(entry));

        // then
        assertThat(thrown)
                .isInstanceOf(HxInsightConnectorRuntimeException.class)
                .hasMessageContaining("only one prediction per entry");
    }

    @Test
    void testMap_multiplePredictions_shouldThrow()
    {
        // given
        Prediction first = new Prediction(FIELD, CONFIDENCE, VALUE);
        Prediction second = new Prediction("cm:title", 0.8f, "other-value");
        PredictionEntry entry = new PredictionEntry(OBJECT_ID, MODEL_ID, AUTOFILL, List.of(first, second));

        // when
        Throwable thrown = catchThrowable(() -> predictionMapper.map(entry));

        // then
        assertThat(thrown)
                .isInstanceOf(HxInsightConnectorRuntimeException.class)
                .hasMessageContaining("only one prediction per entry");
    }

    @Test
    void testMap_singlePrediction_shouldCallThrough()
    {
        // given
        Prediction prediction = new Prediction(FIELD, CONFIDENCE, VALUE);
        PredictionEntry entry = new PredictionEntry(OBJECT_ID, MODEL_ID, AUTOFILL, List.of(prediction));

        PredictionModel expectedModel = new PredictionModel();
        expectedModel.setProperty(FIELD);
        expectedModel.setConfidenceLevel(CONFIDENCE);
        expectedModel.setPredictionValue(VALUE);
        expectedModel.setModelId(MODEL_ID);
        expectedModel.setUpdateType(AUTOFILL);

        given(predictionMapper.map(prediction, MODEL_ID, AUTOFILL)).willReturn(expectedModel);

        // when
        PredictionModel result = predictionMapper.map(entry);

        // then
        then(predictionMapper).should().map(prediction, MODEL_ID, AUTOFILL);
        assertThat(result).isEqualTo(expectedModel);
    }
}
