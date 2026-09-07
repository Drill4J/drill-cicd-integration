/**
 * Copyright 2020 - 2022 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.drill.integration.gradle

import org.apache.maven.plugins.annotations.Parameter

abstract class AbstractAppDrillMojo: AbstractDrillMojo() {
    @Parameter(property = "appId", required = false)
    var appId: String? = null

    @Parameter(property = "packagePrefixes", required = false)
    var packagePrefixes: String? = null

    @Parameter(property = "buildVersion", required = false)
    var buildVersion: String? = null

    @Parameter(property = "envId", required = false)
    var envId: String? = null

    @Parameter(property = "testTaskId", required = false)
    var testTaskId: String? = null

    @Parameter(property = "baseline", required = true)
    var baseline: BaselineConfiguration? = null

    @Parameter(property = "recommendedTests", required = false)
    var recommendedTests: RecommendedTestsConfiguration? = null
}