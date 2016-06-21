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

import io.gravitee.common.http.HttpStatusCode;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.PolicyResult;
import io.gravitee.policy.api.annotations.OnRequest;
import io.gravitee.policy.dynamicrouting.configuration.DynamicRoutingPolicyConfiguration;
import io.gravitee.policy.dynamicrouting.configuration.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.regex.Matcher;

/**
 * @author David BRASSELY (david at gravitee.io)
 * @author GraviteeSource Team
 */
public class DynamicRoutingPolicy {

    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicRoutingPolicy.class);

    /**
     * The associated configuration to this Policy
     */
    private DynamicRoutingPolicyConfiguration configuration;

    /**
     * Create a new policy instance based on its associated configuration
     *
     * @param configuration the associated configuration to the new policy instance
     */
    public DynamicRoutingPolicy(DynamicRoutingPolicyConfiguration configuration) {
        this.configuration = configuration;
    }

    @OnRequest
    public void onRequest(Request request, Response response, ExecutionContext executionContext, PolicyChain policyChain) {
        String path = (String) executionContext.getAttribute(ExecutionContext.ATTR_RESOLVED_PATH);
        LOGGER.debug("Dynamic routing for resolved path {} [complete path: {}]", path, request.path());

        // Look for a matching pattern from rules
        Optional<Rule> optRule = configuration.getRules().stream().filter(
                rule -> rule.getPattern().matcher(path).matches()).findFirst();

        if (optRule.isPresent()) {
            Rule rule = optRule.get();

            LOGGER.debug("Applying rule for path {}: [{} - {}]", path, rule.getPattern(), rule.getUrl());
            String endpoint = rule.getUrl();

            // Apply regex capture / replacement
            Matcher match = rule.getPattern().matcher(path);

            // Required to calculate capture groups
            match.matches();

            for(int idx = 0 ; idx < match.groupCount() ; idx++) {
                String name = "group_" + (idx);
                String value = match.group(idx+1);

                LOGGER.debug("Group count: {}", match.groupCount());
                LOGGER.debug("Add a new variable in template engine: {} - {}", name, value);
                executionContext.getTemplateEngine().getTemplateContext().setVariable(name, value);
            }

            // Given endpoint can be defined as the template using EL
            LOGGER.debug("Transform endpoint {} using template engine", endpoint);
            endpoint = executionContext.getTemplateEngine().convert(endpoint);

            // Set final endpoint
            executionContext.setAttribute(ExecutionContext.ATTR_REQUEST_ENDPOINT, endpoint);
            LOGGER.debug("Route request to {}", endpoint);

            // And continue request processing....
            policyChain.doNext(request, response);
        } else {
            LOGGER.warn("No defined rule is matching path {}", path);
            // No rule is matching request path
            policyChain.failWith(PolicyResult.failure(HttpStatusCode.BAD_REQUEST_400, "No defined rule is matching path " + path));
        }
    }
}
