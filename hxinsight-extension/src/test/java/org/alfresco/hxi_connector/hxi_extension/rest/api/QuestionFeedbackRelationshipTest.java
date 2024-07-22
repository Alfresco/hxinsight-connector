/*
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
package org.alfresco.hxi_connector.hxi_extension.rest.api;

import static java.util.Collections.emptyList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.alfresco.hxi_connector.hxi_extension.rest.api.model.FeedbackType.DISLIKE;
import static org.alfresco.hxi_connector.hxi_extension.rest.api.model.FeedbackType.LIKE;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.extensions.webscripts.WebScriptException;

import org.alfresco.hxi_connector.hxi_extension.rest.api.model.FeedbackModel;
import org.alfresco.hxi_connector.hxi_extension.service.HxInsightClient;

@ExtendWith(MockitoExtension.class)
public class QuestionFeedbackRelationshipTest
{
    @InjectMocks
    QuestionFeedbackRelation questionFeedbackRelation;
    @Mock
    HxInsightClient mockHxInsightClient;

    @Test
    void testFeedbackCanBeSubmittedSuccessfully()
    {
        // given
        FeedbackModel feedback = new FeedbackModel(LIKE, "Thanks for the evidence-based solution to my problem!");

        // when
        List<FeedbackModel> actual = questionFeedbackRelation.create("question-id", List.of(feedback), null);

        // then
        assertEquals(List.of(feedback), actual);
    }

    @Test
    void testMultiplePiecesOfFeedbackThrowsException()
    {
        // given
        FeedbackModel feedbackA = new FeedbackModel(LIKE, null);
        FeedbackModel feedbackB = new FeedbackModel(DISLIKE, "Churros are not suitable for use in surgery");

        // when and then
        assertThrows(WebScriptException.class, () -> questionFeedbackRelation.create("question-id", List.of(feedbackA, feedbackB), null));
    }

    @Test
    void testNoFeedbackThrowsException()
    {
        // when and then
        assertThrows(WebScriptException.class, () -> questionFeedbackRelation.create("question-id", emptyList(), null));
    }
}
