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

import io.github.kevinah95.klizard.klizard_languages.CodeReader
import io.github.kevinah95.klizard.klizard_languages.CodeStateMachine

open class ExtensionBase(): CodeStateMachine() {

    operator fun invoke(tokens: Sequence<String>, reader: CodeReader) = sequence<String> {
        context = reader.context
        for (token in tokens){
            _state(token)
            yield(token)
        }
    }
}