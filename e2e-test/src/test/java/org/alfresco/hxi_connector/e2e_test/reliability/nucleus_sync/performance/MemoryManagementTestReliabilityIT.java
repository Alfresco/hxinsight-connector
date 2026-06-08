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
package org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync.performance;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.common.test.util.RetryUtils;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.ActuatorMetricsProbe;
import org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync.BaseNucleusSyncLargeIngestionIT;

// During 1M user Mapping Test the JVM Memory
@Slf4j
public class MemoryManagementTestReliabilityIT extends BaseNucleusSyncLargeIngestionIT
{

    // First Mock the same 1M users Count

    private static final long MAX_MEMORY_CONSUMPTION_BYTES = 4_000_000_000L; // 4 GB
    private static final long MAX_TIME_BEFORE_FAIL = 20L; // 20 minutes

    @Test
    public void testLargeScaleUserMappingMemoryConsumption() throws Exception
    {
        installAllStubs();
        ActuatorMetricsProbe metricsProbe = environment.nucleusSyncMetrics();
        CompletableFuture<Void> syncTask = CompletableFuture.runAsync(() -> {
            environment.nucleusSyncClient().startSynchronization();
        });

        List<Double> memorySnapShorts = new ArrayList<>();
        // Take memory SnapShort during different Periods for next
        while (!syncTask.isDone())
        {
            memorySnapShorts.add(snapshot(metricsProbe, "during-sync").heapBytes);
            Thread.sleep(50);
        }
        // Check the memory consumption after the sync is complete
        double finalHeapBytes = snapshot(metricsProbe, "post-sync").heapBytes;
        log.info("[reliability] Final heap memory consumption: {} bytes", (long) finalHeapBytes);
        // Find Average and Median for the memory consumption during the sync

        syncTask.get(MAX_TIME_BEFORE_FAIL, TimeUnit.MINUTES);

        RetryUtils.assertWithRetry(() -> {
            double averageMemoryConsumption = findAverageMemoryConsumption(memorySnapShorts);
            double medianMemoryConsumption = findMedianMemoryConsumption(memorySnapShorts);
            double maxMemoryConsumption = findMaxMemoryConsumption(memorySnapShorts);
            double minMemoryConsumption = findMinMemoryConsumption(memorySnapShorts);
            double stdDevMemoryConsumption = findStandardDeviationMemoryConsumption(memorySnapShorts);

            log.info("[reliability] Memory Consumption during sync - Average: {} bytes, Median: {} bytes, Max: {} bytes, Min: {} bytes, Std Dev: {} bytes",
                    (long) averageMemoryConsumption, (long) medianMemoryConsumption, (long) maxMemoryConsumption, (long) minMemoryConsumption, (long) stdDevMemoryConsumption);

            assertThat(finalHeapBytes).isLessThan(MAX_MEMORY_CONSUMPTION_BYTES);
        });
    }

    private double findAverageMemoryConsumption(List<Double> list)
    {
        return list.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private double findMedianMemoryConsumption(List<Double> list)
    {
        return list.stream().mapToDouble(Double::doubleValue).sorted().skip((list.size() - 1) / 2).limit(2 - list.size() % 2).average().orElse(0.0);
    }

    private double findMaxMemoryConsumption(List<Double> list)
    {
        return list.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    private double findMinMemoryConsumption(List<Double> list)
    {
        return list.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
    }

    private double findStandardDeviationMemoryConsumption(List<Double> list)
    {
        double meanMemoryConsumption = findAverageMemoryConsumption(list);
        return list.stream().mapToDouble(Double::doubleValue)
                .map(memory -> Math.pow(memory - meanMemoryConsumption, 2))
                .average()
                .orElse(0.0);
    }

    private static MemoryResourceSnapShort snapshot(ActuatorMetricsProbe metrics, String label)
    {
        double threads = metrics.gaugeValue("jvm.threads.live");
        double heapBytes = metrics.gaugeValue("jvm.memory.used", "area", "heap");
        double loadedClasses = metrics.gaugeValue("jvm.classes.loaded");
        log.info("[reliability] {} snapshot: threads={}, heap={} bytes, classes={}",
                label, (int) threads, (long) heapBytes, (int) loadedClasses);
        return new MemoryResourceSnapShort(threads, heapBytes);
    }

    protected void installAllStubs()
    {
        // 1. Install all stubs
        installNucleusAuthStub();
        installAcsPeopleStubs(TOTAL_USER_COUNT);
        installAcsUserGroupsStub();
        installNucleusIamUsersStubs(TOTAL_USER_COUNT);
        installEmptyMappingsStub();
        installEmptyGroupsStub();
        installEmptyGroupMembersStub();
        installMutationEndpointsWithTracking();
    }

    record MemoryResourceSnapShort(double threads, double heapBytes)
    {}
}
