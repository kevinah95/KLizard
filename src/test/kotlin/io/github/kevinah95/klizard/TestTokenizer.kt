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

package io.github.kevinah95.klizard

import io.github.kevinah95.klizard.klizard_languages.CodeReader
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

fun generateTokens(source: String): List<String> {
    return CodeReader.generateTokens(source).toList()
}

class TestGenerateToken {


    fun checkTokens(source: String, vararg expect: String) {
        val tokens = generateTokens(source)
        assertEquals(expect.toList(), tokens)
    }

    @Test
    fun testEmptyString() {
        checkTokens("")
    }

    @Test
    fun testSpaces() {
        checkTokens("\n", "\n")
        checkTokens("\n\n", "\n", "\n")
        checkTokens(" \n", " ", "\n")
    }

    @Test
    fun testDigits() {
        checkTokens("1", "1")
        checkTokens("123", "123")
    }

    @Test
    fun testOperators() {
        checkTokens("-;", "-", ";")
        checkTokens("-=", "-=")
        checkTokens(">=", ">=")
        checkTokens("<=", "<=")
        checkTokens("||", "||")
        checkTokens(">>", ">", ">")
        checkTokens(">>=", ">>=")
        checkTokens("<<=", "<<=")
    }

    @Test
    fun testMore() {
        checkTokens("int a{}", "int", " ", "a", "{", "}")
    }

    @Test
    fun testString() {
        checkTokens("""""""", """""""")
        checkTokens(""""x\"xx")""", """"x\"xx"""", ")")
        checkTokens("""'\''""", """'\''""")
        checkTokens("""'\\\'""", "'", """\""", """\""", """\""", "'")
    }

    @Test
    fun testLineNumber() {
        checkTokens("abc", "abc")
    }

    @Test
    fun testLineNumber2() {
        val tokens = generateTokens("abc\ndef")
        assertTrue("def" in tokens)
    }

    @Test
    fun testWithMultipleLineString() {
        val tokens = generateTokens(""""sss\nsss" t""")
        assertTrue("t" in tokens)
    }

}

class TestGenerateTokenForMacros {
    @Test
    fun testDefine(){
        val define = """#define xx()                       abc"""
        val tokens = generateTokens("$define\n                    int")
        println(tokens)
        assertEquals(listOf( define, "\n", " ".repeat(20), "int"), tokens)
    }

    @Test
    fun testInclude(){
        val tokens = generateTokens("""#include "abc"""")
        assertEquals(listOf("""#include "abc""""), tokens)
    }

    @Test
    fun testIf(){
        val tokens = generateTokens("#if abc\n")
        assertEquals(listOf("#if abc", "\n"), tokens)
    }

    @Test
    fun testIfdef() {
        val tokens = generateTokens("#ifdef abc\n")
        assertEquals(listOf("#ifdef abc", "\n"), tokens)
    }

    @Test
    fun testWithLineContinuerDefine(){
        val tokens = generateTokens("#define a \\\nb\n t")
        assertTrue("t" in tokens)
    }

    @Test
    fun testDefine2() {
        val source = """ # define yyMakeArray(ptr, count, size)     { MakeArray (ptr, count, size); \
                       yyCheckMemory (* ptr); }
                       t
                    """
        val tokens = generateTokens(source)
        assertTrue("t" in tokens)
    }

    @Test
    fun testHalfCommentFollowing() {
        val comment = "#define A/*\n*/"
        val tokens = generateTokens(comment)
        assertEquals(2, tokens.size)
    }

    @Test
    fun testBlockCommentInDefine() {
        val comment = """#define A \\\n/*\\\n*/"""
        val tokens = generateTokens(comment)
        assertEquals(1, tokens.size)
    }
}

class TestGenerateTokenForComments {

    @Test
    fun testCStyleComment(){
        val tokens = generateTokens("""/***\n**/""")
        assertEquals(listOf("""/***\n**/"""), tokens)
    }

    @Test
    fun testCppStyleComment(){
        val tokens = generateTokens("//aaa\n")
        assertEquals(listOf("//aaa", "\n"), tokens)
    }

    //TODO: If you print this in python as "//a\\\nb" the result is newline before b, but in the example below it does not work as expected
    @Test
    fun testCppStyleCommentWithMultipleLines(){
        val tokens = generateTokens("//a\\\nb")
        assertEquals(listOf("//a\\\nb"), tokens)
    }

    @Test
    fun testCommentedComment() {
        val tokens = generateTokens(" /*/*/")
        assertEquals(listOf(" ", "/*/*/"), tokens)
    }

    @Test
    fun testWithCppComments() {
        val tokens = generateTokens("//abc\n t")
        assertTrue("t" in tokens)
    }


    @Test
    fun testWithCComments() {
        val tokens = generateTokens("/*abc\\n*/ t")
        assertTrue("t" in tokens)
    }

    @Test
    fun testWithCCommentsWithBackslashInIt() {
        val comment = "/**a/*/"
        val tokens = generateTokens(comment)
        assertEquals(listOf(comment), tokens)
    }
}