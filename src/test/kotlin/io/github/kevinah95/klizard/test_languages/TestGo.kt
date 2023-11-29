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

package io.github.kevinah95.klizard.test_languages

import io.github.kevinah95.klizard.FunctionInfo
import io.github.kevinah95.klizard.KLizard
import kotlin.test.Test
import kotlin.test.assertEquals


fun getGoFunctionList(sourceCode: String): MutableList<FunctionInfo> {
    return KLizard().analyzeFile.analyzeSourceCode("a.go", sourceCode).functionList
}



class TestGo {

    @Test
    fun testEmpty() {
        val result = getGoFunctionList("")
        assertEquals(0, result.size)
    }

    @Test
    fun testNoFunction() {
        val result = getGoFunctionList(
            """
            for name, ok := range names; ok {
                print("Hello, \(name)!")
            }
            """.trimIndent()
        )
        assertEquals(0, result.size)
    }

    @Test
    fun testOneFunction() {
        val result = getGoFunctionList(
            """
            func sayGoodbye() { }
            """.trimIndent()
        )

        assertEquals(1, result.size)
        assertEquals("sayGoodbye", result[0].name)
        assertEquals(0, result[0].parameterCount)
        assertEquals(1, result[0].cyclomaticComplexity)
    }

    @Test
    fun testOneWithParameter() {
        val result = getGoFunctionList(
            """
            func sayGoodbye(personName string, alreadyGreeted chan bool) { }
            """.trimIndent()
        )

        assertEquals(1, result.size)
        assertEquals("sayGoodbye", result[0].name)
        assertEquals(2, result[0].parameterCount)
    }

    @Test
    fun testOneFunctionWithReturnValue() {
        val result = getGoFunctionList(
            """
            func sayGoodbye() string { }
            """.trimIndent()
        )

        assertEquals(1, result.size)
        assertEquals("sayGoodbye", result[0].name)
    }

    @Test
    fun testOneFunctionWithTwoReturnValues() {
        val result = getGoFunctionList(
            """
            func sayGoodbye(p int) (string, error) { }
            """.trimIndent()
        )

        assertEquals(1, result.size)
        assertEquals("sayGoodbye", result[0].name)
        assertEquals(1, result[0].parameterCount)
    }

    @Test
    fun testOneFunctionDefinedOnAStruct() {
        val result = getGoFunctionList(
            """
            func (s Stru) sayGoodbye(){ }
            """.trimIndent()
        )

        assertEquals(1, result.size)
        assertEquals("sayGoodbye", result[0].name)
        assertEquals("(s Stru)sayGoodbye", result[0].longName)
    }

    @Test
    fun testOneFunctionWithComplexity() {
        val result = getGoFunctionList(
            """
            func sayGoodbye() { if ++diceRoll == 7 { diceRoll = 1 }}
            """.trimIndent()
        )

        assertEquals(2, result[0].cyclomaticComplexity)
    }

    @Test
    fun testOneFunctionWithReturnEmptyInterface() {
        val result = getGoFunctionList(
            """
            func sayGoodbye() interface{} {
                if ++diceRoll == 7 { diceRoll = 1 }
            }
            """.trimIndent()
        )

        assertEquals(1, result.size)
        assertEquals("sayGoodbye", result[0].name)
        assertEquals(3, result[0].length)
    }

    @Test
    fun testNestFunction() {
        val result = getGoFunctionList(
            """
            func sayGoodbye() {
                f1 := func() {}
                f2 := func(n int) {}
                f3 := func() int {
                    return 0
                }
            }
            """.trimIndent()
        )

        assertEquals(4, result.size)

        assertEquals("", result[0].name)
        assertEquals("", result[0].longName)
        assertEquals(1, result[0].length)

        assertEquals("", result[1].name)
        assertEquals(" n int", result[1].longName)
        assertEquals(1, result[1].length)
        assertEquals(listOf("n int"), result[1].fullParameters)

        assertEquals("", result[2].name)
        assertEquals("", result[2].longName)
        assertEquals(3, result[2].length)

        assertEquals("sayGoodbye", result[3].name)
        assertEquals(7, result[3].length)
    }

    @Test
    fun testInterface() {
        val result = getGoFunctionList(
            """
            type geometry interface{
					 area()  float64
					 perim()  float64
			 }
            func sayGoodbye() { }
            """.trimIndent()
        )

        assertEquals(1, result.size)
        assertEquals("sayGoodbye", result[0].name)
    }

    @Test
    fun testInterfaceFollowedByAClass() {
        val result = getGoFunctionList(
            """
            type geometry interface{
					 area()  float64
					 perim()  float64
			 }
            class c { }
            """.trimIndent()
        )

        assertEquals(0, result.size)
    }
}