# Changelog

### [1.30.10](https://www.github.com/googleapis/google-api-java-client/compare/v1.30.9...v1.30.10) (2020-07-08)


### Bug Fixes

* cleanup checkstyle import rules ([#1503](https://www.github.com/googleapis/google-api-java-client/issues/1503)) ([ce22c0e](https://www.github.com/googleapis/google-api-java-client/commit/ce22c0ea7d02bd1807eae0076bfe55db1a3b754c))
* cleanup commented code of version control ([#1504](https://www.github.com/googleapis/google-api-java-client/issues/1504)) ([5b1968d](https://www.github.com/googleapis/google-api-java-client/commit/5b1968d7bb05affd211b3403a6acc18c3d0c23d9))


### Dependencies

* remove excess dependencies ([#1502](https://www.github.com/googleapis/google-api-java-client/issues/1502)) ([dbe6287](https://www.github.com/googleapis/google-api-java-client/commit/dbe6287dac5b7340827a03c0977ad8f350b58eae))
* update dependency com.google.oauth-client:google-oauth-client-bom to v1.31.0 ([#1544](https://www.github.com/googleapis/google-api-java-client/issues/1544)) ([ebab80d](https://www.github.com/googleapis/google-api-java-client/commit/ebab80d1fec30bde241a5cb7a3146c89aca5359c))
* update dependency com.google.protobuf:protobuf-java to v3.12.2 ([#1527](https://www.github.com/googleapis/google-api-java-client/issues/1527)) ([a4cdec7](https://www.github.com/googleapis/google-api-java-client/commit/a4cdec7afd3c5fb1b6cb4de05021e156dd9d0a14))
* update google-http-client to v1.36.0 ([#1551](https://www.github.com/googleapis/google-api-java-client/issues/1551)) ([01c635a](https://www.github.com/googleapis/google-api-java-client/commit/01c635a21fc3d7b549b6a19cbf3359cacf04ef47))
* update oauth client ([#1499](https://www.github.com/googleapis/google-api-java-client/issues/1499)) ([b9c222c](https://www.github.com/googleapis/google-api-java-client/commit/b9c222cfdc4578dabca48cfbe64068f4f70d3b18))
* update to Guava 29.0 ([#1516](https://www.github.com/googleapis/google-api-java-client/issues/1516)) ([fda223d](https://www.github.com/googleapis/google-api-java-client/commit/fda223d5da5076396bc896db3227facde17e89e5))


### Documentation

* removes reference to deprecated service (Google+) ([#1530](https://www.github.com/googleapis/google-api-java-client/issues/1530)) ([b257207](https://www.github.com/googleapis/google-api-java-client/commit/b2572075418dbbb86b9da8901184c72966464520))

### [1.30.9](https://www.github.com/googleapis/google-api-java-client/compare/v1.30.8...v1.30.9) (2020-02-18)


### Bug Fixes

* use embedded Proguard configuration instead of compile-time annotation ([#1491](https://www.github.com/googleapis/google-api-java-client/issues/1491)) ([8a59ed0](https://www.github.com/googleapis/google-api-java-client/commit/8a59ed071f19d9b78d284cdc75181ee102ca9c61)), closes [#1467](https://www.github.com/googleapis/google-api-java-client/issues/1467) [#1489](https://www.github.com/googleapis/google-api-java-client/issues/1489)

### [1.30.8](https://www.github.com/googleapis/google-api-java-client/compare/v1.30.7...v1.30.8) (2020-01-31)


### Bug Fixes

* future test breakage ([#1457](https://www.github.com/googleapis/google-api-java-client/issues/1457)) ([e7d5c83](https://www.github.com/googleapis/google-api-java-client/commit/e7d5c831a9d5c72206af6322142c338585698a78))


### Dependencies

* update dependency commons-codec:commons-codec to v1.14 ([#1463](https://www.github.com/googleapis/google-api-java-client/issues/1463)) ([97de688](https://www.github.com/googleapis/google-api-java-client/commit/97de688da9a0748e78f314f686a771852ed78459))
* update guava to 28.2-android ([#1473](https://www.github.com/googleapis/google-api-java-client/issues/1473)) ([69a8d6f](https://www.github.com/googleapis/google-api-java-client/commit/69a8d6fb4146e85e8100c9e5fc220835f8ce12d9))
* update http client ([#1482](https://www.github.com/googleapis/google-api-java-client/issues/1482)) ([232785a](https://www.github.com/googleapis/google-api-java-client/commit/232785a9aa8a4441129d74a08066bcb848153df3))

### [1.30.7](https://www.github.com/googleapis/google-api-java-client/compare/v1.30.6...v1.30.7) (2019-12-17)


### Bug Fixes

* update GoogleUtils#getVersion() to use stable logic ([#1452](https://www.github.com/googleapis/google-api-java-client/issues/1452)) ([744839f](https://www.github.com/googleapis/google-api-java-client/commit/744839f51a01301390297b4a5cb037547eefc4c0))


### Dependencies

* update dependency com.google.oauth-client:google-oauth-client-bom to v1.30.5 ([#1453](https://www.github.com/googleapis/google-api-java-client/issues/1453)) ([b32a7d9](https://www.github.com/googleapis/google-api-java-client/commit/b32a7d95801e1e2e5617818a4f11853eca272c72))

### [1.30.6](https://www.github.com/googleapis/google-api-java-client/compare/v1.30.5...v1.30.6) (2019-12-03)


### Bug Fixes

* grab version from package metadata ([#1419](https://www.github.com/googleapis/google-api-java-client/issues/1419)) ([a6c6dec](https://www.github.com/googleapis/google-api-java-client/commit/a6c6decbd4a162ff4030e2c3f74c72eb6ac9eddb))
* user-agent should use identifier/version ([#1425](https://www.github.com/googleapis/google-api-java-client/issues/1425)) ([bfb4d9c](https://www.github.com/googleapis/google-api-java-client/commit/bfb4d9cacdadd8065be07b1bf0c22ea7aeb94d97))


### Documentation

* fix link to media upload documentation ([#1442](https://www.github.com/googleapis/google-api-java-client/issues/1442)) ([21af62a](https://www.github.com/googleapis/google-api-java-client/commit/21af62a45eb167adcf4d6932b27f9e2b86fc06f3))


### Dependencies

* remove jackson-core-asl ([#1414](https://www.github.com/googleapis/google-api-java-client/issues/1414)) ([8e08249](https://www.github.com/googleapis/google-api-java-client/commit/8e082496d41ed271523b78df80e678a338f22a8a))
* update dependency com.google.protobuf:protobuf-java to v3.11.0 ([#1431](https://www.github.com/googleapis/google-api-java-client/issues/1431)) ([c4be24d](https://www.github.com/googleapis/google-api-java-client/commit/c4be24d2f371c22aa12d47085e88f21774efa6e5))
* update dependency com.google.protobuf:protobuf-java to v3.11.1 ([#1436](https://www.github.com/googleapis/google-api-java-client/issues/1436)) ([c1eaa85](https://www.github.com/googleapis/google-api-java-client/commit/c1eaa851d9bd4102a0cff21d972190923050fd5e))
* update guava to 28.1-android ([#1410](https://www.github.com/googleapis/google-api-java-client/issues/1410)) ([1d37f32](https://www.github.com/googleapis/google-api-java-client/commit/1d37f325d3c0cf808cd7c006ba9414e4dd65e5b6))

### [1.30.5](https://www.github.com/googleapis/google-api-java-client/compare/v1.30.4...v1.30.5) (2019-10-24)


### Bug Fixes

* add details to GoogleJsonResponseExceptions created with GoogleJsonResponseExceptionFactoryTesting ([#1395](https://www.github.com/googleapis/google-api-java-client/issues/1395)) ([1ffdba6](https://www.github.com/googleapis/google-api-java-client/commit/1ffdba6071d716d9843fada802c3cb4d2dcaedf7))
* specify Metadata-Flavor for metadata requests ([#1397](https://www.github.com/googleapis/google-api-java-client/issues/1397)) ([d3dcfe9](https://www.github.com/googleapis/google-api-java-client/commit/d3dcfe9c049f72207b30e75f073b4b8ccc14c46d))


### Dependencies

* update dependency com.google.oauth-client:google-oauth-client-bom to v1.30.4 ([#1401](https://www.github.com/googleapis/google-api-java-client/issues/1401)) ([502cf1d](https://www.github.com/googleapis/google-api-java-client/commit/502cf1d1f2b7b6231ace109f3162c069c5f2234c))


### Documentation

* migrate docs from wiki ([#1399](https://www.github.com/googleapis/google-api-java-client/issues/1399)) ([173adca](https://www.github.com/googleapis/google-api-java-client/commit/173adcaed0d96b3771486ee59d0b4a4cf87df895))

### [1.30.4](https://www.github.com/googleapis/google-api-java-client/compare/v1.30.3...v1.30.4) (2019-09-20)


### Dependencies

* update dependency com.google.oauth-client:google-oauth-client-bom to v1.30.3 ([#1386](https://www.github.com/googleapis/google-api-java-client/issues/1386)) ([3a9ba9a](https://www.github.com/googleapis/google-api-java-client/commit/3a9ba9a))
* update dependency com.google.protobuf:protobuf-java to v3.10.0 ([#1379](https://www.github.com/googleapis/google-api-java-client/issues/1379)) ([309f8d4](https://www.github.com/googleapis/google-api-java-client/commit/309f8d4))
* update google-http-client to v1.32.1 ([#1384](https://www.github.com/googleapis/google-api-java-client/issues/1384)) ([859570a](https://www.github.com/googleapis/google-api-java-client/commit/859570a))
