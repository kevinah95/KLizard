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

import io.github.kevinah95.klizard_languages.CLikeReader
import io.github.kevinah95.klizard_languages.CodeReader
import io.github.kevinah95.klizard_languages.getReaderFor

class FileAnalyzer(extensions: List<(Sequence<String>, CodeReader) -> Sequence<String>>) {
    var processors: List<(Sequence<String>, CodeReader) -> Sequence<String>>

    init {
        processors = extensions
    }

    //TODO: implement __call__

    fun analyzeSourceCode(filename: String, code: String): FileInformation {
        val context = FileInfoBuilder(filename)
        var reader = getReaderFor(filename)
        if (reader == null) {
            reader = CLikeReader()
        }

        reader(context)

        var tokens = CodeReader.generateTokens(code)


        try {
            for (processor in processors) {
                tokens = processor.invoke(tokens, reader)
            }

            for (unused in reader(tokens, reader)) {
                //pass
            }
        } catch (e: Exception) {
            // TODO: implement RecursionError
            println("[skip] fail to process '$filename' with RecursionError - $e")
        }

        return context.fileinfo
    }
}