package com.graviteesource.policy.dynamicrouting.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class DynamicRoutingPolicyConfigurationTest {

    @Test
    public void testConfiguration_singleRule() throws IOException {
        DynamicRoutingPolicyConfiguration configuration = load(
            "/com/graviteesource/policy/dynamicrouting/configuration/configuration1.json"
        );

        Assertions.assertThat(configuration.getRules()).hasSize(1);
    }

    private DynamicRoutingPolicyConfiguration load(String resource) throws IOException {
        URL jsonFile = this.getClass().getResource(resource);
        return objectMapper().readValue(jsonFile, DynamicRoutingPolicyConfiguration.class);
    }

    private ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
