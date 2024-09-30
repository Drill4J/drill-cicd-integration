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
package com.epam.drill.integration.common.agent

import java.util.*

private var CURRENT_OS_NAME: String = System.getProperty("os.name").lowercase(Locale.ENGLISH)
private var CURRENT_OS_ARCH: String = System.getProperty("os.arch").lowercase(Locale.ENGLISH)

enum class OS(val family: String, val arch: String, val preset: String, val libExt: String) {
    MAC_X64(family = "mac", arch = "x86_64", preset = "macosX64", libExt = "dylib"),
    MAC_ARM64(family = "mac", arch = "aarch64", preset = "macosArm64", libExt = "dylib"),
    WINDOWS(family = "windows", arch = "amd64", preset = "mingwX64", libExt = "dll"),
    LINUX(family = "linux", arch = "amd64", preset = "linuxX64", libExt = "so"),
}

private fun isFamily(os: OS): Boolean {
    return CURRENT_OS_NAME.contains(os.family)
}

private fun isArch(os: OS): Boolean {
    return CURRENT_OS_ARCH.contains(os.arch)
}

val currentOsPreset: String
    get() = OS.values()
        .firstOrNull() { isFamily(it) && isArch(it) }
        ?.preset
        ?: throw IllegalStateException("No preset for OS: $CURRENT_OS_NAME and arch: $CURRENT_OS_ARCH")

val currentOsLibExt: String
    get() = OS.values()
        .firstOrNull() { isFamily(it) && isArch(it) }
        ?.libExt
        ?: throw IllegalStateException("No library extension for OS: $CURRENT_OS_NAME and arch: $CURRENT_OS_ARCH")