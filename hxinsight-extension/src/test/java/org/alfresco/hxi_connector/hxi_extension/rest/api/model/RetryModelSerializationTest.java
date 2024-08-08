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

package org.alfresco.hxi_connector.hxi_extension.rest.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.hxi_extension.service.model.RestrictionQuery;

class RetryModelSerializationTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldThrowWhenQuestionIdSpecified()
    {
        // given
        String retrySerialised = """
                {
                    "_questionId": "86553f9b-e382-4a1b-b07d-52adae3e96e8",
                    "comments": "This was not succinct enough!",
                    "originalQuestion": {
                        "restrictionQuery": {
                            "nodesIds": ["880a0f47-31b1-4101-b20b-4d325e54e8b1"]
                        },
                        "question": "Explain how the universe works"
                    }
                }
                """;

        // when, then
        assertThrows(UnrecognizedPropertyException.class, () -> objectMapper.readValue(retrySerialised, RetryModel.class));
    }

    @Test
    void shouldThrowWhenQuestionIdSpecified2()
    {
        // given
        String retrySerialised = """
                {
                    "questionId": "86553f9b-e382-4a1b-b07d-52adae3e96e8",
                    "comments": "This was not succinct enough!",
                    "originalQuestion": {
                        "restrictionQuery": {
                            "nodesIds": ["880a0f47-31b1-4101-b20b-4d325e54e8b1"]
                        },
                        "question": "Explain how the universe works"
                    }
                }
                """;

        // when, then
        assertThrows(UnrecognizedPropertyException.class, () -> objectMapper.readValue(retrySerialised, RetryModel.class));
    }

    @Test
    @SneakyThrows
    void shouldDeserializeRetry()
    {
        // given
        String retrySerialised = """
                {
                    "comments": "This was not succinct enough!",
                    "originalQuestion": {
                        "restrictionQuery": {
                            "nodesIds": ["880a0f47-31b1-4101-b20b-4d325e54e8b1"]
                        },
                        "question": "Explain how the universe works"
                    }
                }
                """;

        // when
        RetryModel retry = objectMapper.readValue(retrySerialised, RetryModel.class);

        // then
        RetryModel expected = new RetryModel(null, "This was not succinct enough!",
                new QuestionModel(null, "Explain how the universe works", new RestrictionQuery(Set.of("880a0f47-31b1-4101-b20b-4d325e54e8b1"))));
        assertEquals(expected, retry);
    }
}
