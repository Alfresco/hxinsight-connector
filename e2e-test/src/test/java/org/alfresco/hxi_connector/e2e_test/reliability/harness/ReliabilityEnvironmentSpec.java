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
 * Immutable description of which reliability-fix opt-in toggles a {@link ReliabilityEnvironment} should boot with. Drives both topology choices (which containers to start) and the live-ingester environment-variable map ({@link LiveIngesterEnvVars#forSpec(ReliabilityEnvironmentSpec)}).
 *
 * <p>
 * All toggles default to {@code false}. The default spec ({@code new ReliabilityEnvironmentSpec(false, false, false, false)}) gives a minimal env equivalent to the production-default deployment shape.
 *
 * @param withTransformTopology
 *            see {@link ReliabilityEnvironment.Builder#withTransformTopology()}
 * @param withTransformResponseDeadLetterEnabled
 *            see {@link ReliabilityEnvironment.Builder#withTransformResponseDeadLetterEnabled()}
 * @param withTransformResponseThrowFailedTransforms
 *            see {@link ReliabilityEnvironment.Builder#withTransformResponseThrowFailedTransforms()}
 * @param withRepoEventsDeadLetterUnsupportedTypes
 *            see {@link ReliabilityEnvironment.Builder#withRepoEventsDeadLetterUnsupportedTypes()}
 */
@SuppressWarnings("PMD.LongVariable")
public record ReliabilityEnvironmentSpec(
        boolean withTransformTopology,
        boolean withTransformResponseDeadLetterEnabled,
        boolean withTransformResponseThrowFailedTransforms,
        boolean withRepoEventsDeadLetterUnsupportedTypes)
{
    public static ReliabilityEnvironmentSpec defaultSpec()
    {
        return new ReliabilityEnvironmentSpec(false, false, false, false);
    }
}
