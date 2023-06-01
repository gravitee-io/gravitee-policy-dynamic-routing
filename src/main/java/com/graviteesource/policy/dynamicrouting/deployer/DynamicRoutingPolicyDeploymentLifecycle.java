package com.graviteesource.policy.dynamicrouting.deployer;

import io.gravitee.node.api.deployer.AbstractPluginDeploymentLifecycle;

/**
 * @author Kamiel Ahmadpour (kamiel.ahmadpour at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DynamicRoutingPolicyDeploymentLifecycle extends AbstractPluginDeploymentLifecycle {

    private static final String DYNAMIC_ROUTING_POLICY = "apim-policy-dynamic-routing";

    @Override
    protected String getFeatureName() {
        return DYNAMIC_ROUTING_POLICY;
    }
}
