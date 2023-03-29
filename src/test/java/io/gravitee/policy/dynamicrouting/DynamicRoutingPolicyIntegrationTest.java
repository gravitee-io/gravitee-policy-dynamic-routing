/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.dynamicrouting;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.gravitee.apim.gateway.tests.sdk.AbstractPolicyTest;
import io.gravitee.apim.gateway.tests.sdk.annotations.DeployApi;
import io.gravitee.apim.gateway.tests.sdk.annotations.GatewayTest;
import io.gravitee.apim.gateway.tests.sdk.configuration.GatewayConfigurationBuilder;
import io.gravitee.definition.model.Api;
import io.gravitee.definition.model.ExecutionMode;
import io.gravitee.gateway.reactor.ReactableApi;
import io.gravitee.policy.dynamicrouting.configuration.DynamicRoutingPolicyConfiguration;
import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava3.core.http.HttpClient;
import io.vertx.rxjava3.core.http.HttpClientRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@GatewayTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DynamicRoutingPolicyIntegrationTest extends AbstractPolicyTest<DynamicRoutingPolicy, DynamicRoutingPolicyConfiguration> {

    private final WireMockServer group1WireMock = new WireMockServer(wireMockConfig().dynamicPort());
    private final WireMockServer absoluteUrlWireMock = new WireMockServer(wireMockConfig().dynamicPort());

    @Override
    protected void configureGateway(GatewayConfigurationBuilder gatewayConfigurationBuilder) {
        super.configureGateway(gatewayConfigurationBuilder);
        gatewayConfigurationBuilder.set("api.jupiterMode.enabled", "true");
    }

    @Override
    public void configureApi(Api api) {
        super.configureApi(api);
        api.setExecutionMode(ExecutionMode.JUPITER);
    }

    @BeforeAll
    void beforeAll() {
        group1WireMock.start();
        absoluteUrlWireMock.start();
    }

    @AfterAll
    void afterAll() {
        group1WireMock.stop();
        absoluteUrlWireMock.stop();
    }

    @Test
    @DeployApi(value = { "/apis/v2/with_endpoint.json" })
    public void should_route_to_another_group_when_request_match(HttpClient client) throws InterruptedException {
        group1WireMock.stubFor(get("/my-store").willReturn(ok()));

        client
            .rxRequest(HttpMethod.GET, "/with_endpoint/v1/stores/my-store")
            .flatMap(HttpClientRequest::rxSend)
            .flatMapPublisher(response -> {
                assertThat(response.statusCode()).isEqualTo(200);
                return response.toFlowable();
            })
            .test()
            .await()
            .assertComplete()
            .assertNoErrors();
    }

    @Test
    @DeployApi(value = { "/apis/v2/with_absolute_url.json" })
    public void should_route_to_url_when_request_match(HttpClient client) throws InterruptedException {
        absoluteUrlWireMock.stubFor(get("/").willReturn(ok()));

        client
            .rxRequest(HttpMethod.GET, "/with_absolute_url/")
            .flatMap(HttpClientRequest::rxSend)
            .flatMap(response -> {
                assertThat(response.statusCode()).isEqualTo(200);
                return response.body();
            })
            .test()
            .await()
            .assertComplete()
            .assertNoErrors();
    }

    @Test
    @DeployApi(value = { "/apis/v2/with_endpoint.json" })
    public void should_return_400_when_request_not_match(HttpClient client) throws InterruptedException {
        client
            .rxRequest(HttpMethod.GET, "/with_endpoint/unknown")
            .flatMap(HttpClientRequest::rxSend)
            .flatMapPublisher(response -> {
                assertThat(response.statusCode()).isEqualTo(400);
                return response.toFlowable();
            })
            .test()
            .await()
            .assertComplete()
            .assertNoErrors();
    }

    @Override
    public void configureApi(ReactableApi<?> api, Class<?> definitionClass) {
        super.configureApi(api, definitionClass);

        if (definitionClass.isAssignableFrom(Api.class)) {
            var apiDefinition = (Api) api.getDefinition();

            apiDefinition
                .getProxy()
                .getGroups()
                .stream()
                .filter(group -> group.getName().equals("group1"))
                .forEach(group -> group.getEndpoints().forEach(endpoint -> endpoint.setTarget(group1WireMock.baseUrl())));

            apiDefinition
                .getFlows()
                .forEach(flow ->
                    flow
                        .getPre()
                        .stream()
                        .filter(step -> step.getPolicy().equals("dynamic-routing"))
                        .forEach(step ->
                            step.setConfiguration(step.getConfiguration().replace("https://example.com", absoluteUrlWireMock.baseUrl()))
                        )
                );
        }
    }
}
