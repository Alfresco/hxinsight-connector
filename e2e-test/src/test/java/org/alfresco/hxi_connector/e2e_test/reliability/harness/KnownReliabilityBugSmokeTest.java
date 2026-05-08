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

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

/**
 * Smoke test for {@link KnownReliabilityBug}: proves that {@link KnownReliabilityBugCondition} actually disables annotated tests at runtime. Runs as a plain unit test (not an IT) so it executes in {@code mvn test} without Docker.
 *
 * <p>
 * If the annotation/condition wiring breaks, the {@link #shouldBeSkippedByCondition()} method will execute, hit {@code fail(...)}, and turn the build red — which is exactly the regression we want to catch.
 */
class KnownReliabilityBugSmokeTest
{
    @Test
    @KnownReliabilityBug(id = "RB-000", description = "Smoke: this method must NOT execute", jira = "ACS-N/A")
    void shouldBeSkippedByCondition()
    {
        fail("KnownReliabilityBugCondition did not disable this test — annotation wiring is broken");
    }

    @Test
    void shouldRunWithoutAnnotation()
    {
        // Pure presence check: surfaces "1 of 2 tests skipped, 1 passed" in surefire output, which is the assertion that the condition only fires when the annotation is present.
    }
}
