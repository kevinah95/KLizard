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

import io.github.kevinah95.FileInfoBuilder

// TODO: context is FileInfoBuilder
abstract class CodeReader {
    open lateinit var context: FileInfoBuilder
    open var ext: MutableList<String> = mutableListOf()
    var extraSubclasses: Set<String> = setOf()
    var _conditions: Set<String> = setOf("if", "for", "while", "&&", "||", "?", "catch", "case")
    var parallelStates: List<CodeStateMachine> = listOf()
    var conditions: Set<String>

    init {
        conditions = _conditions.toSet() // make a copy
    }

    fun matchFilename(filename: String): Boolean {
        fun compileFileExtensionRe(exts: List<String>): Regex {
            return Regex(""".*\.(${exts.joinToString("|")})$""", RegexOption.IGNORE_CASE)
        }

        return compileFileExtensionRe(this.ext).matches(filename)
    }

    fun generateTokens(
        sourceCode: String,
        addition: String = "",
        _tokenClass: ((match: MatchResult) -> String)? = null
    ): Sequence<String> {
        fun createToken(match: MatchResult): String {
            return match.groupValues[0]
        }

        var tokenClass = _tokenClass
        if (_tokenClass == null) {
            tokenClass = ::createToken
        }

        fun _generateTokens(source: String, add: String) = sequence {
            val _untilEnd: String = """(?:\\\n|[^\n])*"""
            val combined_symbols: Array<String> = arrayOf(
                "<<=", ">>=", "||", "&&", "===", "!==",
                "==", "!=", "<=", ">=", "->", "=>",
                "++", "--", "+=", "-=",
                "+", "-", "*", "/",
                "*=", "/=", "^=", "&=", "|=", "..."
            )

            // TODO: Add RegexOption.DOT_MATCHES_ALL when available for common
            val tokenPattern = Regex(
                """(?:""" +
                        """\/\*.*?\*\/""" +
                        add +
                        """|(?:\d+\')+\d+""" +
                        """|\w+""" +
                        """|\"(?:\\.|[^\"\\])*\"""" +
                        """|\'(?:\\.|[^\'\\])*?\'""" +
                        """|\/\/""" + _untilEnd +
                        """|\#""" +
                        """|:=|::|\*\*""" +
                        """|\<\s*\?(?:\s*extends\s+\w+)?\s*\>""" +
                        """|""" + combined_symbols.map { Regex.escape(it) }.joinToString("|") +
                        """|\\\n""" +
                        """|\n""" +
                        """|[^\S\n]+""" +
                        """|.)""", setOf(RegexOption.MULTILINE)
            )

            var macro: String = ""

            for (match in tokenPattern.findAll(source)) {
                val token: String = tokenClass?.let { it(match) }.orEmpty()
                if (macro != "") {
                    if (token.contains("""\\\n""") || token.contains("""\n""")) {
                        macro += token
                    } else {
                        yield(macro)
                        yield(token)
                        macro = ""
                    }
                } else if (token == "#") {
                    macro = token
                } else {
                    yield(token)
                }
            }

            if (macro != "") {
                yield(macro)
            }


        }

        return _generateTokens(sourceCode, addition)
    }

    open operator fun invoke(_context: FileInfoBuilder) {
        context = _context
    }

    operator fun invoke(tokens: Sequence<String>, reader: CodeReader) = sequence {
        context = reader.context
        for (token in tokens) {
            for (state in parallelStates) {
                //TODO: state(token)
            }
            yield(token)
        }

        for (state in parallelStates) {
            state.statemachineBeforeReturn()
        }
        eof()
    }

    fun eof() {}

    open fun preprocess(tokens: Sequence<String>) = sequence<String> {

    }
}



















