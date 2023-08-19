package io.github.kevinah95

import io.github.kevinah95.klizard_languages.CodeReader
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
        val define = """#define xx()                    abc"""
        val tokens = generateTokens("$define\\n                    int")
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

    //TODO: Check the slashes
    @Test
    fun testWithLineContinuerDefine(){
        val tokens = generateTokens("""#define a \\\nb\n t""")
        assertTrue("t" in tokens)
    }

    @Test
    fun testDefine2() {
        // TODO: testDefine2
    }

    @Test
    fun testHalfCommentFollowing() {
        val comment = """#define A/*\n*/"""
        val tokens = generateTokens(comment)
        assertEquals(2, tokens.size)
    }

    //TODO: Check slashes as in testWithLineContinuerDefine
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
        val tokens = generateTokens("""//aaa\n""")
        assertEquals(listOf("//aaa", "\n"), tokens)
    }

    //TODO: If you print this in python as "//a\\\nb" the result is newline before b, but in the example below it does not work as expected
    @Test
    fun testCppStyleCommentWithMultipleLines(){
        val tokens = generateTokens("""//a\
            |b
        """.trimMargin())
        assertEquals(listOf("""//a\\\nb"""), tokens)
    }

    @Test
    fun testCommentedComment() {
        val tokens = generateTokens(" /*/*/")
        assertEquals(listOf(" ", "/*/*/"), tokens)
    }

    @Test
    fun testWithCppComments() {
        val tokens = generateTokens("//abc\\n t")
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