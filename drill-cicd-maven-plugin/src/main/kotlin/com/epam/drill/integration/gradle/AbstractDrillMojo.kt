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

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

abstract class AbstractDrillMojo : AbstractMojo() {
    @Parameter(defaultValue = "\${project}", required = true, readonly = true)
    protected lateinit var project: MavenProject

    @Parameter(property = "apiUrl", required = true)
    var apiUrl: String? = null

    @Parameter(property = "apiKey")
    var apiKey: String? = null

    @Parameter(property = "groupId", required = true)
    var groupId: String? = null

    @Parameter(property = "useTestRecommendations")
    var useTestRecommendations: RecommendedTestsConfiguration? = null
}