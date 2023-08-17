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

class FileInfoBuilder(filename: String) {
    var fileinfo = FileInformation(filename, 0)
    var currentLine = 0
    var forgive = false
    var newline = true
    var globalPseudoFunction = FunctionInfo("*global*", filename, 0)
    var currentFunction = globalPseudoFunction
    var stackedFunctions: MutableList<FunctionInfo> = mutableListOf()
    var _nestingStack = NestingStack()

    //TODO: To do verify this. I dont know why this is decorated, but I use _nestingStack instead of decorate_nesting_stack.

    fun popNesting() {
        val nest = _nestingStack.popNesting()
        if (nest is FunctionInfo) {
            val endLine = currentFunction.endLine
            endOfFunction()
            currentFunction = if (_nestingStack.lastFunction != null) {
                _nestingStack.lastFunction!!
            } else{
                globalPseudoFunction
            }

            currentFunction.endLine = endLine
        }
    }

    fun addNloc(count: Int) {
        fileinfo.nloc += count
        currentFunction.nloc += count
        currentFunction.endLine = currentLine
        newline = count > 0
    }

    fun tryNewFunction(name: String) {
        currentFunction = FunctionInfo(
            _nestingStack.withNamespace(name),
            fileinfo.filename,
            currentLine
        )
        currentFunction.topNestingLevel = _nestingStack.currentNestingLevel
    }

    fun confirmNewFunction() {
        _nestingStack.startNewFunctionNesting(currentFunction)
        currentFunction.cyclomaticComplexity = 1
    }

    fun restartNewFunction(name: String) {
        tryNewFunction(name)
        confirmNewFunction()
    }

    fun pushNewFunction(name: String) {
        stackedFunctions.add(currentFunction)
        restartNewFunction(name)
    }

    fun addCondition(inc:Int = 1){
        currentFunction.cyclomaticComplexity += inc
    }

    fun addToLongFunctionName(app: String) {
        currentFunction.addToLongName(app)
    }

    fun addToFunctionName(app: String) {
        currentFunction.addToFunctionName(app)
    }

    fun parameter(token: String) {
        currentFunction.addParameter(token)
    }

    fun endOfFunction() {
        if(!forgive){
            fileinfo.functionList.add(currentFunction)
        }
        forgive = false
        currentFunction = if (stackedFunctions.isNotEmpty()){
            stackedFunctions.removeLast()
        } else {
            globalPseudoFunction
        }
    }
}