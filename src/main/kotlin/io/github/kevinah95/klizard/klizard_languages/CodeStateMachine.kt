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

open class CodeStateMachine {
    lateinit var context: FileInfoBuilder
    var savedState: (token: String) -> Boolean?

    var lastToken: String? = null
    var toExit: Boolean = false
    var callback: (() -> Unit)? = null
    var rutTokens: MutableList<String> = mutableListOf()
    var brCount: Int = 0

    var _state: ((token: String) -> Boolean?) = ::_stateGlobal


    open var commandsByName = listOf(::_stateGlobal).associateBy { it.name }

    constructor() {}

    constructor(context: FileInfoBuilder) {
        this.context = context
    }

    init {
        savedState = _state
    }

    fun statemachineClone(): CodeStateMachine {
        return CodeStateMachine(this.context)
    }

    fun next(state: ((token: String) -> Boolean?), token: String? = null): Boolean? {
        _state = state
        if (token != null){
            return this.invoke(token)
        }
        return null
    }

    fun nextIf(state: ((token: String) -> Boolean?), token: String, expected: String) {
        if (token != expected) {
            return
        }
        next(state, token)
    }

    fun statemachineReturn() {
        toExit = true
        statemachineBeforeReturn()
    }

    fun subState(state: ((token: String) -> Boolean?), callback: (() -> Unit)? = null, token: String? = null) {
        savedState = _state
        this.callback = callback
        next(state, token)
    }

    operator fun invoke(token: String, reader: CodeReader? = null): Boolean? {
        //TODO: Check this != null
        if (_state(token) != null) {
            next(savedState)
            if (callback != null) {
                callback?.let { it() }
            }
        }
        lastToken = token
        if (toExit) {
            return true
        }
        // TODO: Verify this
        return null
    }

    open fun _stateGlobal(token: String): Boolean? {
        return null
    }

    fun statemachineBeforeReturn() {}

    fun readInsideBracketsThen(brs: String, endState: String? = null, function: (String) -> Unit): (String) -> Boolean? {
        fun decorator(func: ((String) -> Unit)): (String) -> Boolean? {
            fun readUntilMatchingBrackets(token: String): Boolean? {

                brCount += when (token) {
                    brs[0].toString() -> 1
                    brs[1].toString() -> -1
                    else -> 0
                }

                if (brCount == 0 || endState != null) {
                    func(token)
                }

                if (brCount == 0 && endState != null) {
                    // TODO: Review this method: https://stackoverflow.com/questions/69622835/how-to-call-a-function-in-kotlin-from-a-string-name
                    commandsByName[endState]?.let { next(it) }
                }

                return null
            }
            return ::readUntilMatchingBrackets
        }
        return decorator(function)
    }

    fun readUntilThen(tokens: String, function: (String, List<String>) -> Unit): (String) -> Boolean? {
        fun decorator(func: ((String, List<String>) -> Unit)): (String) -> Boolean? {
            fun readUntilThenToken(token: String): Boolean? {

                if (token in tokens){
                    func(token, rutTokens)
                    rutTokens = mutableListOf()
                } else {
                    rutTokens.add(token)
                }
                return null
            }
            return ::readUntilThenToken
        }
        return decorator(function)
    }
}