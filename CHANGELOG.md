# [2.1.0](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/compare/2.0.0...2.1.0) (2026-06-24)


### Features

* enable policy for mcp proxy on request phase ([924ba85](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/commit/924ba85f68d3d9403b6cbcde486723812eb8f74b))

# [2.0.0](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/compare/1.13.0...2.0.0) (2026-06-24)


### Bug Fixes

* replace removed ExecutionMode.JUPITER with V4_EMULATION_ENGINE ([7ecd373](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/commit/7ecd3732556c202ad04cedad54b02fde24d3fde8))


### chore

* **deps:** bump up gravitee:gravitee-parent to v23.5.0 ([63e9b28](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/commit/63e9b284fcb1caa9e7fde97b2cd41e7eebbba278))


### BREAKING CHANGES

* **deps:** gravitee-parent v23 compiles with JDK 21, so the minimum
supported APIM version is now 4.8.x. Update the compatibility matrix
accordingly (plugin 2.x targets APIM 4.8 and later).

# [1.13.0](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/compare/1.12.1...1.13.0) (2023-12-13)


### Features

* enable policy on REQUEST phase for message APIs ([0924bd3](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/commit/0924bd330c0c9e9e1980958e97c8c29e938a2c9c))

## [1.12.1](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/compare/1.12.0...1.12.1) (2023-07-20)


### Bug Fixes

* update policy description ([b1384ab](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/commit/b1384ab81e37dade627f1d46e8a08c5d2542678c))

# [1.12.0](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/compare/1.11.3...1.12.0) (2023-07-05)


### Features

* define the execution phase of the policy in the plugin.properties ([e01c3c8](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/commit/e01c3c8372761ff41cba7e037725b2549c1cb038))

## [1.11.3](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/compare/1.11.2...1.11.3) (2023-06-29)


### Bug Fixes

* match correctly a group when using an encoded rule pattern ([91e1b67](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/commit/91e1b673822163ff3ffc48e0e4bef686c9021729))

## [1.11.2](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/compare/1.11.1...1.11.2) (2022-11-10)


### Bug Fixes

* **dynamic-routing-policy:** update hint for Match expression ([817e1d1](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/commit/817e1d1ddf914b30d9a47f66331ae0d7a178bb10))

## [1.11.1](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/compare/1.11.0...1.11.1) (2022-11-10)


### Bug Fixes

* **dynamic-routing-policy:** update hint for Match expression ([817e1d1](https://github.com/gravitee-io/gravitee-policy-dynamic-routing/commit/817e1d1ddf914b30d9a47f66331ae0d7a178bb10))
