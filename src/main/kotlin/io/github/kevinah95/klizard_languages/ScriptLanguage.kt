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

package io.github.kevinah95.klizard_languages

class ScriptLanguageMixIn {
    fun getCommentFromToken(token: String): String? {
        if (token.startsWith("#")){
            return token.drop(1)
        }
        return null
    }

    fun generateCommonTokens(sourceCode: String, addition: String = "", matchHolder: ((match: MatchResult) -> String)? = null): Sequence<String>{
        val _untilEnd = "(?:\\\n|[^\n])*"
        return CodeReader.generateTokens(
            sourceCode,
            "|#"+_untilEnd + addition,
            matchHolder
        )
    }

    companion object {
        fun generateCommonTokens(sourceCode: String, addition: String = "", matchHolder: ((match: MatchResult) -> String)? = null): Sequence<String>{
            val _untilEnd = "(?:\\\n|[^\n])*"
            return CodeReader.generateTokens(
                sourceCode,
                "|#"+_untilEnd + addition,
                matchHolder
            )
        }
    }
}