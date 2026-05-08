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

import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * JUnit 5 {@link ExecutionCondition} backing {@link KnownReliabilityBug}. When the annotation is present on the test method (or its enclosing class) the condition disables the test with a reason that surfaces the bug ID, Jira key, and description in the surefire output.
 *
 * <p>
 * The annotated test can still be opted in for a one-off run by setting the system property {@code -DrunKnownReliabilityBugs=true} (e.g. when periodically checking whether an upstream change has accidentally fixed the bug). Tag-based selection alone (e.g. {@code -Dgroups=known-reliability-bug}) is <i>not</i> sufficient because JUnit evaluates execution conditions independently of tag filters.
 */
public final class KnownReliabilityBugCondition implements ExecutionCondition
{
    static final String OPT_IN_PROPERTY = "runKnownReliabilityBugs";

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context)
    {
        Optional<KnownReliabilityBug> bug = context.getElement()
                .flatMap(element -> AnnotationSupport.findAnnotation(element, KnownReliabilityBug.class));
        if (bug.isEmpty())
        {
            return ConditionEvaluationResult.enabled("No @KnownReliabilityBug annotation present");
        }
        if (Boolean.parseBoolean(System.getProperty(OPT_IN_PROPERTY)))
        {
            return ConditionEvaluationResult.enabled(
                    "-D%s=true: opting in @KnownReliabilityBug(%s)".formatted(OPT_IN_PROPERTY, bug.get().id()));
        }
        KnownReliabilityBug b = bug.get();
        return ConditionEvaluationResult.disabled(
                "Tracked as %s (%s): %s".formatted(b.id(), b.jira(), b.description()));
    }
}
