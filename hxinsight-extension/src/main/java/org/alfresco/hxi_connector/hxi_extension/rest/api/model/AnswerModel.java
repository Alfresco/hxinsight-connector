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
package org.alfresco.hxi_connector.hxi_extension.rest.api.model;

import static java.util.stream.Collectors.toSet;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.collections4.SetUtils;

import org.alfresco.hxi_connector.hxi_extension.service.model.AnswerResponse;

@ToString
@EqualsAndHashCode
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(NON_NULL)
public class AnswerModel
{
    private static final String RESPONSE_STATUS_COMPLETE = "complete";

    private String answer;
    private String question;
    private boolean isComplete;
    private Set<ReferenceModel> references;

    public static AnswerModel fromServiceModel(AnswerResponse answer)
    {
        Set<ReferenceModel> references = SetUtils.emptyIfNull(answer.getReferences())
                .stream()
                .map(ReferenceModel::fromServiceModel)
                .collect(toSet());

        return new AnswerModel(
                answer.getAnswer(),
                answer.getQuestion(),
                RESPONSE_STATUS_COMPLETE.equalsIgnoreCase(answer.getResponseCompleteness()),
                references);
    }

    @ToString
    @EqualsAndHashCode
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(NON_NULL)
    public static class ReferenceModel
    {
        private String referenceId;
        private String referenceText;

        public static ReferenceModel fromServiceModel(AnswerResponse.Reference reference)
        {
            return new ReferenceModel(reference.getReferenceId(), reference.getTextReference());
        }
    }

}
