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

class CLikeNestingStackStates(context: FileInfoBuilder) : CodeStateMachine(context) {
    val __namespace_separators = listOf("<", ":", "final", "[", "extends", "implements")

    override var commandsByName = listOf(::_stateGlobal, ::_readNamespace).associateBy { it.name }

    override fun _stateGlobal(token: String): Boolean? {
        if (token == "template") {
            _state = _templateDeclaration
        } else if (token == ".") {
            _state = ::_dot
        } else if (token in setOf("struct", "class", "namespace", "union")) {
            _state = ::_readNamespace
        } else if (token == "{") {
            context._nestingStack.addBareNesting()
        } else if (token == "}") {
            context.popNesting()
        }

        return null
    }

    fun _dot(token: String): Boolean? {
        _state = ::_stateGlobal
        return null
    }

    fun _readNamespace(token: String): Boolean? {
        if (token == "[") {
            _state = _readAttribute
        } else {
            _state = _readNamespaceName
        }
        _state(token)

        return null
    }


    val _readNamespaceName = readUntilThen(")({;") { token: String, saved: List<String> ->
        _state = ::_stateGlobal
        if (token == "{") {
            val namespace = saved.takeWhile { it !in __namespace_separators }.joinToString("")
            context.addNamespace(namespace)
        }
    }

    val _templateDeclaration = readInsideBracketsThen("<>", "_stateGlobal") { _ ->
        //do nothing
    }

    val _readAttribute = readInsideBracketsThen("[]", "_readNamespace") { _ ->
        //do nothing
    }
}