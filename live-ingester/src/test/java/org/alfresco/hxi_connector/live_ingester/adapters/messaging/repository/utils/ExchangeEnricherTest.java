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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.utils;

import static org.mockito.BDDMockito.then;

import static org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.utils.ExchangeEnricher.UPDATED_EVENT_TYPE_PROP;
import static org.alfresco.repo.event.v1.model.EventType.NODE_CREATED;
import static org.alfresco.repo.event.v1.model.EventType.NODE_DELETED;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
class ExchangeEnricherTest
{

    @Mock
    private Exchange mockExchange;

    @Test
    void whenFilteringResultHasNotChanged_thenExchangeIsNotEnriched()
    {
        // when
        ExchangeEnricher.enrichExchangeAfterFiltering(mockExchange, true, true);

        then(mockExchange).shouldHaveNoInteractions();

        // when
        ExchangeEnricher.enrichExchangeAfterFiltering(mockExchange, false, false);

        then(mockExchange).shouldHaveNoInteractions();
    }

    @Test
    void whenFilteringResultChangedFromAllowedToDenied_thenExchangeEnrichedWithDeleteProperty()
    {
        // when
        ExchangeEnricher.enrichExchangeAfterFiltering(mockExchange, true, false);

        then(mockExchange).should().setProperty(UPDATED_EVENT_TYPE_PROP, NODE_DELETED.getType());
        then(mockExchange).shouldHaveNoMoreInteractions();
    }

    @Test
    void whenFilteringResultChangedFromDeniedToAllowed_thenExchangeEnrichedWithCreateProperty()
    {
        // when
        ExchangeEnricher.enrichExchangeAfterFiltering(mockExchange, false, true);

        then(mockExchange).should().setProperty(UPDATED_EVENT_TYPE_PROP, NODE_CREATED.getType());
        then(mockExchange).shouldHaveNoMoreInteractions();
    }
}
