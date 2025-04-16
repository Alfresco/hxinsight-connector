/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.alfresco.hxi_connector.hxi_extension.service.model.AnswerResponse;

@ToString
@EqualsAndHashCode
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(NON_NULL)
@Builder
public class AnswerModel
{
    private static final String RESPONSE_STATUS_COMPLETE = "complete";

    private String answer;
    private String question;
    private boolean isComplete;
    private Set<ObjectReferenceModel> objectReferences;

    public static AnswerModel fromServiceModel(AnswerResponse response)
    {
        Set<ObjectReferenceModel> objectReferences = Optional.ofNullable(response.getObjectReferences())
                .map(refs -> refs.stream()
                        .map(ObjectReferenceModel::fromServiceModel)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());

        return AnswerModel.builder()
                .question(response.getQuestion())
                .answer(response.getAnswer())
                .isComplete(RESPONSE_STATUS_COMPLETE.equalsIgnoreCase(response.getResponseCompleteness()))
                .objectReferences(objectReferences)
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ObjectReferenceModel
    {
        private String objectId;
        private Set<ReferenceModel> references;

        public static ObjectReferenceModel fromServiceModel(AnswerResponse.ObjectReference reference)
        {
            Set<ReferenceModel> references = Optional.ofNullable(reference.getReferences())
                    .map(refs -> refs.stream()
                            .map(ReferenceModel::fromServiceModel)
                            .collect(Collectors.toSet()))
                    .orElse(Collections.emptySet());

            return ObjectReferenceModel.builder()
                    .objectId(reference.getObjectId())
                    .references(references)
                    .build();
        }
    }

    @ToString
    @EqualsAndHashCode
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(NON_NULL)
    @Builder
    public static class ReferenceModel
    {
        private String referenceId;
        private double rankScore;
        private int rank;

        public static ReferenceModel fromServiceModel(AnswerResponse.Reference reference)
        {
            return ReferenceModel.builder()
                    .referenceId(reference.getReferenceId())
                    .rankScore(reference.getRankScore())
                    .rank(reference.getRank())
                    .build();
        }
    }

}
