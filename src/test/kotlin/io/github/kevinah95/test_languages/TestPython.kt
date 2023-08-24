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
            class namespace1:
                def simple_function():
                    if IamOnEarth:
                        return toMars()
        """.trimMargin()

        val functions = getPythonFunctionList(sourceCode)
        assertEquals(1, functions.size)
        assertEquals("simple_function", functions[0].name)
        assertEquals(2, functions[0].cyclomaticComplexity)
        //TODO: test when max_nesting_depth is implemeted
        assertEquals(4, functions[0].endLine)
        assertEquals("simple_function( )", functions[0].longName)
    }

    @Test
    fun testTwoSimplePythonFunction() {
        val sourceCode = """
            def foo():
                #'
                return False

            def bar():
                if foo == 'bar':
                    return True
                """.trimIndent()
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(2, functions.size)
    }
}