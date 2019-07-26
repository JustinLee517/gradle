/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.integtests.resolve.forsubgraph

import org.gradle.integtests.fixtures.GradleMetadataResolveRunner
import org.gradle.integtests.fixtures.RequiredFeature
import org.gradle.integtests.fixtures.RequiredFeatures
import org.gradle.integtests.resolve.AbstractModuleDependencyResolveTest

class ForSubgraphVersionConstraintsIntegrationTest extends AbstractModuleDependencyResolveTest {

    def setup() {
        resolve.withStrictReasonsCheck()
    }

    @RequiredFeatures([
        @RequiredFeature(feature = GradleMetadataResolveRunner.GRADLE_METADATA, value="true")]
    )
    void "can downgrade version through platform"() {
        given:
        repository {
            'org:platform:1.0'() {
                constraint(group: 'org', artifact: 'bar', version: '1.0')
                constraint(group: 'org', artifact: 'foo', version: '1.0', forSubgraph: true)
            }
            'org:foo:1.0'()
            'org:foo:2.0'()
            'org:bar:1.0' {
                dependsOn 'org:foo:2.0'
            }
        }

        buildFile << """
            dependencies {
                conf('org:platform:1.0') {
                    inheritSubgraphConstraints()
                }
                conf('org:bar')
            }           
        """

        when:
        repositoryInteractions {
            'org:platform:1.0' {
                expectGetMetadata()
                expectGetArtifact()
            }
            'org:bar:1.0' {
                expectGetMetadata()
                expectGetArtifact()
            }
            'org:foo:1.0' {
                expectGetMetadata()
                expectGetArtifact()
            }
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(':', ':test:') {
                module('org:platform:1.0') {
                    constraint('org:bar:1.0').byConstraint()
                    constraint('org:foo:1.0').byConstraint()
                }
                edge('org:bar', 'org:bar:1.0') {
                    edge('org:foo:2.0', 'org:foo:1.0').byParent()
                }.byRequest()
            }
        }
    }

    @RequiredFeatures([
        @RequiredFeature(feature = GradleMetadataResolveRunner.GRADLE_METADATA, value = "true")]
    )
    void "can deal with platform upgrades"() {
        given:
        repository {
            'org:platform:1.0'() {
                constraint(group: 'org', artifact: 'bar', version: '1.0')
                constraint(group: 'org', artifact: 'foo', version: '1.0', forSubgraph: true)
            }
            'org:platform:2.0'() {
                constraint(group: 'org', artifact: 'bar', version: '1.0')
                constraint(group: 'org', artifact: 'foo', version: '1.0')
            }
            'org:foo:1.0'()
            'org:foo:2.0'()
            'org:bar:1.0' {
                dependsOn 'org:foo:2.0'
                dependsOn 'org:platform:2.0'
            }
        }

        buildFile << """
            dependencies {
                conf('org:platform:1.0') {
                    inheritSubgraphConstraints()
                }
                conf('org:bar')
            }           
        """

        when:
        repositoryInteractions {
            'org:platform:1.0' {
                expectGetMetadata()
            }
            'org:platform:2.0' {
                expectGetMetadata()
                expectGetArtifact()
            }
            'org:bar:1.0' {
                expectGetMetadata()
                expectGetArtifact()
            }
            'org:foo:1.0' {
                expectGetMetadata()
                expectGetArtifact()
            }
        }
        run ':checkDeps'
        run ':dependencies'

        then:
        resolve.expectGraph {
            root(':', ':test:') {
                edge('org:platform:1.0', 'org:platform:2.0') {
                    constraint('org:bar:1.0').byConstraint()
                    constraint('org:foo:1.0').byConstraint()
                }.byConflictResolution("between versions 2.0 and 1.0")
                edge('org:bar', 'org:bar:1.0') {
                    // FIXME the following is wrong - more selection need to be invalidated when 'platform' gets upgraded
                    // This is a general issue with 'forSubgraph()'.
                    // If a module changes the 'forSubgraph' (edge removed, added or 'forSubgraph' flag changed) state on an outgoing edge,
                    // all the corresponding module always needs to be re-selected.
                    edge('org:foo:2.0', 'org:foo:1.0').byParent()
                    module('org:platform:2.0').byRequest()
                }.byRequest()
            }
        }
    }
}
