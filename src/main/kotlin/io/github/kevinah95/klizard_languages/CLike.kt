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

interface CCppCommentsMixin {
    fun getCommentFromToken(token: String): String? {
        if (token.startsWith("/*") || token.startsWith("//")) {
            return token.substring(2)
        }
        return null
    }
}

class CLikeReader : CodeReader(), CCppCommentsMixin {
    override lateinit var context: FileInfoBuilder
    override var ext: MutableList<String> = mutableListOf("c", "cpp", "cc", "mm", "cxx", "h", "hpp")
    val languageNames = listOf("cpp", "c")

    // TODO: Add RegexOption.DOT_MATCHES_ALL when available for common
    val macroPattern = Regex("""#\s*(\w+)\s*(.*)""", RegexOption.MULTILINE)

    init {
        // TODO: parallel_states


    }

    override operator fun invoke(_context: FileInfoBuilder) {
        context = _context
    }

    override fun preprocess(tokens: Sequence<String>) = sequence<String> {
        var tilde: Boolean = false

        for (token in tokens) {
            if (token == "~") {
                tilde = true
            } else if (tilde) {
                tilde = false
                yield("~$token")
            } else if (!token.all { it.isWhitespace() } || token == "\n") {
                val macro = macroPattern.matchEntire(token)
                if (macro != null) {
                    if (macro.groupValues[1] in listOf("if", "ifdef", "elif")) {
                        context.addCondition()
                    } else if (macro.groupValues[1] == "include") {
                        yield("#include")
                        try {
                            if (macro.groupValues[2] != null) yield(macro.groupValues[2])
                        } catch (_: Exception) {
                            yield("\"\"")
                        }
                        try {
                            for (x in macro.groupValues[2].lines().drop(1)) {
                                yield("\n")
                            }
                        } catch (_: Exception) {
                        }
                    }
                } else {
                    yield(token)
                }
            }
        }
    }
}


class CppRValueRefStates(context: FileInfoBuilder) : CodeStateMachine(context) {

    override val _stateGlobal: (token: String) -> Unit = {
        if (it == "&&") {
            //TODO: next(_rValueRef)
        } else if (it == "typedef") {
            //TODO: next(_typedef)
        }
    }


    fun _rValueRef(token: String, tokens: List<String>) =
        readUntilThen("=;{})", token) { token: String, tokens: List<String> ->
            if (token == "=") {
                context.addCondition(-1)
            }
            next(_stateGlobal)
        }

    fun _typedef(token: String, tokens: List<String>) =
        readUntilThen(";", token) { token: String, tokens: List<String> ->
            context.addCondition(-tokens.count { it == "&&" })
            next(_stateGlobal)
        }

    fun _typedef2 (token: String, tokens: List<String>) {
        readUntilThen(";", token, ::_typedef)
    }

//    val r = readUntilThen(";", token) { token: String, tokens: List<String> ->
//        context.addCondition(-tokens.count { it == "&&" })
//        next(_stateGlobal)
//    }
}

