/*
 *
 * Copyright 2023 Kevin Hernández
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

package io.github.kevinah95.klizard.klizard_languages

import io.github.kevinah95.klizard.FileInfoBuilder

class GoReader : CodeReader() {

    override var ext: MutableList<String> = mutableListOf("go")

    // TODO: Implement this
    val languageNames: List<String> = listOf("go")

    override operator fun invoke(_context: FileInfoBuilder) {
        context = _context
        parallelStates = listOf(
            GoStates(context)
        )
    }
}

class GoStates(context: FileInfoBuilder) : GoLikeStates(context) {
    // implementation here
}