package com.graviteesource.policy.dynamicrouting.configuration;

import io.gravitee.policy.api.PolicyConfiguration;
import java.util.List;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DynamicRoutingPolicyConfiguration implements PolicyConfiguration {

    private List<Rule> rules;

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
}
