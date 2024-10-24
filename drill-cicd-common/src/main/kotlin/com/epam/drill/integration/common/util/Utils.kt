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
package com.epam.drill.integration.common.util

fun <T> T?.required(name: String): T =
    this ?: throw IllegalArgumentException("Property '$name' is not set or null")

fun <T> T?.fromEnv(envVar: String): String? =
    this?.toString() ?: System.getenv(envVar)

/**
 * Adds `--add-opens` options for Java 17 and higher to avoid java.lang.reflect.InaccessibleObjectException
 */
fun getJavaAddOpensOptions(): List<String> {
    val javaVersion = getCurrentJavaVersion()
    return if (javaVersion != null && javaVersion >= 17) {
        listOf(
            "--add-opens=java.base/java.util=ALL-UNNAMED",
            "--add-opens=java.base/java.lang=ALL-UNNAMED"
        )
    } else {
        emptyList()
    }
}

private fun getCurrentJavaVersion() = System.getProperty("java.version").split(".")[0].toIntOrNull()