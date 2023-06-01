package com.graviteesource.policy.dynamicrouting.configuration;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public final class Rule {

    private String pattern;

    private String url;

    public Rule() {}

    public Rule(String pattern, String url) {
        this.pattern = pattern;
        this.url = url;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
