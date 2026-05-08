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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Marks a reliability test that documents a known, currently unfixed bug. The annotated test is skipped by default (via {@link KnownReliabilityBugCondition}) but can be opted in for a one-off run to detect accidental upstream fixes:
 *
 * <pre>{@code
 * mvn -pl e2e-test verify -Preliability-tests \
 *     -Dgroups=known-reliability-bug -DrunKnownReliabilityBugs=true
 * }</pre>
 *
 * <p>
 * The {@code @Tag("known-reliability-bug")} narrows the executed set to just these tests so the run is fast; {@code -DrunKnownReliabilityBugs=true} flips {@link KnownReliabilityBugCondition} from "disabled" to "enabled" for them. Both are needed because JUnit evaluates execution conditions independently of tag filters.
 *
 * <p>
 * Every usage MUST cite a stable bug identifier (e.g. {@code RB-001}) tracked in the reliability bug log. When the underlying bug is fixed, remove the annotation so the test runs by default and acts as a regression guard.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Tag("known-reliability-bug")
@ExtendWith(KnownReliabilityBugCondition.class)
public @interface KnownReliabilityBug
{
    /**
     * Stable bug identifier as tracked in the reliability bug log, e.g. {@code "RB-001"}.
     */
    String id();

    /**
     * One-line description; should match the headline used in the bugs doc.
     */
    String description();

    /**
     * Jira key tracking the fix once raised. Use {@code "ACS-TBD"} while no Jira exists.
     */
    String jira() default "ACS-TBD";
}
