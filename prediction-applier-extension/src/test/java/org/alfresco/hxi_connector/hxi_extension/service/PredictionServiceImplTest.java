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
package org.alfresco.hxi_connector.hxi_extension.service;

import static java.util.Collections.emptyList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import static org.alfresco.hxi_connector.hxi_extension.rest.api.data_model.PredictionDataModel.ASSOC_PREDICTED_BY;
import static org.alfresco.hxi_connector.hxi_extension.rest.api.data_model.PredictionDataModel.PROP_CONFIDENCE_LEVEL;
import static org.alfresco.hxi_connector.hxi_extension.rest.api.data_model.PredictionDataModel.PROP_MODEL_ID;
import static org.alfresco.hxi_connector.hxi_extension.rest.api.data_model.PredictionDataModel.PROP_PREDICTION_DATE_TIME;
import static org.alfresco.hxi_connector.hxi_extension.rest.api.data_model.PredictionDataModel.PROP_PREDICTION_VALUE;
import static org.alfresco.hxi_connector.hxi_extension.rest.api.data_model.PredictionDataModel.PROP_PREVIOUS_VALUE;
import static org.alfresco.hxi_connector.hxi_extension.rest.api.data_model.PredictionDataModel.PROP_REVIEW_STATUS;
import static org.alfresco.hxi_connector.hxi_extension.rest.api.data_model.PredictionDataModel.PROP_UPDATE_TYPE;
import static org.alfresco.hxi_connector.hxi_extension.rest.api.data_model.PredictionDataModel.TYPE_PREDICTION;
import static org.alfresco.hxi_connector.hxi_extension.rest.api.model.ReviewStatus.UNREVIEWED;
import static org.alfresco.hxi_connector.hxi_extension.rest.api.model.UpdateType.AUTOCORRECT;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.alfresco.hxi_connector.common.exception.ValidationException;
import org.alfresco.hxi_connector.hxi_extension.rest.api.exception.PredictionStateChangedException;
import org.alfresco.hxi_connector.hxi_extension.rest.api.model.ReviewStatus;
import org.alfresco.hxi_connector.hxi_extension.service.model.Prediction;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/** Unit tests for {@link PredictionServiceImpl}. */
@ExtendWith(MockitoExtension.class)
public class PredictionServiceImplTest
{
    private static final NodeRef NODE_REF = new NodeRef("node://ref/");
    private static final NodeRef PREDICTION_NODE_REF = new NodeRef("prediction://node/ref/");
    private static final String NAMESPACE = "http://namespace";
    private static final String NAMESPACE_PREFIX = "ns";
    private static final QName PROPERTY_QNAME = QName.createQName(NAMESPACE, "propertyName");
    private static final ChildAssociationRef CHILD_ASSOC_REF = new ChildAssociationRef(ASSOC_PREDICTED_BY, NODE_REF, PROPERTY_QNAME, PREDICTION_NODE_REF);

    @InjectMocks
    private PredictionServiceImpl predictionService;
    @Mock
    private NodeService nodeService;
    @Mock
    private NamespaceService namespaceService;

    @Test
    public void testGetPredictedProperties_noPredictions()
    {
        // given
        given(nodeService.getChildAssocs(NODE_REF, Set.of(TYPE_PREDICTION))).willReturn(List.of());

        // when
        List<String> predictedProperties = predictionService.getPredictedProperties(NODE_REF);

        // then
        assertEquals(emptyList(), predictedProperties);
    }

    @Test
    public void testGetPredictedProperties_withPredictions()
    {
        // given
        given(nodeService.getChildAssocs(NODE_REF, Set.of(TYPE_PREDICTION))).willReturn(List.of(CHILD_ASSOC_REF));
        given(namespaceService.getPrefixes(NAMESPACE)).willReturn(Set.of(NAMESPACE_PREFIX));

        // when
        List<String> predictedProperties = predictionService.getPredictedProperties(NODE_REF);

        // then
        assertEquals(List.of("ns:propertyName"), predictedProperties);
    }

    @Test
    public void testGetPredictions_noPredictions()
    {
        // when
        List<Prediction> predictions = predictionService.getPredictions(NODE_REF);

        // then
        assertEquals(emptyList(), predictions);
    }

    @Test
    public void testGetPredictions_withPredictions()
    {
        // given
        Map<QName, Serializable> properties = Map.of(
                PROP_PREDICTION_DATE_TIME, new Date(1_234_567_890L),
                PROP_CONFIDENCE_LEVEL, 0.5f,
                PROP_MODEL_ID, "hx-model-id",
                PROP_PREDICTION_VALUE, "blue",
                PROP_PREVIOUS_VALUE, "red",
                PROP_UPDATE_TYPE, AUTOCORRECT.toString(),
                PROP_REVIEW_STATUS, UNREVIEWED.toString());
        given(nodeService.getProperties(PREDICTION_NODE_REF)).willReturn(properties);
        given(nodeService.getChildAssocs(NODE_REF, Set.of(TYPE_PREDICTION))).willReturn(List.of(CHILD_ASSOC_REF));
        given(namespaceService.getPrefixes(NAMESPACE)).willReturn(Set.of(NAMESPACE_PREFIX));

        // when
        List<Prediction> predictions = predictionService.getPredictions(NODE_REF);

        // then
        Prediction expectedPrediction = new Prediction(PREDICTION_NODE_REF.getId(), "ns:propertyName", new Date(1_234_567_890L),
                0.5f, "hx-model-id", "blue", "red", AUTOCORRECT, UNREVIEWED);
        assertEquals(List.of(expectedPrediction), predictions);
    }

    @Test
    public void testApplyPredictions_newPrediction()
    {
        // given
        given(nodeService.getChildAssocs(NODE_REF, Set.of(TYPE_PREDICTION))).willReturn(List.of());
        given(nodeService.getProperties(NODE_REF)).willReturn(Map.of(PROPERTY_QNAME, "red"));
        given(namespaceService.getNamespaceURI(NAMESPACE_PREFIX)).willReturn(NAMESPACE);
        Map<QName, Serializable> expectedProperties = Map.of(
                PROP_PREDICTION_DATE_TIME, new Date(1_234_567_890L),
                PROP_CONFIDENCE_LEVEL, 0.5f,
                PROP_MODEL_ID, "hx-model-id",
                PROP_PREDICTION_VALUE, "blue",
                PROP_PREVIOUS_VALUE, "red",
                PROP_UPDATE_TYPE, AUTOCORRECT,
                PROP_REVIEW_STATUS, UNREVIEWED.toString());
        given(nodeService.createNode(NODE_REF, ASSOC_PREDICTED_BY, PROPERTY_QNAME, TYPE_PREDICTION, expectedProperties)).willReturn(CHILD_ASSOC_REF);

        // when
        Prediction prediction = new Prediction(null, "ns:propertyName", new Date(1_234_567_890L),
                0.5f, "hx-model-id", "blue", null, AUTOCORRECT, UNREVIEWED);
        List<Prediction> returnedPredictions = predictionService.applyPredictions(NODE_REF, List.of(prediction));

        // then
        Prediction expectedPrediction = new Prediction(PREDICTION_NODE_REF.getId(), "ns:propertyName", new Date(1_234_567_890L),
                0.5f, "hx-model-id", "blue", "red", AUTOCORRECT, UNREVIEWED);
        assertEquals(List.of(expectedPrediction), returnedPredictions);
        assertPropertySet(NODE_REF, PROPERTY_QNAME, "blue");
    }

    @Test
    public void testApplyPredictions_updatedPrediction()
    {
        // given
        given(nodeService.getChildAssocs(NODE_REF, Set.of(TYPE_PREDICTION))).willReturn(List.of(CHILD_ASSOC_REF));
        given(nodeService.getProperties(NODE_REF)).willReturn(Map.of(PROPERTY_QNAME, "green"));
        given(nodeService.getProperties(PREDICTION_NODE_REF)).willReturn(Map.of(PROP_PREDICTION_VALUE, "green",
                PROP_PREVIOUS_VALUE, "red"));
        given(namespaceService.getNamespaceURI(NAMESPACE_PREFIX)).willReturn(NAMESPACE);

        // when
        Prediction prediction = new Prediction(null, "ns:propertyName", new Date(1_234_567_890L),
                0.5f, "hx-model-id", "blue", null, AUTOCORRECT, UNREVIEWED);
        List<Prediction> returnedPredictions = predictionService.applyPredictions(NODE_REF, List.of(prediction));

        // then
        Prediction expectedPrediction = new Prediction(PREDICTION_NODE_REF.getId(), "ns:propertyName", new Date(1_234_567_890L),
                0.5f, "hx-model-id", "blue", "red", AUTOCORRECT, UNREVIEWED);
        assertEquals(List.of(expectedPrediction), returnedPredictions);
        assertPropertySet(NODE_REF, PROPERTY_QNAME, "blue");
    }

    private void assertPropertySet(NodeRef nodeRef, QName propertyQName, String expectedValue)
    {
        ArgumentCaptor<Map<QName, Serializable>> propertyCaptor = ArgumentCaptor.forClass(Map.class);
        then(nodeService).should().setProperties(eq(nodeRef), propertyCaptor.capture());
        assertEquals(expectedValue, propertyCaptor.getValue().get(propertyQName));
    }

    private void assertSinglePropertySet(NodeRef nodeRef, QName propertyQName, Serializable expectedValue)
    {
        ArgumentCaptor<Serializable> propertyCaptor = ArgumentCaptor.forClass(Serializable.class);
        then(nodeService).should().setProperty(eq(nodeRef), eq(propertyQName), propertyCaptor.capture());
        assertEquals(expectedValue, propertyCaptor.getValue());
    }

    @Test
    public void testApplyPredictions_updateAfterRejectedPrediction()
    {
        // given
        given(nodeService.getChildAssocs(NODE_REF, Set.of(TYPE_PREDICTION))).willReturn(List.of(CHILD_ASSOC_REF));
        // The user has rejected the prediction and so that current value doesn't match the previous prediction.
        given(nodeService.getProperties(NODE_REF)).willReturn(Map.of(PROPERTY_QNAME, "red"));
        given(nodeService.getProperties(PREDICTION_NODE_REF)).willReturn(Map.of(PROP_PREDICTION_VALUE, "green",
                PROP_PREVIOUS_VALUE, "red"));
        given(namespaceService.getNamespaceURI(NAMESPACE_PREFIX)).willReturn(NAMESPACE);

        // when
        Prediction prediction = new Prediction(null, "ns:propertyName", new Date(1_234_567_890L),
                0.5f, "hx-model-id", "blue", null, AUTOCORRECT, UNREVIEWED);
        List<Prediction> returnedPredictions = predictionService.applyPredictions(NODE_REF, List.of(prediction));

        // then
        assertEquals(emptyList(), returnedPredictions);
        then(nodeService).shouldHaveNoMoreInteractions();
    }

    @Test
    public void testApplyPredictions_missingPrediction()
    {
        // when
        assertThrows(ValidationException.class, () -> predictionService.applyPredictions(NODE_REF, emptyList()));
    }

    @Test
    public void testReviewPrediction_propertyValueChangedBeforePredictionReview()
    {
        // given
        reviewPredictionTestSetup(UNREVIEWED);
        // value has changed after prediction has been applied
        given(nodeService.getProperty(NODE_REF, PROPERTY_QNAME)).willReturn("yellow");

        // when
        assertThrows(PredictionStateChangedException.class, () -> predictionService.reviewPrediction(PREDICTION_NODE_REF, ReviewStatus.CONFIRMED));
    }

    @Test
    public void testReviewPrediction_predictionAlreadyReviewedAndReviewStatusChangeRequested()
    {
        // given
        reviewPredictionTestSetup(ReviewStatus.REJECTED);
        given(nodeService.getProperty(NODE_REF, PROPERTY_QNAME)).willReturn("blue");
        given(nodeService.getProperty(PREDICTION_NODE_REF, PROP_REVIEW_STATUS)).willReturn(ReviewStatus.REJECTED.toString());

        // when
        assertThrows(PredictionStateChangedException.class, () -> predictionService.reviewPrediction(PREDICTION_NODE_REF, ReviewStatus.CONFIRMED));
    }

    @Test
    public void testReviewPrediction_predictionConfirmed()
    {
        // given
        reviewPredictionTestSetup(UNREVIEWED);
        given(nodeService.getProperty(NODE_REF, PROPERTY_QNAME)).willReturn("blue");
        given(nodeService.getProperty(PREDICTION_NODE_REF, PROP_REVIEW_STATUS)).willReturn(UNREVIEWED.toString());

        // when
        predictionService.reviewPrediction(PREDICTION_NODE_REF, ReviewStatus.CONFIRMED);

        // then
        assertSinglePropertySet(PREDICTION_NODE_REF, PROP_REVIEW_STATUS, ReviewStatus.CONFIRMED.toString());

    }

    @Test
    public void testReviewPrediction_predictionRejected()
    {
        // given
        reviewPredictionTestSetup(UNREVIEWED);
        given(nodeService.getProperty(NODE_REF, PROPERTY_QNAME)).willReturn("blue");
        given(nodeService.getProperty(PREDICTION_NODE_REF, PROP_REVIEW_STATUS)).willReturn(UNREVIEWED.toString());

        // when
        predictionService.reviewPrediction(PREDICTION_NODE_REF, ReviewStatus.REJECTED);

        // then
        assertSinglePropertySet(PREDICTION_NODE_REF, PROP_REVIEW_STATUS, ReviewStatus.REJECTED.toString());
        assertSinglePropertySet(NODE_REF, PROPERTY_QNAME, "red");
    }

    private void reviewPredictionTestSetup(ReviewStatus initialReviewStatus)
    {
        Map<QName, Serializable> properties = Map.of(
                PROP_PREDICTION_DATE_TIME, new Date(1_234_567_890L),
                PROP_CONFIDENCE_LEVEL, 0.5f,
                PROP_MODEL_ID, "hx-model-id",
                PROP_PREDICTION_VALUE, "blue",
                PROP_PREVIOUS_VALUE, "red",
                PROP_UPDATE_TYPE, AUTOCORRECT.toString(),
                PROP_REVIEW_STATUS, initialReviewStatus.toString());
        given(nodeService.getProperties(PREDICTION_NODE_REF)).willReturn(properties);
        given(namespaceService.getNamespaceURI(NAMESPACE_PREFIX)).willReturn(NAMESPACE);
        given(namespaceService.getPrefixes(NAMESPACE)).willReturn(Set.of(NAMESPACE_PREFIX));
        given(nodeService.getPrimaryParent(PREDICTION_NODE_REF)).willReturn(CHILD_ASSOC_REF);
    }
}
