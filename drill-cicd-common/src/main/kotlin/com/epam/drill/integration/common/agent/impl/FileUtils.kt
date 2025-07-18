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
package com.epam.drill.integration.common.agent.impl

import com.epam.drill.integration.common.agent.Directory
import java.io.File
import java.util.zip.ZipFile
import kotlin.sequences.forEach

fun findFile(directory: Directory, fileExtension: String): File? {
    val files = directory.listFiles() ?: return null

    for (file in files) {
        if (file.isDirectory) {
            val result = findFile(file, fileExtension)
            if (result != null) {
                return result
            }
        } else if (file.extension == fileExtension) {
            return file
        }
    }

    return null
}

fun unzip(zipFile: File, destinationDir: Directory): Directory {
    if (!zipFile.exists()) {
        throw IllegalStateException("File $zipFile doesn't exist")
    }
    val unzippedDir = Directory(destinationDir, zipFile.nameWithoutExtension)
    if (!unzippedDir.exists()) {
        unzippedDir.mkdirs()
    }
    ZipFile(zipFile).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            if (entry.isDirectory) {
                File(unzippedDir, entry.name).mkdirs()
            } else {
                zip.getInputStream(entry).use { input ->
                    File(unzippedDir, entry.name).outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }
    return unzippedDir
}