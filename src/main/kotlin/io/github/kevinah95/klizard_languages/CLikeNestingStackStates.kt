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

class CLikeNestingStackStates(context: FileInfoBuilder) : CodeStateMachine(context) {
    val __namespace_separators = listOf("<", ":", "final", "[", "extends", "implements")

    override val _stateGlobal: (token: String) -> Unit = { token ->
        if (token == "template") {
            //_state = _template_declaration
        } else if (token == ".") {
            _state = _dot
        } else if (token in setOf("struct", "class", "namespace", "union")) {
            _state = _dot
        } else if (token == "{") {

        } else if (token == "}") {

        }
    }

    val _dot: ((token: String) -> Unit)
        get() { return _stateGlobal }

    val _readNamespace: (token: String) -> Unit = {token ->
        if (token == "["){
            _state = ::_readAttribute
        } else {
            //TODO: _state = ::_readNamespaceName
        }
    }

    //TODO: add decorator read_until_then
    fun _readNamespaceName(token: String, saved: List<String>) {
        _state = _stateGlobal
        if (token == "{") {
            val namespace = saved.takeWhile { it in __namespace_separators }.joinToString("")
            context.addNamespace(namespace)
        }
    }
    fun _templateDeclaration(token: String? = null) = readInsideBracketsThen("<>", "_state_global", token) {
        //do nothing
    }

    fun _readAttribute(token: String) = readInsideBracketsThen("[]", "_read_namespace", null) {
        //do nothing
    }
}