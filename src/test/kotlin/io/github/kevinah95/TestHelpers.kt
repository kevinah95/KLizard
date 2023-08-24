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

fun getCppFileinfo(sourceCode: String): FileInformation {
    return KLizard().analyzeFile.analyzeSourceCode("a.cpp", sourceCode)
}

fun getCppFileinfoWithExtension(sourceCode: String, extension: Any): FileInformation {
    return FileAnalyzer(KLizard().getExtensions(listOf(extension))).analyzeSourceCode("a.cpp", sourceCode)
}

fun getCppFunctionListWithExtension(sourceCode: String, extension: Any): MutableList<FunctionInfo> {
    return getCppFileinfoWithExtension(sourceCode, extension).functionList
}

fun getPythonFunctionListWithExtension(sourceCode: String, extension: Any): MutableList<FunctionInfo> {
    return FileAnalyzer(KLizard().getExtensions(listOf(extension))).analyzeSourceCode("a.py", sourceCode).functionList
}

fun getCppFunctionList(sourceCode: String): MutableList<FunctionInfo> {
    return getCppFileinfo(sourceCode).functionList
}
