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
        //TODO: checkTokens("""'\\''""", """'\\''""")
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
}