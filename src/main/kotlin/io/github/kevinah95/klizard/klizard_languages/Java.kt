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

class JavaReader : CLikeReader() {
    override var ext: MutableList<String> = mutableListOf("java")

    override val languageNames: List<String> = listOf("java")

    override operator fun invoke(_context: FileInfoBuilder) {
        context = _context
        parallelStates = listOf(
            JavaStates(context),
            CLikeNestingStackStates(context)
        )
    }
}

class JavaStates(context: FileInfoBuilder) : CLikeStates(context) {

    override fun _stateOldCParams(token: String) {
        if (token == "{"){
            _stateDecToImp(token)
        }
    }

    override fun tryNewFunction(name: String) {
        context.tryNewFunction(name)
        _state = ::_stateFunction
    }

    override fun _stateGlobal(token: String) {
        if (token == "@"){
            _state = ::_stateDecorator
            return
        }

        super._stateGlobal(token)
    }

    fun _stateDecorator(token: String) {
        _state = ::_statePostDecorator
    }

    fun _statePostDecorator(token: String) {
        if (token == "."){
            _state = ::_stateDecorator
        } else {
            _state = ::_stateGlobal
            _state(token)
        }
    }
}