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
package org.alfresco.hxi_connector.live_ingester.adapters.config.health;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.apache.camel.ServiceStatus;
import org.apache.camel.spi.RouteController;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

/**
 * Reports {@code UP} only after every Camel route registered on the {@link CamelContext} has transitioned to {@link ServiceStatus#Started}. Wired into the {@code readiness} health group via {@code management.endpoint.health.group.readiness.include} so the readiness probe ({@code /actuator/health/readiness}) does not flip to {@code UP} until the connector is actually consuming JMS events.
 *
 * <p>
 * Background: Spring Boot's default readiness state flips to {@code ACCEPTING_TRAFFIC} on {@code ApplicationReadyEvent}. The connector configures Camel with {@code camel.main.auto-startup=false} so routes do not start with the context — they are started by {@link org.alfresco.hxi_connector.common.adapters.messaging.repository.ProcessingStarter} on the first {@code AcsHealthy} event from {@code AcsHealthProbe}. There is therefore a window — Spring boot complete, ACS health probe still polling — during which the readiness probe would falsely report {@code UP} while the JMS subscription on {@code alfresco.repo.event2} is not yet established. Operators using {@code /actuator/health/readiness} as a Kubernetes readinessProbe (or Testcontainers' {@code Wait.forHttp} during integration tests) would see "ready" before the connector is consuming events. This indicator closes that gap by reporting {@code OUT_OF_SERVICE} until {@code RouteController.getRouteStatus(routeId) == Started} for every
 * registered route.
 *
 * <p>
 * Liveness is unaffected — {@code /actuator/health/liveness} continues to track process liveness only.
 */
@RequiredArgsConstructor
public class CamelRoutesReadyHealthIndicator implements HealthIndicator
{
    private final CamelContext camelContext;

    @Override
    public Health health()
    {
        RouteController routeController = camelContext.getRouteController();
        if (routeController.isStartingRoutes())
        {
            return Health.outOfService()
                    .withDetail("status", "starting")
                    .build();
        }

        List<Route> routes = camelContext.getRoutes();
        if (routes.isEmpty())
        {
            return Health.outOfService()
                    .withDetail("status", "no-routes-registered")
                    .build();
        }

        long startedCount = routes.stream()
                .map(Route::getId)
                .map(routeController::getRouteStatus)
                .filter(ServiceStatus.Started::equals)
                .count();
        if (startedCount == routes.size())
        {
            return Health.up()
                    .withDetail("started", startedCount)
                    .withDetail("total", routes.size())
                    .build();
        }
        return Health.outOfService()
                .withDetail("status", "partially-started")
                .withDetail("started", startedCount)
                .withDetail("total", routes.size())
                .build();
    }
}
