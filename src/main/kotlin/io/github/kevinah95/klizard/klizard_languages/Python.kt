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

fun countSpaces(token: String): Int {
    return token.replace("\t", " ".repeat(8)).length
}

class PythonIndents(val context: FileInfoBuilder) {
    var indents: MutableList<Int> = mutableListOf(0)

    fun setNesting(spaces: Int, token: String = "") {
        while ((indents.last() > spaces) && (!token.startsWith(")"))) {
            indents.removeLast()
            context.popNesting()
        }
        if (indents.last() < spaces) {
            indents.add(spaces)
            context._nestingStack.addBareNesting()
        }
    }

    fun reset() {
        setNesting(0)
    }
}

class PythonReader() : CodeReader() {
    override var ext: MutableList<String> = mutableListOf("py")

    val languageNames: List<String> = listOf("python")

    override var _conditions: Set<String> = setOf("if", "for", "while", "and", "or", "elif", "except", "finally")

    override var conditions: Set<String> = _conditions.toSet()

    init {
        conditions = _conditions.toSet() // make a copy
    }

    override operator fun invoke(_context: FileInfoBuilder) {
        context = _context
        parallelStates = listOf(
            PythonStates(context, this)
        )
    }

    override fun getCommentFromToken(token: String): String? {
        if (token.startsWith("#")){
            return token.drop(1)
        }
        return null
    }

    override fun generateTokens(
        sourceCode: String,
        addition: String,
        _tokenClass: ((match: MatchResult) -> String)?
    ): Sequence<String> {
        return ScriptLanguageMixIn.generateCommonTokens(
            sourceCode,
            """|'''.*?'''""" + """|\"\"\".*?\"\"\"""",
            _tokenClass
        )
    }

    companion object {
        fun generateTokens(
            sourceCode: String,
            addition: String = "",
            _tokenClass: ((match: MatchResult) -> String)? = null
        ): Sequence<String> {
            return ScriptLanguageMixIn.generateCommonTokens(
                sourceCode,
                """|'''.*?'''""" + """|\"\"\".*?\"\"\"""",
                _tokenClass
            )
        }
    }

    override fun preprocess(tokens: Sequence<String>) = sequence<String> {
        val indents = PythonIndents(context)
        var currentLeadingSpaces = 0
        var readingLeadingSpace = true

        for (token in tokens) {
            if (token != "\n") {
                if (readingLeadingSpace) {
                    if (token.all { it.isWhitespace() }) {
                        currentLeadingSpaces += countSpaces(token)
                    } else {
                        if (!token.startsWith('#')) {
                            val currentFunction = context.currentFunction
                            if (currentFunction.name == "*global*" || currentFunction.longName.endsWith(")")){
                                indents.setNesting(currentLeadingSpaces, token)
                            }
                        }
                        readingLeadingSpace = false
                    }
                }
            } else {
                readingLeadingSpace = true
                currentLeadingSpaces = 0
            }

            if (!token.all { it.isWhitespace() } || token == "\n"){
                yield(token)
            }
        }
        indents.reset()
    }
}

class PythonStates(context: FileInfoBuilder, reader: CodeReader) : CodeStateMachine(context) {

    override fun _stateGlobal(token: String): Boolean? {
        if (token == "def") {
            _state = ::_function
        }
        return null
    }

    fun _function(token: String): Boolean? {
        if (token != "(") {
            context.restartNewFunction(token)
            context.addToLongFunctionName("(")
        } else {
            _state = ::_dec
        }

        return null
    }

    fun _dec(token: String): Boolean? {
        if (token == ")") {
            _state = ::_stateColon
        } else {
            context.parameter(token)
            return null
        }
        context.addToLongFunctionName(" $token")
        return null
    }

    fun _stateColon(token: String): Boolean? {
        if (token == ":") {
            next(::_stateFirstLine)
        } else {
            next(::_stateGlobal)
        }

        return null
    }

    fun _stateFirstLine(token: String): Boolean? {
        _state = ::_stateGlobal
        if (token.startsWith(""""""""") || token.startsWith("'''")) {
            context.addNloc(-token.count { it == '\n' } - 1)
        }
        _stateGlobal(token)

        return null
    }
}


