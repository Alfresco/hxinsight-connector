/*
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
package org.alfresco.hxi_connector.common.config.properties;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.exception.EndpointServerErrorException;

class RetryTest
{

    @Test
    void shouldCreateRetryWithDefaults()
    {
        Retry retry = new Retry();

        assertThat(retry.attempts()).isEqualTo(10);
        assertThat(retry.initialDelay()).isEqualTo(500);
        assertThat(retry.delayMultiplier()).isEqualTo(2.0);
        assertThat(retry.reasons()).isNotEmpty();
        assertThat(retry.reasons()).contains(EndpointServerErrorException.class);
    }

    @Test
    void shouldCreateRetryWithCustomValues()
    {
        Retry retry = new Retry(5, 1000, 3.0);

        assertThat(retry.attempts()).isEqualTo(5);
        assertThat(retry.initialDelay()).isEqualTo(1000);
        assertThat(retry.delayMultiplier()).isEqualTo(3.0);
        assertThat(retry.reasons()).isNotEmpty();
    }

    @Test
    void shouldCreateRetryWithCustomReasons()
    {
        Set<Class<? extends Throwable>> reasons = Set.of(IllegalStateException.class);

        Retry retry = new Retry(3, 200, 1.5, reasons);

        assertThat(retry.attempts()).isEqualTo(3);
        assertThat(retry.initialDelay()).isEqualTo(200);
        assertThat(retry.delayMultiplier()).isEqualTo(1.5);
        assertThat(retry.reasons()).containsExactlyInAnyOrder(IllegalStateException.class);
    }
}
