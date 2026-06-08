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
package org.alfresco.hxi_connector.e2e_test.reliability.NucleusSync.Performance;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.alfresco.hxi_connector.e2e_test.reliability.NucleusSync.BaseNucleusSyncLargeIngestionIT;
import org.alfresco.hxi_connector.e2e_test.reliability.harness.ActuatorMetricsProbe;

/**
 * Measures JVM and system CPU utilization during the 1M-user mapping sync. Detects CPU bottlenecks — excessive serialization, tight spin loops, or unbounded parallelism that saturates all available cores.
 *
 * <p>
 * Asserts that:
 * <ul>
 * <li>p95 JVM CPU usage stays below {@value #MAX_P95_CPU_PERCENTAGE}%</li>
 * <li>Average JVM CPU usage stays below {@value #MAX_AVG_CPU_PERCENTAGE}%</li>
 * </ul>
 */
@Slf4j
public class CpuManagementTestReliabilityIT extends BaseNucleusSyncLargeIngestionIT
{

    private static final long MAX_TIME_BEFORE_FAIL = 20L; // minutes
    private static final long SAMPLE_INTERVAL_MS = 1_000L; // 1s between samples — avoids observer effect

    /**
     * p95 threshold: allows short spikes (GC, burst batching) but catches sustained saturation.
     */
    private static final double MAX_P95_CPU_PERCENTAGE = 90.0;
    /**
     * Average threshold: catches sustained high CPU that indicates an algorithmic bottleneck.
     */
    private static final double MAX_AVG_CPU_PERCENTAGE = 70.0;

    @Test
    public void shouldNotSaturateCpuDuringLargeScaleSync() throws Exception
    {
        installAllStubs();

        ActuatorMetricsProbe nucleusSyncProbe = environment.nucleusSyncMetrics();
        List<Double> jvmCpuSamples = new ArrayList<>();
        List<Double> systemCpuSamples = new ArrayList<>();

        long startMs = System.currentTimeMillis();
        CompletableFuture<Void> syncJob = CompletableFuture.runAsync(
                () -> environment.nucleusSyncClient().startSynchronization());

        while (!syncJob.isDone())
        {
            CpuSnapshot snap = snapshot(nucleusSyncProbe, "during-sync");
            jvmCpuSamples.add(snap.jvmCpuPct);
            systemCpuSamples.add(snap.systemCpuPct);
            Thread.sleep(SAMPLE_INTERVAL_MS);
        }

        syncJob.get(MAX_TIME_BEFORE_FAIL, TimeUnit.MINUTES);
        long elapsedSec = (System.currentTimeMillis() - startMs) / 1000;

        // Compute statistics
        double avgJvmCpu = average(jvmCpuSamples);
        double maxJvmCpu = max(jvmCpuSamples);
        double p95JvmCpu = percentile(jvmCpuSamples, 95);
        double p50JvmCpu = percentile(jvmCpuSamples, 50);

        double avgSystemCpu = average(systemCpuSamples);
        double maxSystemCpu = max(systemCpuSamples);

        log.info("[reliability] === CPU PRESSURE RESULTS ({} samples over {} sec) ===", jvmCpuSamples.size(), elapsedSec);
        log.info("[reliability] JVM  CPU: avg={}%, p50={}%, p95={}%, max={}%",
                String.format("%.1f", avgJvmCpu), String.format("%.1f", p50JvmCpu),
                String.format("%.1f", p95JvmCpu), String.format("%.1f", maxJvmCpu));
        log.info("[reliability] System CPU: avg={}%, max={}%",
                String.format("%.1f", avgSystemCpu), String.format("%.1f", maxSystemCpu));

        // Assertions — p95 is more meaningful than raw max (single GC spike won't fail the test)
        assertThat(p95JvmCpu)
                .as("p95 JVM CPU usage (%.1f%%) should stay below %s%% — sustained saturation indicates "
                        + "a CPU bottleneck (tight loops, excessive serialization, or unbounded parallelism)",
                        p95JvmCpu, MAX_P95_CPU_PERCENTAGE)
                .isLessThan(MAX_P95_CPU_PERCENTAGE);

        assertThat(avgJvmCpu)
                .as("Average JVM CPU usage (%.1f%%) should stay below %s%% — high average indicates "
                        + "the workload is compute-bound rather than I/O-bound as expected",
                        avgJvmCpu, MAX_AVG_CPU_PERCENTAGE)
                .isLessThan(MAX_AVG_CPU_PERCENTAGE);
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────────

    private CpuSnapshot snapshot(ActuatorMetricsProbe metrics, String label)
    {
        // Actuator returns 0.0–1.0 fractions; convert to percentage for readability
        double systemCpuPct = metrics.gaugeValue("system.cpu.usage") * 100.0;
        double jvmCpuPct = metrics.gaugeValue("process.cpu.usage") * 100.0;
        log.debug("[reliability] {} CPU snapshot: system={}%, jvm={}%", label,
                String.format("%.1f", systemCpuPct), String.format("%.1f", jvmCpuPct));
        return new CpuSnapshot(systemCpuPct, jvmCpuPct);
    }

    private static double average(List<Double> samples)
    {
        return samples.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private static double max(List<Double> samples)
    {
        return samples.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    private static double percentile(List<Double> samples, int p)
    {
        if (samples.isEmpty())
            return 0.0;
        List<Double> sorted = new ArrayList<>(samples);
        Collections.sort(sorted);
        int idx = Math.min(sorted.size() - 1, (int) Math.ceil(p / 100.0 * sorted.size()) - 1);
        return sorted.get(Math.max(0, idx));
    }

    protected void installAllStubs()
    {
        installNucleusAuthStub();
        installAcsPeopleStubs(TOTAL_USER_COUNT);
        installAcsUserGroupsStub();
        installNucleusIamUsersStubs(TOTAL_USER_COUNT);
        installEmptyMappingsStub();
        installEmptyGroupsStub();
        installEmptyGroupMembersStub();
        installMutationEndpointsWithTracking();
    }

    private record CpuSnapshot(double systemCpuPct, double jvmCpuPct)
    {}
}
