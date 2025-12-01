/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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
package org.alfresco.hxi_connector.nucleus_sync.services.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.alfresco.hxi_connector.nucleus_sync.client.AlfrescoClient;
import org.alfresco.hxi_connector.nucleus_sync.model.UserMapping;

@SpringBootTest(classes = UserGroupMembershipCacheBuilderService.class)
class UserGroupMembershipCacheBuilderServiceIntegrationTest
{
    @MockitoBean
    private AlfrescoClient alfrescoClient;

    @Autowired
    private UserGroupMembershipCacheBuilderService service;

    @Test
    void shouldProcessMultipleUsersInParallel() throws InterruptedException
    {
        // Given
        int userCount = 10;
        List<UserMapping> users = generateUsers(userCount);

        AtomicInteger concurrentCalls = new AtomicInteger(0);
        AtomicInteger maxConcurrency = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(userCount);

        when(alfrescoClient.getUserGroups(anyString())).thenAnswer(invocation -> {
            int current = concurrentCalls.incrementAndGet();
            maxConcurrency.updateAndGet(max -> Math.max(max, current));

            // Simulate API delay
            Thread.sleep(100);

            concurrentCalls.decrementAndGet();
            latch.countDown();
            return List.of("group1");
        });

        // When
        long startTime = System.currentTimeMillis();
        Map<String, List<String>> result = service.buildCacheFromAlfresco(users);
        long duration = System.currentTimeMillis() - startTime;

        // Then
        assertThat(result).hasSize(userCount);
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();

        // With parallel execution, duration should be much less than sequential (10 * 100ms)
        // This will fail when manually debugging
        assertThat(duration).isLessThan(500);

        // Verify actual parallelism occurred
        assertThat(maxConcurrency.get()).isGreaterThan(1);
    }

    private List<UserMapping> generateUsers(int count)
    {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new UserMapping("user" + i + "@email.com", "user" + i, UUID.randomUUID().toString()))
                .toList();
    }
}
