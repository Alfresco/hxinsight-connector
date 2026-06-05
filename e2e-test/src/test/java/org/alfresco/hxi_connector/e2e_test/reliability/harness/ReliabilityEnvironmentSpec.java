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
package org.alfresco.hxi_connector.e2e_test.reliability.harness;

/**
 * Topology + opt-out toggles for a {@link ReliabilityEnvironment}. The opt-out flags exist so legacy-behaviour ITs can flip a now-default-on reliability property back off. All flags default to {@code false} — the resulting env tracks the production-default deployment shape.
 *
 * @param withTransformTopology
 *            see {@link ReliabilityEnvironment.Builder#withTransformTopology()}
 * @param withTransformResponseDeadLetterDisabled
 *            see {@link ReliabilityEnvironment.Builder#withTransformResponseDeadLetterDisabled()}
 * @param withTransformResponseThrowFailedTransformsDisabled
 *            see {@link ReliabilityEnvironment.Builder#withTransformResponseThrowFailedTransformsDisabled()}
 * @param withRepoEventsDeadLetterUnsupportedTypesDisabled
 *            see {@link ReliabilityEnvironment.Builder#withRepoEventsDeadLetterUnsupportedTypesDisabled()}
 */
@SuppressWarnings("PMD.LongVariable")
public record ReliabilityEnvironmentSpec(
        boolean withTransformTopology,
        boolean withTransformResponseDeadLetterDisabled,
        boolean withTransformResponseThrowFailedTransformsDisabled,
        boolean withRepoEventsDeadLetterUnsupportedTypesDisabled)
{
    public static ReliabilityEnvironmentSpec defaultSpec()
    {
        return new ReliabilityEnvironmentSpec(false, false, false, false);
    }
}
