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

    override fun _stateGlobal(token: String): Boolean? {
        if (token == FUNC_KEYWORD) {
            _state = ::_functionName
            context.pushNewFunction("")
        } else if (token in setOf("{")) {
            subState(statemachineClone()._state)
        } else if (token in setOf("}")) {
            statemachineReturn()
        }

        return null
    }

    fun _functionName(token: String): Boolean? {
        if (token != "`") {
            if (token == "(") {
                if (context.stackedFunctions.size > 0 && context.stackedFunctions.last().name != "*global*") {
                    return next(_functionDec, token)
                } else {
                    return next(_memberFunction, token)
                }
            }
            if (token == "{") {
                return next(::_expectFunctionImpl, token)
            }
            context.addToFunctionName(token)
            _state = ::_expectFunctionDec
        }
        return null
    }

    fun _expectFunctionDec(token: String): Boolean? {
        if (token == "(") {
            next(_functionDec, token)
        } else if (token == "<") {
            next(_generalize, token)
        } else {
            _state = ::_stateGlobal
        }

        return null
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



    fun _expectFunctionImpl(token: String): Boolean? {
        if (token == "{" && lastToken != "interface") {
            next(::_functionImpl, token)
        }

        return null
    }

    fun _functionImpl(token: String): Boolean? {
        val callback = {
            _state = ::_stateGlobal
            context.endOfFunction()
        }
        ///////////////////////////////////////
        subState(statemachineClone()._state, callback)

        return null
    }



}