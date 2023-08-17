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

class FunctionInfo (var name: String, var filename: String, var startLine: Int = 0,  ccn: Int = 1): Nesting {

    var tokenCount: Int
    var cyclomaticComplexity: Int
    var nloc: Int
    var longName: String
    var endLine: Int
    var fullParameters: MutableList<String>
    var topNestingLevel: Int
    var fanIn: Int
    var fanOut: Int
    var generalFanOut: Int
    init {
        cyclomaticComplexity = ccn
        nloc = 1
        tokenCount = 1
        longName = name
        endLine = startLine
        fullParameters = mutableListOf()
        topNestingLevel = -1
        fanIn = 0
        fanOut = 0
        generalFanOut = 0
    }

    override val nameInSpace: String
        get() = "$name."

    val unqualifiedName: String
        get() = name.split("::").last()

    //TODO: location

    val parameterCount: Int
        get() = fullParameters.size

    val parameters : List<String>
        get() {

            val matches = fullParameters.map { Regex("""(\w+)(\s=.*)?${'$'}""").matchEntire(it) }
            return matches.filterNotNull().map { it.groupValues[1] }
        }

    val length: Int
        get() = endLine - startLine + 1

    fun addToFunctionName(app: String) {
        name += app
        longName += app
    }

    fun addToLongName(app: String) {
        if(longName.isNotEmpty()){
            if (longName.last().isLetter() and app[0].isLetter()){
                longName += " "
            }
        }
        longName += app
    }

    fun addParameter(token: String) {
        addToLongName(" $token")

        if (fullParameters.isEmpty()){
            fullParameters.add(token)
        } else if(token == ",") {
            fullParameters.add("")
        } else {
            fullParameters[fullParameters.lastIndex] += " $token"
        }
    }
}