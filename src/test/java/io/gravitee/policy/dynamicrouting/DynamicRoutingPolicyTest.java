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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.gravitee.el.TemplateContext;
import io.gravitee.el.TemplateEngine;
import io.gravitee.gateway.api.ExecutionContext;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.PolicyResult;
import io.gravitee.policy.dynamicrouting.configuration.DynamicRoutingPolicyConfiguration;
import io.gravitee.policy.dynamicrouting.configuration.Rule;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author Nicolas GERAUD (nicolas.geraud at graviteesource.com)
 * @author GraviteeSource Team
 */
@ExtendWith(MockitoExtension.class)
public class DynamicRoutingPolicyTest {

    private DynamicRoutingPolicy dynamicRoutingPolicy;

    @Mock
    private DynamicRoutingPolicyConfiguration dynamicRoutingPolicyConfiguration;

    @Mock
    protected Request request;

    @Mock
    protected Response response;

    @Mock
    protected PolicyChain policyChain;

    @Mock
    protected ExecutionContext executionContext;

    @BeforeEach
    public void init() {
        dynamicRoutingPolicy = new DynamicRoutingPolicy(dynamicRoutingPolicyConfiguration);
    }

    @Test
    public void test_shouldThrowFailure_noRule() {
        // Prepare inbound request
        when(request.pathInfo()).thenReturn("/products/ecom/");

        // Execute policy
        dynamicRoutingPolicy.onRequest(request, response, executionContext, policyChain);

        // Check results
        verify(policyChain).doNext(request, response);
    }

    @Test
    public void test_shouldDynamicRouting_noMatchingRule() {
        // Prepare policy configuration
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("/mag/", "http://host1/product"));

        when(dynamicRoutingPolicyConfiguration.getRules()).thenReturn(rules);

        // Prepare inbound request
        when(request.pathInfo()).thenReturn("/products/v1/ecom/");

        // Prepare context
        when(executionContext.getTemplateEngine()).thenReturn(TemplateEngine.templateEngine());

        // Execute policy
        dynamicRoutingPolicy.onRequest(request, response, executionContext, policyChain);

        // Check results
        verify(policyChain).failWith(any(PolicyResult.class));
    }

    @Test
    public void test_shouldDynamicRouting_singleMatchingRule() {
        // Prepare policy configuration
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("/v1/ecom/", "http://host1/product"));

        when(dynamicRoutingPolicyConfiguration.getRules()).thenReturn(rules);

        // Prepare inbound request
        when(request.pathInfo()).thenReturn("/v1/ecom/");

        // Prepare context
        when(executionContext.getTemplateEngine()).thenReturn(TemplateEngine.templateEngine());

        // Execute policy
        dynamicRoutingPolicy.onRequest(request, response, executionContext, policyChain);

        // Check results
        verify(policyChain).doNext(request, response);
        verify(executionContext).setAttribute(ExecutionContext.ATTR_REQUEST_ENDPOINT, rules.iterator().next().getUrl());
    }

    @Test
    public void test_shouldDynamicRouting_singleMatchingRule_notEncodedUrl() {
        // Prepare policy configuration
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("/[0-9,;]+", "http://host1/product"));

        when(dynamicRoutingPolicyConfiguration.getRules()).thenReturn(rules);

        // Prepare inbound request
        when(request.pathInfo()).thenReturn("/2124;2125");

        // Prepare context
        when(executionContext.getTemplateEngine()).thenReturn(TemplateEngine.templateEngine());

        // Execute policy
        dynamicRoutingPolicy.onRequest(request, response, executionContext, policyChain);

        // Check results
        verify(policyChain).doNext(request, response);
        verify(executionContext).setAttribute(ExecutionContext.ATTR_REQUEST_ENDPOINT, rules.iterator().next().getUrl());
    }

    @Test
    public void test_shouldDynamicRouting_singleMatchingRule_encodedUrl() {
        // Prepare policy configuration
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("/[0-9,;]+", "http://host1/product"));

        when(dynamicRoutingPolicyConfiguration.getRules()).thenReturn(rules);

        // Prepare inbound request
        when(request.pathInfo()).thenReturn("/2124%3B2125");

        // Prepare context
        when(executionContext.getTemplateEngine()).thenReturn(TemplateEngine.templateEngine());

        // Execute policy
        dynamicRoutingPolicy.onRequest(request, response, executionContext, policyChain);

        // Check results
        verify(policyChain).doNext(request, response);
        verify(executionContext).setAttribute(ExecutionContext.ATTR_REQUEST_ENDPOINT, rules.iterator().next().getUrl());
    }

    @Test
    public void test_shouldDynamicRouting_multipleMatchingRule() {
        // Prepare policy configuration
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("/v1/ecom/", "http://host1/product"));
        rules.add(new Rule("/v1/ecom/subpath", "http://host2/product"));

        when(dynamicRoutingPolicyConfiguration.getRules()).thenReturn(rules);

        // Prepare inbound request
        when(request.pathInfo()).thenReturn("/v1/ecom/");

        // Prepare context
        when(executionContext.getTemplateEngine()).thenReturn(TemplateEngine.templateEngine());

        // Execute policy
        dynamicRoutingPolicy.onRequest(request, response, executionContext, policyChain);

        // Check results
        verify(policyChain).doNext(request, response);
        verify(executionContext).setAttribute(ExecutionContext.ATTR_REQUEST_ENDPOINT, rules.get(0).getUrl());
    }

    @Test
    public void test_shouldDynamicRouting_multipleMatchingRule_regex() {
        // Prepare policy configuration
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("/v1/ecome.*", "http://host1/product"));
        rules.add(new Rule("/v1/ecom/(.*)", "http://host2/product"));

        when(dynamicRoutingPolicyConfiguration.getRules()).thenReturn(rules);

        // Prepare inbound request
        when(request.pathInfo()).thenReturn("/v1/ecom/");

        // Prepare context
        when(executionContext.getTemplateEngine()).thenReturn(TemplateEngine.templateEngine());

        // Execute policy
        dynamicRoutingPolicy.onRequest(request, response, executionContext, policyChain);

        // Check results
        verify(policyChain).doNext(request, response);
        verify(executionContext).setAttribute(ExecutionContext.ATTR_REQUEST_ENDPOINT, rules.get(1).getUrl());
    }

    @Test
    public void test_shouldDynamicRouting_multipleMatchingRule_transformEndpoint() {
        // Prepare policy configuration
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("/v1/ecome.*", "http://host1/product"));
        rules.add(new Rule("/v1/ecom/(.*)", "http://host2/product/{#group[0]}"));

        when(dynamicRoutingPolicyConfiguration.getRules()).thenReturn(rules);

        // Prepare inbound request
        when(request.pathInfo()).thenReturn("/v1/ecom/search");

        // Prepare context
        when(executionContext.getTemplateEngine()).thenReturn(TemplateEngine.templateEngine());

        // Execute policy
        dynamicRoutingPolicy.onRequest(request, response, executionContext, policyChain);

        // Check results
        verify(policyChain).doNext(request, response);
        verify(executionContext).setAttribute(ExecutionContext.ATTR_REQUEST_ENDPOINT, "http://host2/product/search");
    }

    @Test
    public void test_shouldDynamicRouting_multipleMatchingRule_transformEndpointWithGroupName() {
        // Prepare policy configuration
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("/api/(?<version>v[0-9]+)/ecome.*", "http://host1/products/api/{#groupName['version']}/{#group[0]}"));

        when(dynamicRoutingPolicyConfiguration.getRules()).thenReturn(rules);

        // Prepare inbound request
        when(request.pathInfo()).thenReturn("/api/v12/ecome");

        // Prepare context
        when(executionContext.getTemplateEngine()).thenReturn(TemplateEngine.templateEngine());

        // Execute policy
        dynamicRoutingPolicy.onRequest(request, response, executionContext, policyChain);

        // Check results
        verify(policyChain).doNext(request, response);
        verify(executionContext).setAttribute(ExecutionContext.ATTR_REQUEST_ENDPOINT, "http://host1/products/api/v12/v12");
    }

    @Test
    public void test_shouldDynamicRouting_singleMatchingRule_EL() {
        // Prepare policy configuration
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("/{#request.paths[1]}/(.*)", "http://host1/api/products"));

        when(dynamicRoutingPolicyConfiguration.getRules()).thenReturn(rules);

        // Prepare inbound request
        when(request.pathInfo()).thenReturn("/api/products/2124%3B2125");

        // Prepare context
        final TemplateEngine templateEngine = mock(TemplateEngine.class);
        when(executionContext.getTemplateEngine()).thenReturn(templateEngine);
        when(templateEngine.getValue("/{#request.paths[1]}/(.*)", String.class)).thenReturn("/(.*)");
        final TemplateContext templateContext = mock(TemplateContext.class);
        when(templateEngine.getTemplateContext()).thenReturn(templateContext);

        // Execute policy
        dynamicRoutingPolicy.onRequest(request, response, executionContext, policyChain);

        // Check results
        verify(policyChain).doNext(request, response);
    }

    @Test
    public void test_invalidPattern() {
        // Prepare policy configuration
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("/ecom/($12[a-/search.*)", "http://host1/api/ecom"));

        when(dynamicRoutingPolicyConfiguration.getRules()).thenReturn(rules);

        // Prepare inbound request
        when(request.pathInfo()).thenReturn("/ecom/2124%3B2125");

        // Prepare context
        final TemplateEngine templateEngine = mock(TemplateEngine.class);
        when(executionContext.getTemplateEngine()).thenReturn(templateEngine);
        when(templateEngine.getValue("/ecom/($12[a-/search.*)", String.class)).thenReturn("/ecom/($12[a-/search.*)");

        // Execute policy
        dynamicRoutingPolicy.onRequest(request, response, executionContext, policyChain);

        // Check results
        verify(policyChain).failWith(any(PolicyResult.class));
    }

    @Test
    public void test_shouldDynamicRouting_singleMatchingRule_encodeddUrlAndNotEncodedPath() {
        // Prepare policy configuration
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("/foo bar", "http://host1/product"));

        when(dynamicRoutingPolicyConfiguration.getRules()).thenReturn(rules);

        // Prepare inbound request
        when(request.pathInfo()).thenReturn("/foo%20bar");

        // Prepare context
        when(executionContext.getTemplateEngine()).thenReturn(TemplateEngine.templateEngine());

        // Execute policy
        dynamicRoutingPolicy.onRequest(request, response, executionContext, policyChain);

        // Check results
        verify(policyChain).doNext(request, response);
        verify(executionContext).setAttribute(ExecutionContext.ATTR_REQUEST_ENDPOINT, rules.iterator().next().getUrl());
    }

    @Test
    public void test_shouldNotDynamicRouting_singleMatchingRule_encodedUrlAndEncodedPath() {
        // Prepare policy configuration
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("/foo%20bar", "http://host1/product"));

        when(dynamicRoutingPolicyConfiguration.getRules()).thenReturn(rules);

        // Prepare inbound request
        when(request.pathInfo()).thenReturn("/products/foo%20bar");

        // Prepare context
        when(executionContext.getTemplateEngine()).thenReturn(TemplateEngine.templateEngine());

        // Execute policy
        dynamicRoutingPolicy.onRequest(request, response, executionContext, policyChain);

        // Check results
        verify(policyChain).failWith(any(PolicyResult.class));
    }

    @Test
    public void test_shouldDynamicRouting_singleMatchingRule_WithGroup() {
        // Prepare policy configuration
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("/foo/(.*)", "http://host1/{#group[0]}"));

        when(dynamicRoutingPolicyConfiguration.getRules()).thenReturn(rules);

        // Prepare inbound request
        when(request.pathInfo()).thenReturn("/foo/bar");

        // Prepare context
        when(executionContext.getTemplateEngine()).thenReturn(TemplateEngine.templateEngine());

        // Execute policy
        dynamicRoutingPolicy.onRequest(request, response, executionContext, policyChain);

        // Check results
        verify(policyChain).doNext(request, response);
        verify(executionContext).setAttribute(ExecutionContext.ATTR_REQUEST_ENDPOINT, "http://host1/bar");
    }

    @Test
    public void test_shouldDynamicRouting_singleMatchingRule_WithEncodedGroup() {
        // Prepare policy configuration
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("/foo/(.*)", "http://host1/{#group[0]}"));

        when(dynamicRoutingPolicyConfiguration.getRules()).thenReturn(rules);

        // Prepare inbound request
        when(request.pathInfo()).thenReturn("/foo/%3Dbar");

        // Prepare context
        when(executionContext.getTemplateEngine()).thenReturn(TemplateEngine.templateEngine());

        // Execute policy
        dynamicRoutingPolicy.onRequest(request, response, executionContext, policyChain);

        // Check results
        verify(policyChain).doNext(request, response);
        verify(executionContext).setAttribute(ExecutionContext.ATTR_REQUEST_ENDPOINT, "http://host1/%3Dbar");
    }

    @Test
    public void test_shouldDynamicRouting_singleMatchingRule_WithEncodedGroupAndEncodedPattern() {
        // Prepare policy configuration
        List<Rule> rules = new ArrayList<>();
        rules.add(new Rule("(/%2377777)", "http://host1{#group[0]}"));

        when(dynamicRoutingPolicyConfiguration.getRules()).thenReturn(rules);

        // Prepare inbound request
        when(request.pathInfo()).thenReturn("/%2377777");

        // Prepare context
        when(executionContext.getTemplateEngine()).thenReturn(TemplateEngine.templateEngine());

        // Execute policy
        dynamicRoutingPolicy.onRequest(request, response, executionContext, policyChain);

        // Check results
        verify(policyChain).doNext(request, response);
        verify(executionContext).setAttribute(ExecutionContext.ATTR_REQUEST_ENDPOINT, "http://host1/%2377777");
    }
}
