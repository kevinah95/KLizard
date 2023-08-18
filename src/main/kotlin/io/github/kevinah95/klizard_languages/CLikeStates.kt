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

class CLikeStates(context: FileInfoBuilder) : CodeStateMachine(context) {
    val parameterBracketOpen = "(<"
    val parameterBracketClose = ")>"
    var bracketStack = mutableListOf<String>()
    var _savedTokens = mutableListOf<String>()

    fun tryNewFunction(name: String) {
        context.tryNewFunction(name)
        _state = ::_stateFunction
        if (name == "operator") {
            _state = ::_stateOperator
        }
    }

    override val _stateGlobal: (token: String) -> Unit
        get() = {token ->
            if (token[0].isLetter() || token[0] in "_~") {
                tryNewFunction(token)
            }
        }

    fun _stateFunction(token: String) {
        if (token == "("){
            next(::_stateDec, token)
        } else if (token == "::"){
            context.addToFunctionName(token)
            next(::_stateNameWithSpace)
        } else if (token == "<") {
            next(::_stateTemplateInName, token)
        } else {
            next(_stateGlobal, token)
        }

    }

    fun _stateTemplateInName(token: String) = readInsideBracketsThen("<>", "_state_function") {
        context.addToFunctionName(token)
    }

    fun _stateOperator(token: String) {
        if (token != "("){
            _state = ::_stateOperatorNext
        }
        context.addToFunctionName(" $token")
    }

    fun _stateOperatorNext(token: String) {
        if (token == "(") {
            _stateFunction(token)
        } else {
            context.addToFunctionName(" $token")
        }
    }

    fun _stateNameWithSpace(token: String) {
        _state = if(token == "operator") {
            ::_stateOperator
        } else {
            ::_stateFunction
        }
        context.addToFunctionName(token)
    }

    fun _stateDec (token: String) = readInsideBracketsThen("()", "_state_dec_to_imp") {
        if (token in parameterBracketOpen){
            bracketStack.add(token)
        } else if (token in parameterBracketClose){
            if (bracketStack.isNotEmpty()){
                bracketStack.removeLast()
            } else {
                next(_stateGlobal)
            }
        } else if (bracketStack.size == 1){
            context.parameter(token)
            return
        }
        context.addToLongFunctionName(token)
    }

    fun _stateDecToImp(token: String) {
        if (token in listOf("const", "&", "&&")){
            context.addToLongFunctionName(" $token")
        } else if (token == "throw"){
            _state = ::_stateThrow
        } else if (token == "throws"){
            _state = ::_stateThrows
        } else if (token == "->"){
            _state = ::_stateTrailingReturn
        } else if (token == "noexcept"){
            _state = ::_stateNoexcept
        } else if (token == "("){
            val longName = context.currentFunction.longName
            tryNewFunction(longName)
            _stateFunction(token)
        } else if (token == "{"){
            next(::_stateEnteringImp, "{")
        } else if (token == ":"){
            _state = ::_stateInitializationList
        } else if (token == "["){
            _state = ::_stateAttribute
            _state?.invoke(token)
        } else if (!(token[0].isLetter() || token[0] == '_')) {
            _state = _stateGlobal
            _state?.invoke(token)
        } else {
            _state = ::_stateOldCParams
            _savedTokens = mutableListOf(token)
        }
    }

    fun _stateThrow(token: String) = readInsideBracketsThen("()") {
        _state = ::_stateDecToImp
    }

    fun _stateThrows(token: String) = readUntilThen(";{", "") { token, _ ->
        _state = ::_stateDecToImp
        _state?.invoke(token)
    }

    fun _stateNoexcept(token: String) {
        if (token == "("){
            _state = ::_stateThrow
        } else {
            _state = ::_stateDecToImp
        }

        _state?.invoke(token)
    }

    fun _stateTrailingReturn(token: String) = readUntilThen(";{", "") { token, _ ->
        _state = ::_stateDecToImp
        _state?.invoke(token)
    }

    fun _stateOldCParams(token: String) {
        _savedTokens.add(token)
        if (token == ";"){
            _savedTokens = mutableListOf<String>()
            _state = ::_stateDecToImp
        } else if(token == "{"){
            if (_savedTokens.size == 2){
                _savedTokens = mutableListOf<String>()
                _stateDecToImp(token)
                return
            }

            _state = _stateGlobal

            for(tkn in _savedTokens){
                _state?.invoke(tkn)
            }
        } else if (token == "(") {
            _state = _stateGlobal
            for (tkn in _savedTokens){
                _state?.invoke(tkn)
            }
        }
    }

    fun _stateInitializationList (token: String) {
        //TODO: _state = ::_stateOneInitialization
        if(token == "{"){
            next(::_stateEnteringImp, "{")
        }
    }

    fun _stateOneInitialization(token: String, tokens: List<String>) = readUntilThen("({", "") {token, tokens ->
        if (token == "("){
            _state = ::_stateInitializationValue1
        } else{
            _state = ::_stateInitializationValue2
        }
        _state?.invoke(token)
    }

    fun _stateInitializationValue1(token: String) = readInsideBracketsThen("()", null, token) {
        _state = ::_stateInitializationList
    }
    fun _stateInitializationValue2(token: String) = readInsideBracketsThen("{}", null, token) {
        _state = ::_stateInitializationList
    }

    fun _stateEnteringImp(token: String) {
        context.confirmNewFunction()
        next(::_stateImp, token)
    }

    fun _stateImp(token: String) = readInsideBracketsThen("{}", null, token){
        _state = _stateGlobal
    }

    fun _stateAttribute(token: String) = readInsideBracketsThen("[]", "_state_dec_to_imp", token) {
        // pass
    }


}