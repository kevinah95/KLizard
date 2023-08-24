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

package io.github.kevinah95.test_languages

import io.github.kevinah95.FunctionInfo
import io.github.kevinah95.getPythonFunctionListWithExtension
import io.github.kevinah95.klizard_languages.PythonReader
import kotlin.test.Test
import kotlin.test.assertEquals

fun getPythonFunctionList(sourceCode: String): MutableList<FunctionInfo> {
    return getPythonFunctionListWithExtension(sourceCode, listOf<Any>())
}

class testTokenizerForPython {

    @Test
    fun testCommentWithQuote() {
        val tokens = PythonReader.generateTokens("#'\n''")
        assertEquals(listOf("#'", "\n", "''").size, tokens.toList().size)
        assertEquals(listOf("#'", "\n", "''"), tokens.toList())
    }

}

//TODO: implement class Test_Python_nesting_level(unittest.TestCase):

class testParserForPython {

    @Test
    fun testEmptySourceShouldReturnNoFunction() {
        val functions = getPythonFunctionList("")
        assertEquals(0, functions.size)
    }

    @Test
    fun testSimplePythonFunction() {
        val sourceCode = """
            |    def simple_function():
            |        pass
        """.trimMargin()

        val functions = getPythonFunctionList(sourceCode)
        assertEquals(1, functions.size)
    }
}