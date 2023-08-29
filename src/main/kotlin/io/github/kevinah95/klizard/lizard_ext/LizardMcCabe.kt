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

package io.github.kevinah95.klizard.lizard_ext

class LizardMcCabe(): ExtensionBase() {

    override fun _stateGlobal(token: String) {
        if (token == "case") {
            _state = ::_inCase
        }
    }

    fun _inCase(token: String){
        if (token == ":"){
            _state = ::_afterACase
        }
    }

    fun _afterACase(token: String) {
        if (token == "case"){
            context.addCondition(-1)
            if (context::class.members.any { it.name == "add_nd_condition" }){
                //context.javaClass.getMethod("add_nd_condition").invoke(-1)
                TODO("implement this")
            }
            next(::_inCase)
        } else {
            next(::_stateGlobal)
        }
    }

}