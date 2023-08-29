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

import kotlin.test.Test
import kotlin.test.assertEquals

class Test_Token_Count {

    @Test
    fun testNonFunctionTokensAreCounted(){
        val fileinfo = getCppFileinfo("int i, j;")
        assertEquals(5, fileinfo.tokenCount)
    }

    @Test
    fun testIncludeIsCountedAs2(){
        val fileinfo = getCppFileinfo("#include \"abc.h\"")
        assertEquals(2, fileinfo.tokenCount)
    }

    @Test
    fun testIncludeWithLgAndGgIsCountedAs2(){
        val fileinfo = getCppFileinfo("#include <abc.h>")
        assertEquals(2, fileinfo.tokenCount)
    }

    @Test
    fun testOneFunctionWithNoToken(){
        val result = getCppFunctionList("int fun(){}")
        assertEquals(5, result[0].tokenCount)
    }

    @Test
    fun testOneFunctionWithOneToken(){
        val result = getCppFunctionList("int fun(){;}")
        assertEquals(6, result[0].tokenCount)
    }

    @Test
    fun testOneFunctionWithContent(){
        val result = getCppFunctionList("int fun(){if(a){xx;}}")
        assertEquals(13, result[0].tokenCount)
    }

    @Test
    fun testOneFunctionWithCommentsOnly(){
        val result = getCppFunctionList("int fun(){/**/}")
        assertEquals(5, result[0].tokenCount)
    }
}

class TestNLOC {
    @Test
    fun testOneFunctionWithContent(){
        val result = getCppFunctionList("int fun(){if(a){xx;}}")
        assertEquals(1, result[0].nloc)
    }

    @Test
    fun testNlocOfEmptyFunction(){
        val result = getCppFunctionList("int fun(){}")
        assertEquals(1, result[0].nloc)
    }

    @Test
    fun testNloc(){
        val result = getCppFunctionList("int fun(){\n\n\n}")
        assertEquals(2, result[0].nloc)
    }

    @Test
    fun testNlocWithNewLineInComment(){
        val result = getCppFunctionList("int fun(){/*\n*/}")
        assertEquals(2, result[0].nloc)
    }

    @Test
    fun testNlocWithCommentBetweenNewLines(){
        val result = getCppFunctionList("int fun(){\n/*\n*/\n}")
        assertEquals(2, result[0].nloc)
    }

    @Test
    fun testNloc2(){
        val result = getCppFunctionList("int fun(){aa();\n\n\n\nbb();\n\n\n}")
        assertEquals(3, result[0].nloc)
        assertEquals(1, result[0].startLine)
        assertEquals(8, result[0].endLine)
    }

    fun checkFileNloc(expect: Int, source: String){
        val fileinfo = getCppFileinfo(source)
        assertEquals(expect, fileinfo.nloc)
    }

    @Test
    fun testLastLineWithoutReturnShouldBeCountedInFileinfo(){
        checkFileNloc(1, ";\n")
        checkFileNloc(2, ";\n\n;\n")
        checkFileNloc(2, ";\n;")
        checkFileNloc(1, "fun(){}")
        checkFileNloc(1, "fun(){};\n")
    }
}

class TestLOC {

    @Test
    fun testHavingEmptyLine(){
        val result = getCppFunctionList("\nint fun(){}")
        assertEquals(2, result[0].startLine)
    }

    @Test
    fun testNewlineInMacro(){
        val result = getCppFunctionList("#define a \\\nb\nint fun(){}")
        assertEquals(3, result[0].startLine)
    }

    @Test
    fun testHavingEmptyLineThatHasSpaces(){
        val result = getCppFunctionList("  \nint fun(){}")
        assertEquals(2, result[0].startLine)
    }

    @Test
    fun testHavingMultipleLineComments(){
        val sourceCode = """int fun(){
                                /*2
                                  3
                                  4*/
                            }""".trimIndent()
        val result = getCppFunctionList(sourceCode)
        assertEquals(5, result[0].endLine)
    }
}

class TestFileNLOC {
    @Test
    fun testEmptyFileShouldHas0Nloc(){
        val fileinfo = getCppFileinfo("")
        assertEquals(0, fileinfo.nloc)
    }

    @Test
    fun testOneLineFileShouldHas1Nloc(){
        val fileinfo = getCppFileinfo("a")
        assertEquals(1, fileinfo.nloc)
    }

    @Test
    fun testOneLineFileWithNewlineAtTheEndShouldHas1Nloc(){
        val fileinfo = getCppFileinfo("a\n")
        assertEquals(1, fileinfo.nloc)
    }

    @Test
    fun testTwoOneLineFileShouldHas2Nloc(){
        val fileinfo = getCppFileinfo("a\nb")
        assertEquals(2, fileinfo.nloc)
    }

    @Test
    fun testCommentShouldNotCount(){
        val fileinfo = getCppFileinfo("//abc")
        assertEquals(0, fileinfo.nloc)
    }
}