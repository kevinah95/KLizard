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
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

fun getPythonFunctionList(sourceCode: String): MutableList<FunctionInfo> {
    return getPythonFunctionListWithExtension(sourceCode, listOf<Any>())
}

class TestTokenizerForPython {

    @Test
    fun testCommentWithQuote() {
        val tokens = PythonReader.generateTokens("#'\n''")
        assertEquals(listOf("#'", "\n", "''").size, tokens.toList().size)
        assertEquals(listOf("#'", "\n", "''"), tokens.toList())
    }

}

//TODO: implement class Test_Python_nesting_level(unittest.TestCase):

class TestParserForPython {

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

    @Test
    fun testMultiLineFunctionDefFunctionEnd() {
        val sourceCode = """
            
            def foo(arg1,
                arg2,
            ):
                # comment
                return True

            def foo2(arg1,
                arg2,
                arg3
            ):
                if True:
                    return False
                """.trimIndent()
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(6, functions[0].endLine)
        assertEquals(13, functions[1].endLine)
    }

    @Test
    fun testMultiLineFunctionDefWithIndentationMoreThanFunctionBody() {
        val sourceCode = """
            def function(arg1,
                     arg2
                     ):
                if True:
                    return False
                """.trimIndent()
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(5, functions[0].nloc)
        assertEquals(5, functions[0].endLine)
    }

    @Test
    fun testFunctionSurroundedByGlobalStatements(){
        val sourceCode = """
            
            s1 = 'global statement'
            def function(arg1,
                         arg2
                         ):
                if True:
                    return False
            s2 = 'global statement'
                """.trimIndent()
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(5, functions[0].nloc)
        assertEquals(7, functions[0].endLine)
    }

    @Test
    fun testParameterCount(){
        val sourceCode = """
            
            class namespace2:
                def function_with_2_parameters(a, b):
                    pass
                """.trimIndent()
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(2, functions[0].parameterCount)
    }

    @Test
    fun testParameterCountWithDefaultValue() {
        val sourceCode = """
            class namespace3:
                def simple_function(self):
                    pass

                blah = 42
                """.trimIndent()
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(1, functions.size)
        assertEquals("simple_function", functions[0].name)
        assertEquals(3, functions[0].endLine)
    }

    @Test
    fun testTopLevelFunctions() {
        val sourceCode = """
            def top_level_function_for_test():
                pass
                """.trimIndent()
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(1, functions.size)
    }

    @Test
    fun test2TopLevelFunctions(){
        val sourceCode = """
            def a():
                pass
            def b():
                pass
                """.trimIndent()
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(2, functions.size)
        assertEquals("a", functions[0].name)
        assertEquals("b", functions[1].name)
    }

    @Test
    fun test2Functions(){
        val sourceCode = """
            class namespace4:
                def function1(a, b):
                    pass
                def function2(a, b):
                    pass
                """.trimIndent()
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(2, functions.size)
        assertEquals("function1", functions[0].name)
        assertEquals("function2", functions[1].name)
    }

    @Test
    fun testNestedFunctions() {
        val sourceCode = """
            class namespace5:
                def function1(a, b):
                    def function2(a, b):
                        pass
                    a = 1 if b == 2 else 3
                """.trimIndent()
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(2, functions.size)
        assertEquals("function1.function2", functions[0].name)
        assertEquals(4, functions[0].endLine)
        assertEquals("function1", functions[1].name)
        assertEquals(5, functions[1].endLine)
        assertEquals(2, functions[1].cyclomaticComplexity)
        //TODO: implement when max_nesting_depth is completed:  assertEquals(2, functions[1].maxNestingDepth)
    }

    @Test
    fun testNestedFunctionsEndedAtEof(){
        val sourceCode = """
            class namespace6:
                def function1(a, b):
                    def function2(a, b):
                        pass
                """.trimIndent()
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(2, functions.size)
        assertEquals("function1.function2", functions[0].name)
        assertEquals(4, functions[0].endLine)
        assertEquals("function1", functions[1].name)
        assertEquals(4, functions[1].endLine)
    }

    @Test
    fun testNestedFunctionsEndedAtSameLine(){
        val sourceCode = """
            class namespace7:
                def function1(a, b):
                    def function2(a, b):
                        pass
                def function3():
                    pass
                """.trimIndent()
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(3, functions.size)
        assertEquals("function1.function2", functions[0].name)
        assertEquals(4, functions[0].endLine)
        assertEquals("function1", functions[1].name)
        assertEquals(4, functions[1].endLine)
    }

    @Test
    @Ignore("This test is going to fail, that's why it is marked with a xtest...")
    fun xtestOneLineFunctions(){
        val sourceCode = """
            class namespace8:
                def a( ):pass
                def b( ):pass
                """.trimIndent()
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(2, functions.size)
        assertEquals("a", functions[0].name)
        assertEquals("b", functions[1].name)
    }

    @Test
    fun testNestedDepthMetricMultipleContinuousLoopStatements(){
        val sourceCode = """
            class namespace9:
                def function1():
                    if IamOnEarth:
                        if IamOnShip:
                            return toMars()
                """.trimIndent()
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(1, functions.size)
        assertEquals("function1", functions[0].name)
        assertEquals(3, functions[0].cyclomaticComplexity)
        //TODO: implement when max_nesting_depth is completed: assertEquals(2, functions[0].maxNestingDepth)
        assertEquals(5, functions[0].endLine)
    }

    @Test
    fun xtestNestedDepthMetricMultipleDiscreteLoopStatement(){
        val sourceCode = """
            class namespace10:
                def function1():
                    if IamOnEarth:
                        if not IamOnShip:
                            return toMars()
                    elif IamOnMoon:
                        return backEarth()
                """.trimIndent()
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(1, functions.size)
        assertEquals("function1", functions[0].name)
        assertEquals(4, functions[0].cyclomaticComplexity)
        //TODO: implement when max_nesting_depth is completed: assertEquals(2, functions[0].maxNestingDepth)
        assertEquals(7, functions[0].endLine)
    }

    @Test
    fun testCommentIsNotCountedInNloc(){
        val sourceCode = """
            def function_with_comments():
    
                # comment
                pass
                """.trimIndent()
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(2, functions[0].nloc)
    }

    @Test
    fun testOddBlankLine(){
        val sourceCode = "class c:\n" +
                         "    def f():\n" +
                         "  \n" +
                         "         pass\n"
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(4, functions[0].endLine)
    }

    @Test
    fun testOddLineWithComment(){
        val sourceCode = "class c:\n" +
                         "    def f():\n" +
                         "  #\n" +
                         "         pass\n"
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(4, functions[0].endLine)
    }

    @Test
    fun testTabIsSameAs8Spaces(){
        val sourceCode = " ".repeat(7) + "def a():\n" +
                         "\t"    +  "pass\n"
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(2, functions[0].endLine)
    }

    @Test
    fun xtestIfElifAndOrForWhileExceptFinally() {
        val sourceCode = "def a():\n" +
                         "    if elif and or for while except finally\n"
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(9, functions[0].cyclomaticComplexity)
        //TODO: implement when max_nesting_depth is completed: assertEquals(8, functions[0].maxNestingDepth)
    }

    @Test
    fun testBlockStringIsOneToken() {
        val sourceCode = "def a():\n" +
                         "    a = '''\n" +
                         "a b c d e f g h i'''\n"+
                         "    return a\n"
        val functions = getPythonFunctionList(sourceCode)
        assertEquals(9, functions[0].tokenCount)
        assertEquals(4, functions[0].endLine)
    }

    fun checkFunctionInfo(source: String, expectTokenCount: Int, expectNloc: Int, expectEndline: Int) {
        val functions = getPythonFunctionList(source)
        assertEquals(expectTokenCount, functions[0].tokenCount)
        assertEquals(expectNloc, functions[0].nloc)
        assertEquals(expectEndline, functions[0].endLine)
    }

    @Test
    fun testBlockString() {
        checkFunctionInfo("def f():\n a=\"\"\"block string\"\"\"", 7, 2, 2)
        checkFunctionInfo("def f():\n a='''block string'''", 7, 2, 2)
        checkFunctionInfo("def f():\n a='''block string'''", 7, 2, 2)
        checkFunctionInfo("def f():\n a='''block\n string'''", 7, 3, 3)
        checkFunctionInfo("def f():\n a='''block\n '''", 7, 3, 3)
    }

    @Test
    fun testDocstringIsNotCountedInNloc() {
        checkFunctionInfo("def f():\n '''block\n '''\n pass", 6, 2, 4)
    }
}