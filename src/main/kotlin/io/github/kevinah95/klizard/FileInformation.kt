/*
 *
 * Copyright 2012-2023 Kevin Hernández
 * Copyright 2012-2023 Terry Yin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.kevinah95.klizard

class FileInformation(
    var filename: String,
    var nloc: Int,
    var functionList: MutableList<FunctionInfo> = mutableListOf()
) {

    var tokenCount: Int = 0

    val averageNloc: Int
        get() {
            val summary = functionList.sumOf { it.nloc }
            return if (functionList.isNotEmpty()) {
                summary / functionList.size
            } else {
                0
            }
        }

    val averageTokenCount: Int
        get() {
            val summary = functionList.sumOf { it.tokenCount }
            return if (functionList.isNotEmpty()) {
                summary / functionList.size
            } else {
                0
            }
        }

    val averageCyclomaticComplexity: Int
        get() {
            val summary = functionList.sumOf { it.cyclomaticComplexity }
            return if (functionList.isNotEmpty()) {
                summary / functionList.size
            } else {
                0
            }
        }

    val CCN: Int
        get() = functionList.sumOf { it.cyclomaticComplexity }

    val ND: Int
        get() = TODO("max_nesting_depth property this is an extension")
}