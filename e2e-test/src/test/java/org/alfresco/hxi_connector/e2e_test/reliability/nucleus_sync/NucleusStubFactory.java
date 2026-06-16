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
package org.alfresco.hxi_connector.e2e_test.reliability.nucleus_sync;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;

import java.util.List;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import lombok.extern.slf4j.Slf4j;

/**
 * Encapsulates all WireMock stub registration logic for nucleus-sync reliability tests. Each method registers one or more stubs and appends them to the provided tracking list so they can be cleaned up after each test.
 *
 * <p>
 * Single Responsibility: this class only knows HOW to register stubs — it does not manage lifecycle, assertions, or environment concerns.
 * </p>
 */
@Slf4j
public class NucleusStubFactory
{
    private final WireMock wireMock;
    private final List<StubMapping> registeredStubs;
    private final int acsPageSize;
    private final int nucleusPageSize;
    private final int stubPriority;

    // Nucleus endpoint paths
    private final String userMappingsPath;
    private final String groupsPath;
    private final String groupMembersPath;

    public NucleusStubFactory(
            WireMock wireMock,
            List<StubMapping> registeredStubs,
            String systemId,
            int acsPageSize,
            int nucleusPageSize,
            int stubPriority)
    {
        this.wireMock = wireMock;
        this.registeredStubs = registeredStubs;
        this.acsPageSize = acsPageSize;
        this.nucleusPageSize = nucleusPageSize;
        this.stubPriority = stubPriority;
        this.userMappingsPath = "/system-integrations/systems/" + systemId + "/user-mappings";
        this.groupsPath = "/system-integrations/systems/" + systemId + "/groups";
        this.groupMembersPath = "/system-integrations/systems/" + systemId + "/group-members";
    }

    // --- Auth ---

    public void installNucleusAuthStub()
    {
        StubMapping stub = wireMock.register(post(urlEqualTo("/token"))
                .atPriority(stubPriority)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "access_token": "stub-access-token",
                                  "expires_in": 3600,
                                  "token_type": "Bearer",
                                  "scope": "iam.user-data.account system-integrations-config system-integrations-runtime environment_authorization"
                                }
                                """)));
        registeredStubs.add(stub);
    }

    // --- ACS People ---

    public void installSingleAcsUserStub()
    {
        StubMapping stub = wireMock.register(
                get(urlPathMatching("/alfresco/api/-default-/public/alfresco/versions/1/people"))
                        .atPriority(stubPriority)
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("""
                                        {
                                          "list": {
                                            "pagination": {
                                              "count": 1,
                                              "hasMoreItems": false,
                                              "totalItems": 1,
                                              "skipCount": 0,
                                              "maxItems": 100
                                            },
                                            "entries": [
                                              {
                                                "entry": {
                                                  "id": "user1",
                                                  "email": "abcd@hyland.com",
                                                  "enabled": true
                                                }
                                              }
                                            ]
                                          }
                                        }
                                        """)));
        registeredStubs.add(stub);
    }

    public void installAcsPeopleStubs(int totalUserCount)
    {
        int pageCount = (totalUserCount + acsPageSize - 1) / acsPageSize;
        log.info("[scale-test] Installing {} ACS /people page stubs for {} users (page size {})",
                pageCount, totalUserCount, acsPageSize);

        for (int page = 0; page < pageCount; page++)
        {
            int skipCount = page * acsPageSize;
            int remaining = totalUserCount - skipCount;
            int count = Math.min(acsPageSize, remaining);
            boolean hasMoreItems = skipCount + count < totalUserCount;

            String responseBody = StubResponseBuilder.buildAcsPeoplePageJson(
                    skipCount, count, hasMoreItems, totalUserCount, acsPageSize);

            StubMapping stub = wireMock.register(
                    get(urlPathEqualTo("/alfresco/api/-default-/public/alfresco/versions/1/people"))
                            .withQueryParam("skipCount", equalTo(String.valueOf(skipCount)))
                            .atPriority(stubPriority)
                            .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody(responseBody)));
            registeredStubs.add(stub);

            if (page % 1000 == 0)
            {
                log.debug("[scale-test] Installed ACS /people stub for page {} (skipCount={})", page, skipCount);
            }
        }
        log.info("[scale-test] Finished installing {} ACS /people stubs", pageCount);
    }

    // --- ACS User Groups ---

    public void installAcsUserGroupsStub()
    {
        StubMapping stub = wireMock.register(
                get(urlPathMatching("/alfresco/api/-default-/public/alfresco/versions/1/people/[^/]+/groups"))
                        .atPriority(stubPriority)
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"list\":{\"pagination\":{\"count\":0,\"hasMoreItems\":false,\"totalItems\":0,\"skipCount\":0,\"maxItems\":100},\"entries\":[]}}")));
        registeredStubs.add(stub);
    }

    public void installAcsUserGroupStubWithGroupId(String sharedGroupId)
    {
        String body = "{\"list\":{"
                + "\"pagination\":{\"count\":1,\"hasMoreItems\":false,\"totalItems\":1,\"skipCount\":0,\"maxItems\":100},"
                + "\"entries\":[{\"entry\":{\"id\":\"" + sharedGroupId + "\"}}]"
                + "}}";
        StubMapping stub = wireMock.register(
                get(urlPathMatching("/alfresco/api/-default-/public/alfresco/versions/1/people/user[0-9]+/groups"))
                        .atPriority(stubPriority)
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(body)));
        registeredStubs.add(stub);
    }

    public void installRepositoryGroupStubs(int totalGroupsCount)
    {
        int pageCount = (totalGroupsCount + acsPageSize - 1) / acsPageSize;
        log.info("[scale-test] Installing {} ACS /groups page stubs for {} groups (page size {})",
                pageCount, totalGroupsCount, acsPageSize);

        for (int page = 0; page < pageCount; page++)
        {
            int skipCount = page * acsPageSize;
            int remaining = totalGroupsCount - skipCount;
            int count = Math.min(acsPageSize, remaining);
            boolean hasMoreItems = skipCount + count < totalGroupsCount;

            String responseBody = StubResponseBuilder.buildAcsGroupPageJson(
                    skipCount, count, hasMoreItems, totalGroupsCount, acsPageSize);

            StubMapping stub = wireMock.register(
                    get(urlPathEqualTo("/alfresco/api/-default-/public/alfresco/versions/1/people/user1/groups"))
                            .withQueryParam("skipCount", equalTo(String.valueOf(skipCount)))
                            .atPriority(stubPriority)
                            .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody(responseBody)));
            registeredStubs.add(stub);

            if (page % 1000 == 0)
            {
                log.debug("[scale-test] Installed ACS /groups stub for page {} (skipCount={})", page, skipCount);
            }
        }
        log.info("[scale-test] Finished installing {} ACS /groups stubs", pageCount);
    }

    // --- Nucleus IAM Users ---

    public void installNucleusIamUsersStubs(int totalUserCount)
    {
        int pageCount = (totalUserCount + nucleusPageSize - 1) / nucleusPageSize;
        log.info("[scale-test] Installing {} Nucleus /api/users page stubs for {} users (page size {})",
                pageCount, totalUserCount, nucleusPageSize);

        int firstPageSize = Math.min(nucleusPageSize, totalUserCount);
        boolean hasNext = totalUserCount > nucleusPageSize;
        String firstPageUsers = StubResponseBuilder.buildNucleusUsersJson(0, firstPageSize);
        String firstPageNext = hasNext
                ? ",\"next\":\"/api/users?limit=" + nucleusPageSize + "&offset=" + nucleusPageSize + "\""
                : "";

        StubMapping firstStub = wireMock.register(
                get(urlPathEqualTo("/api/users"))
                        .withQueryParam("offset", absent())
                        .atPriority(stubPriority)
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"users\":[" + firstPageUsers + "]" + firstPageNext + "}")));
        registeredStubs.add(firstStub);

        for (int page = 1; page < pageCount; page++)
        {
            int offset = page * nucleusPageSize;
            int remaining = totalUserCount - offset;
            int count = Math.min(nucleusPageSize, remaining);
            boolean hasMore = offset + count < totalUserCount;

            String pageUsers = StubResponseBuilder.buildNucleusUsersJson(offset, count);
            String nextLink = hasMore
                    ? ",\"next\":\"/api/users?limit=" + nucleusPageSize + "&offset=" + (offset + nucleusPageSize) + "\""
                    : "";

            StubMapping stub = wireMock.register(
                    get(urlPathEqualTo("/api/users"))
                            .withQueryParam("offset", equalTo(String.valueOf(offset)))
                            .atPriority(stubPriority)
                            .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("{\"users\":[" + pageUsers + "]" + nextLink + "}")));
            registeredStubs.add(stub);

            if (page % 100 == 0)
            {
                log.debug("[scale-test] Installed Nucleus /api/users stub for page {} (offset={})", page, offset);
            }
        }
        log.info("[scale-test] Finished installing {} Nucleus /api/users stubs", pageCount);
    }

    public void installNucleusSingleUserStub()
    {
        StubMapping users = wireMock.register(get(urlPathEqualTo("/api/users"))
                .atPriority(stubPriority)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "users": [
                                    {
                                      "userName": "asmith",
                                      "userId": "iam-1234",
                                      "email": "abcd@hyland.com"
                                    }
                                  ]
                                }
                                """)));
        registeredStubs.add(users);
    }

    // --- Nucleus Sync Endpoints (empty GETs + mutation POSTs) ---

    public void installEmptyMappingsStub()
    {
        StubMapping stub = wireMock.register(get(urlPathEqualTo(userMappingsPath))
                .atPriority(stubPriority)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"items\": [] }")));
        registeredStubs.add(stub);
    }

    public void installEmptyGroupsStub()
    {
        StubMapping stub = wireMock.register(get(urlPathEqualTo(groupsPath))
                .atPriority(stubPriority)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"items\": [] }")));
        registeredStubs.add(stub);
    }

    public void installEmptyGroupMembersStub()
    {
        StubMapping stub = wireMock.register(get(urlPathEqualTo(groupMembersPath))
                .atPriority(stubPriority)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"items\": [] }")));
        registeredStubs.add(stub);
    }

    public void installMutationEndpointsWithTracking()
    {
        StubMapping mappings = wireMock.register(post(urlEqualTo(userMappingsPath))
                .atPriority(stubPriority)
                .willReturn(aResponse().withStatus(200).withBody("{}")));
        registeredStubs.add(mappings);

        StubMapping groups = wireMock.register(post(urlEqualTo(groupsPath))
                .atPriority(stubPriority)
                .willReturn(aResponse().withStatus(200).withBody("{}")));
        registeredStubs.add(groups);

        StubMapping members = wireMock.register(post(urlEqualTo(groupMembersPath))
                .atPriority(stubPriority)
                .willReturn(aResponse().withStatus(200).withBody("{}")));
        registeredStubs.add(members);
    }
}
