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

package io.github.kevinah95

class NestingStack {
    var nestingStack: MutableList<Nesting> = mutableListOf()
    var pendingFunction: FunctionInfo? = null
    //TODO: var functionStack it is probable not necessary

    fun withNamespace(name: String): String {
        return (nestingStack.map { it.nameInSpace } + listOf(name)).joinToString("")
    }

    fun addBareNesting() {
        nestingStack.add(_createNesting())
    }

    fun addNamespace(token: String) {
        pendingFunction = null
        nestingStack.add(Namespace(token))
    }

    fun startNewFunctionNesting(function: FunctionInfo) {
        pendingFunction = function
    }

    fun _createNesting(): Nesting {
        val tmp = pendingFunction
        pendingFunction = null
        if (tmp != null) {
            return tmp as Nesting
        }
        return Nesting()
    }

    fun popNesting(): Nesting? {
        pendingFunction = null
        if (nestingStack.isNotEmpty()) {
            return nestingStack.removeLast()
        }
        return null
    }

    val currentNestingLevel: Int
        get() = nestingStack.size

    val lastFunction: FunctionInfo?
        get() {
            val functions = nestingStack.filterIsInstance<FunctionInfo>()
            return if (functions.isNotEmpty()) {
                functions.last()
            } else {
                null
            }
        }
}