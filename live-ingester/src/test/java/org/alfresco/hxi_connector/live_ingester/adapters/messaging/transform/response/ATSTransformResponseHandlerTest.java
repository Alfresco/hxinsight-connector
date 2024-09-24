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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.response;

import static org.assertj.core.api.Assertions.assertThat;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.response.ATSTransformResponseHandler.EXPECTED_STATUS_CODE_REGEX;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ATSTransformResponseHandlerTest
{

    private static Stream<Arguments> atsResponses()
    {
        return Stream.of(
                Arguments.of("""
                        {"requestId":"dedc19a9-d214-4832-91e1-1dca3e34ce4e","status":201,"errorDetails":null,"sourceReference":"6a47d27c-0503-49b7-a6da-23c4b381c9a3","targetReference":"bccd309c-e104-4a85-84a8-93598f8ea51b","clientData":"{\\"nodeRef\\":\\"7a82cd4e-215b-4e2f-82cd-4e215b9e2f9e\\",\\"targetMimeType\\":\\"application/pdf\\",\\"retryAttempt\\":0,\\"timestamp\\":1727175016824}","schema":1,"internalContext":null}
                        """, true),
                Arguments.of("""
                        {"requestId":"b79a8e6c-c22a-4b2b-8510-cfd1937578f4","status":400,"errorDetails":"08250077 Transformation failed occurred.","sourceReference":"56804085-1ed6-4b1a-a4ca-6fd4268c7491","targetReference":"8d864f52-eb33-49a1-b3da-6a825cbcdc47","clientData":"{\\"nodeRef\\":\\"e7af4b5b-c812-429b-a9c2-34b98fa0eaac\\",\\"targetMimeType\\":\\"application/pdf\\",\\"retryAttempt\\":0,\\"timestamp\\":1727175018945}","schema":1,"internalContext":null}
                        """, false),
                Arguments.of("""
                        {
                          "requestId" : "9cb47782-ec93-4008-bd0a-4fa0b1c7d092",
                          "status" : 201,
                          "errorDetails" : null,
                          "sourceReference" : "workspace://SpacesStore/1a0b110f-1e09-4ca2-b367-fe25e4964a4e",
                          "targetReference" : "54dff325-d334-417c-9008-80dfc5e15f53",
                          "clientData" : "{\\"nodeRef\\":\\"1a0b110f-1e09-4ca2-b367-fe25e4964a4e\\",\\"targetMimeType\\":\\"application/pdf\\",\\"retryAttempt\\":0,\\"timestamp\\":1727175017384}",
                          "schema" : 1,
                          "internalContext" : null
                        }
                        """, true),
                Arguments.of("""
                        {
                          "requestId" : "8e7ca542-027a-4e8b-ae3d-1a9dcf2a5dfb",
                          "status" : 400,
                          "errorDetails" : "08240066 Transformation failed occurred.",
                          "sourceReference" : "workspace://SpacesStore/1cffebce-c758-4071-a6ae-1e5730015e81",
                          "targetReference" : null,
                          "clientData" : "{\\"nodeRef\\":\\"1cffebce-c758-4071-a6ae-1e5730015e81\\",\\"targetMimeType\\":\\"application/pdf\\",\\"retryAttempt\\":0,\\"timestamp\\":1727175017669}",
                          "schema" : 1,
                          "internalContext" : null
                        }
                        """, false));
    }

    @ParameterizedTest
    @MethodSource("atsResponses")
    void testAtsResponseStatusRegex(String atsResponse, boolean expectedStatusMatch)
    {
        // when
        boolean actualStatusMatch = atsResponse.matches(EXPECTED_STATUS_CODE_REGEX);

        // then
        assertThat(actualStatusMatch).isEqualTo(expectedStatusMatch);
    }
}
