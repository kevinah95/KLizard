/*
 *
 * Copyright 2012-2023 Kevin Hernández
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

package io.github.kevinah95.klizard

import io.github.kevinah95.klizard.klizard_languages.CodeReader
import io.github.kevinah95.klizard.lizard_ext.ExtensionBase

class KLizard {
    fun preprocessing(tokens: Sequence<String>, reader: CodeReader): Sequence<String> {
        if(reader.javaClass.kotlin.members.any { it.name == "preprocess"}){
            return reader.preprocess(tokens)
        }

        return tokens.filter { token ->  !token.all { it.isWhitespace() } || token == "\n" }
    }

    fun commentCounter(tokens: Sequence<String>, reader: CodeReader) = sequence<String> {
        for (token in tokens){
            val comment: String? = reader.getCommentFromToken(token)
            if (comment != null){
                for(unused in comment.lines().drop(1)){
                    yield("\n")
                }

                if (comment.trim().startsWith("#lizard forgive")){
                    reader.context.forgive = true
                }
                if ("GENERATED CODE" in comment) {
                    return@sequence // TODO: verify behaviour
                }

            } else {
                yield(token)
            }
        }
    }

    fun lineCounter(tokens: Sequence<String>, reader: CodeReader) = sequence<String> {
        val context  = reader.context
        context.currentLine = 1
        var newline = 1
        for(token in tokens){
            if (token != "\n"){
                val count = token.count { c -> c == '\n' }
                context.currentLine += count
                context.addNloc(count + newline)
                newline = 0
                yield(token)
            } else {
                context.currentLine += 1
                newline = 1
            }
        }
    }

    fun tokenCounter(tokens: Sequence<String>, reader: CodeReader) = sequence<String> {
        val context = reader.context
        for(token in tokens){
            context.fileinfo.tokenCount += 1
            context.currentFunction.tokenCount += 1
            yield(token)
        }
    }

    fun conditionCounter(tokens: Sequence<String>, reader: CodeReader) = sequence<String> {
        val conditions = reader.conditions

        for (token in tokens){
            if(token in conditions){
                reader.context.addCondition()
            }
            yield(token)
        }
    }

    fun getExtensions(extensionNames: List<Any>): MutableList<(Sequence<String>, CodeReader) -> Sequence<String>> {
        fun expandExtensions(existing: MutableList<(tokens: Sequence<String>, reader: CodeReader) -> Sequence<String>>): MutableList<(Sequence<String>, CodeReader) -> Sequence<String>> {
            for (name in extensionNames) {
                if (name is ExtensionBase) {
                    existing.add((name)::invoke)
                }
            }
            return existing
        }
        return expandExtensions(mutableListOf(
            ::preprocessing,
            ::commentCounter,
            ::lineCounter,
            ::tokenCounter,
            ::conditionCounter
        ))
    }

    val  analyzeFile: FileAnalyzer = FileAnalyzer(getExtensions(mutableListOf()))


}