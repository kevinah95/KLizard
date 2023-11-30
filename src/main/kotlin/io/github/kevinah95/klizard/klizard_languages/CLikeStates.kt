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

open class CLikeStates(context: FileInfoBuilder) : CodeStateMachine(context) {
    val parameterBracketOpen = "(<"
    val parameterBracketClose = ")>"
    var bracketStack = mutableListOf<String>()
    var _savedTokens = mutableListOf<String>()

    override var commandsByName = listOf(::_stateFunction, ::_stateDecToImp).associateBy { it.name }

    open fun tryNewFunction(name: String) {
        context.tryNewFunction(name)
        _state = ::_stateFunction
        if (name == "operator") {
            _state = ::_stateOperator
        }
    }

    override fun _stateGlobal(token: String): Boolean? {
        if (token[0].isLetter() || token[0] in "_~") {
            tryNewFunction(token)
        }

        return null
    }

    fun _stateFunction(token: String): Boolean? {
        if (token == "(") {
            next(_stateDec, token)
        } else if (token == "::") {
            context.addToFunctionName(token)
            next(::_stateNameWithSpace)
        } else if (token == "<") {
            next(_stateTemplateInName, token)
        } else {
            next(::_stateGlobal, token)
        }
        return null
    }

    val _stateTemplateInName = readInsideBracketsThen("<>", "_stateFunction") {token ->
        context.addToFunctionName(token)
    }

    fun _stateOperator(token: String): Boolean? {
        if (token != "(") {
            _state = ::_stateOperatorNext
        }
        context.addToFunctionName(" $token")

        return null
    }

    fun _stateOperatorNext(token: String): Boolean? {
        if (token == "(") {
            _stateFunction(token)
        } else {
            context.addToFunctionName(" $token")
        }

        return null
    }

    fun _stateNameWithSpace(token: String): Boolean? {
        _state = if (token == "operator") {
            ::_stateOperator
        } else {
            ::_stateFunction
        }
        context.addToFunctionName(token)
        return null
    }

    val _stateDec = readInsideBracketsThen("()", "_stateDecToImp") { token ->
        if (token in parameterBracketOpen) {
            bracketStack.add(token)
        } else if (token in parameterBracketClose) {
            if (bracketStack.isNotEmpty()) {
                bracketStack.removeLast()
            } else {
                next(::_stateGlobal)
            }
        } else if (bracketStack.size == 1) {
            context.parameter(token)
            //TODO: Check this
            return@readInsideBracketsThen
        }
        context.addToLongFunctionName(token)
    }

    fun _stateDecToImp(token: String): Boolean? {
        if (token in listOf("const", "&", "&&")) {
            context.addToLongFunctionName(" $token")
        } else if (token == "throw") {
            _state = _stateThrow
        } else if (token == "throws") {
            _state = _stateThrows
        } else if (token == "->") {
            _state = _stateTrailingReturn
        } else if (token == "noexcept") {
            _state = ::_stateNoexcept
        } else if (token == "(") {
            val longName = context.currentFunction.longName
            tryNewFunction(longName)
            _stateFunction(token)
        } else if (token == "{") {
            next(::_stateEnteringImp, "{")
        } else if (token == ":") {
            _state = ::_stateInitializationList
        } else if (token == "[") {
            _state = _stateAttribute
            _state(token)
        } else if (!(token[0].isLetter() || token[0] == '_')) {
            _state = ::_stateGlobal
            _state(token)
        } else {
            _state = ::_stateOldCParams
            _savedTokens = mutableListOf(token)
        }

        return null
    }

    val _stateThrow = readInsideBracketsThen("()") { _ ->
        _state = ::_stateDecToImp
    }


    val _stateThrows = readUntilThen(";{") { token, _ ->
        _state = ::_stateDecToImp
        _state(token)
    }

    fun _stateNoexcept(token: String): Boolean? {
        if (token == "(") {
            _state = _stateThrow
        } else {
            _state = ::_stateDecToImp
        }

        _state(token)

        return null
    }

    val _stateTrailingReturn = readUntilThen(";{") { token, _ ->
        _state = ::_stateDecToImp
        _state(token)
    }

    open fun _stateOldCParams(token: String): Boolean? {
        _savedTokens.add(token)
        if (token == ";") {
            _savedTokens = mutableListOf<String>()
            _state = ::_stateDecToImp
        } else if (token == "{") {
            if (_savedTokens.size == 2) {
                _savedTokens = mutableListOf<String>()
                _stateDecToImp(token)
                return null
            }

            _state = ::_stateGlobal

            for (tkn in _savedTokens) {
                _state(tkn)
            }
        } else if (token == "(") {
            _state = ::_stateGlobal
            for (tkn in _savedTokens) {
                _state(tkn)
            }
        }

        return null
    }

    fun _stateInitializationList(token: String): Boolean? {
        _state = _stateOneInitialization
        if (token == "{") {
            next(::_stateEnteringImp, "{")
        }

        return null
    }


    val _stateOneInitialization = readUntilThen("({") { token, tokens ->
        if (token == "(") {
            _state = _stateInitializationValue1
        } else {
            _state = _stateInitializationValue2
        }
        _state(token)
    }


    val _stateInitializationValue1 = readInsideBracketsThen("()", null) { _ ->
        _state = ::_stateInitializationList
    }

    val _stateInitializationValue2 = readInsideBracketsThen("{}", null) { _ ->
        _state = ::_stateInitializationList
    }

    fun _stateEnteringImp(token: String): Boolean? {
        context.confirmNewFunction()
        next(_stateImp, token)
        return null
    }

    val _stateImp = readInsideBracketsThen("{}", null) {_ ->
        _state = ::_stateGlobal
    }

    val _stateAttribute = readInsideBracketsThen("[]", "_stateDecToImp") {_ ->
        // pass
    }
}