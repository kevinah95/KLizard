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

open class GoLikeStates(context: FileInfoBuilder): CodeStateMachine(context) {
    val FUNC_KEYWORD = "func"

    override fun _stateGlobal(token: String) {
        if (token == FUNC_KEYWORD) {
            _state = ::_functionName
            context.pushNewFunction("")
        } else if (token in setOf("{")) {
            subState(statemachineClone()._state)
        } else if (token in setOf("}")) {
            statemachineReturn()
        }
    }

    fun _functionName(token: String) {
        if (token != "`") {
            if (token == "(") {
                if (context.stackedFunctions.size > 0 && context.stackedFunctions.last().name != "*global*") {
                    next(_functionDec, token)
                    return
                } else {
                    next(_memberFunction, token)
                    return
                }
            }
            if (token == "{") {
                next(::_expectFunctionImpl, token)
                return
            }
            context.addToFunctionName(token)
            _state = ::_expectFunctionDec
        }
    }

    fun _expectFunctionDec(token: String) {
        if (token == "(") {
            next(_functionDec, token)
        } else if (token == "<") {
            next(_generalize, token)
        } else {
            _state = ::_stateGlobal
        }
    }

    val _generalize = readInsideBracketsThen("<>", "_expectFunctionDec") {token ->
        // implementation
    }

    val _memberFunction = readInsideBracketsThen("()", "_functionName") {token ->
        context.addToLongFunctionName(token)
    }

    val _functionDec = readInsideBracketsThen("()", "_expectFunctionImpl") {token ->
        if (token !in listOf("(", ")")) {
            context.parameter(token)
        }
    }



    fun _expectFunctionImpl(token: String) {
        if (token == "{" && lastToken != "interface") {
            next(::_functionImpl, token)
        }
    }

    fun _functionImpl(token: String) {
        val callback = {
            _state = ::_stateGlobal
            context.endOfFunction()
        }
        subState(statemachineClone()._state, callback)
    }



}