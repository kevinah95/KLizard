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

class CSharpReader : CLikeReader() {
    override var ext: MutableList<String> = mutableListOf("cs")

    override val languageNames: List<String> = listOf("csharp")

    override var _conditions: Set<String> = setOf("if", "for", "while", "&&", "||", "?", "catch", "case", "??")

    override var conditions: Set<String> = _conditions.toSet()

    init {
        conditions = _conditions.toSet() // make a copy
    }


    override fun generateTokens(
        sourceCode: String,
        addition: String,
        tokenClass: ((match: MatchResult) -> String)?
    ): Sequence<String> {
        return super.generateTokens(sourceCode, """|(?:\?\?)""", tokenClass)
    }

}