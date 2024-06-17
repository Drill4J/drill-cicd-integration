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
package com.epam.drill.integration.common

import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

abstract class GitTestBase {
    protected lateinit var workingDir: File

    @BeforeTest
    fun setUpWorkingDir() {
        workingDir = Files.createTempDirectory("git-").toFile()
    }

    @AfterTest
    fun tearDownWorkingDir() {
        workingDir.deleteRecursively()
    }

    fun exec(command: String): String {
        val process = ProcessBuilder(command.split(" "))
            .directory(workingDir)
            .start()
        if (process.waitFor() != 0)
            throw IllegalStateException("Failed to execute $command: ${process.errorStream.bufferedReader().readText()}")
        return process.inputStream.bufferedReader().readText().trim()
    }

}