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

import io.github.kevinah95.getCppFunctionList
import kotlin.test.Test
import kotlin.test.assertEquals

class TestCCppLizard {
    @Test
    fun testEmpty() {
        val result = getCppFunctionList("")
        assertEquals(0, result.size)
    }

    @Test
    fun testNoFunction() {
        val result = getCppFunctionList("#include <stdio.h>\n")
        assertEquals(0, result.size)
    }

    @Test
    fun testOneFunction() {
        val result = getCppFunctionList("int fun(){}")
        assertEquals(1, result.size)
        assertEquals("fun", result[0].name)
    }

    @Test
    fun testTwoFunction() {
        val result = getCppFunctionList("int fun(){}\nint fun1(){}\n")
        assertEquals(2, result.size)
        assertEquals("fun", result[0].name)
        assertEquals("fun1", result[1].name)
        assertEquals(1, result[0].startLine)
        assertEquals(1, result[0].endLine)
        assertEquals(2, result[1].startLine)
        assertEquals(2, result[1].endLine)
    }

    @Test
    fun testTwoSimplestFunction() {
        val result = getCppFunctionList("f(){}g(){}")
        assertEquals(2, result.size)
        assertEquals("f", result[0].name)
        assertEquals("g", result[1].name)
    }

    @Test
    fun testFunctionWithContent() {
        val result = getCppFunctionList("int fun(xx oo){int a; a= call(p1,p2);}")
        assertEquals(1, result.size)
        assertEquals("fun", result[0].name)
        assertEquals("fun( xx oo)", result[0].longName)
    }

    @Test
    fun testOldStyleCFunction() {
        val result = getCppFunctionList("int fun(param) int praram; {}")
        assertEquals(1, result.size)
    }

    @Test
    fun testNotOldStyleCFunction() {
        val result = getCppFunctionList("m()".repeat(1500) + "a(){}")
        assertEquals(1, result.size)
    }

    @Test
    fun testComplicatedCFunction() {
        val result = getCppFunctionList("int f(int(*)()){}")
        assertEquals("f", result[0].name)
    }

    @Test
    fun testFunctionDecWithThrow() {
        val result = getCppFunctionList("int fun() throw();void foo(){}")
        assertEquals(1, result.size)
    }

    @Test
    fun testFunctionDecWithNoexcept() {
        val result = getCppFunctionList("int fun() noexcept(true);void foo(){}")
        assertEquals(1, result.size)
    }

    @Test
    fun testFunctionDecFollowedWithOneWordIsOk() {
        val result = getCppFunctionList("int fun() no_throw {}")
        assertEquals(1, result.size)
    }

    @Test
    fun testFunctionDeclarationIsNotCounted() {
        val result = getCppFunctionList("int fun();class A{};")
        assertEquals(0, result.size)
    }

    @Test
    fun testOldStyleCFunctionHasSemicolon() {
        val result = getCppFunctionList("{(void*)a}{}")
        assertEquals(0, result.size)
    }

    @Test
    fun testTypedefIsNotOldStyleCFunction() {
        val result = getCppFunctionList("typedef T() nT; foo(){}")
        assertEquals("foo", result[0].name)
    }

    @Test
    fun testStupidMacroBeforeFunction() {
        val result = getCppFunctionList("T() foo(){}")
        assertEquals("foo", result[0].name)
    }

    @Test
    fun testOnlyWordCanBeFunctionName() {
        val result = getCppFunctionList("[(){}")
        assertEquals(0, result.size)
    }

    @Test
    fun testDoubleSlashWithinString() {
        val result = getCppFunctionList("""int fun(){char *a="\\\\";}""")
        assertEquals(1, result.size)
    }

    @Test
    fun testNumberWithThousandsSeperatorSinceCpp14() {
        val sourceCode = """int fun(){
                                int a= 100'000; if(b) c; return 123'456'789;
                            }""".trimIndent()
        val result = getCppFunctionList(sourceCode)
        assertEquals(1, result.size)
        assertEquals(2, result[0].cyclomaticComplexity)
    }

    @Test
    fun testFunctionWithNoParam() {
        val result = getCppFunctionList("int fun(){}")
        assertEquals(0, result[0].parameterCount)
    }

    @Test
    fun testFunctionWith1Param() {
        val result = getCppFunctionList("int fun(aa bb){}")
        assertEquals(1, result[0].parameterCount)
        assertEquals(listOf("bb"), result[0].parameters)
    }

    @Test
    fun testFunctionWith1RefParam() {
        val result = getCppFunctionList("int fun(aa * bb){}")
        assertEquals(1, result[0].parameterCount)
        assertEquals(listOf("bb"), result[0].parameters)
    }

    @Test
    fun testFunctionWithParam() {
        val result = getCppFunctionList("int fun(aa * bb, cc dd){}")
        assertEquals(2, result[0].parameterCount)
    }

    @Test
    fun testFunctionWithStrangParam() {
        val result = getCppFunctionList("int fun(aa<mm, nn> bb){}")
        assertEquals(1, result[0].parameterCount)
        assertEquals("fun( aa<mm,nn> bb)", result[0].longName)
    }

    @Test
    fun testFunctionWithStrangParam2() {
        val result = getCppFunctionList("int fun(aa<x<mm,(x, y)>, nn> bb, (cc)<xx, oo> d){}")
        assertEquals(2, result[0].parameterCount)
    }

    @Test
    fun testOneFunctionWithNamespace() {
        val result = getCppFunctionList("int abc::fun(){}")
        assertEquals(1, result.size)
        assertEquals("abc::fun", result[0].name)
        assertEquals("abc::fun()", result[0].longName)
    }

    @Test
    fun testOneFunctionWithConst() {
        val result = getCppFunctionList("int abc::fun()const{}")
        assertEquals(1, result.size)
        assertEquals("abc::fun", result[0].name)
        assertEquals("abc::fun() const", result[0].longName)
    }

    @Test
    fun testOneFunctionWithThrow() {
        var result = getCppFunctionList("int fun() throw() {}")
        assertEquals(1, result.size)
        assertEquals("fun", result[0].name)
        result = getCppFunctionList("int fun() throw(Exception) {}")
        assertEquals(1, result.size)
        assertEquals("fun", result[0].name)
    }

    @Test
    fun testOneFunctionWithNoexcept() {
        var result = getCppFunctionList("int abc::fun()noexcept{}")
        assertEquals(1, result.size)
        assertEquals("abc::fun", result[0].name)
        result = getCppFunctionList("int fun() noexcept(true) {}")
        assertEquals(1, result.size)
        assertEquals("fun", result[0].name)
        result = getCppFunctionList("int fun() noexcept(noexcept(foo()) && noexcept(Bar())) {}")
        assertEquals(1, result.size)
        assertEquals("fun", result[0].name)
    }

    @Test
    fun testTwoFunctionsInClass() {
        var result = getCppFunctionList("class c {~c(){}}; int d(){}")
        assertEquals(2, result.size)
        assertEquals("c::~c", result[0].name)
        assertEquals("d", result[1].name)
    }

    @Test
    fun testOneMacroInClass() {
        var result = getCppFunctionList("class c {M()}; int d(){}")
        assertEquals(1, result.size)
        assertEquals("d", result[0].name)
    }

    @Test
    fun testPreClass() {
        var result = getCppFunctionList("class c; int d(){}")
        assertEquals(1, result.size)
        assertEquals("d", result[0].name)
    }

    @Test
    fun testClassWithInheritance() {
        var result = getCppFunctionList("class c final:public b {int f(){}};")
        assertEquals(1, result.size)
        assertEquals("c::f", result[0].name)
    }

    @Test
    fun testNestedClass() {
        var result = getCppFunctionList("class c {class d {int f(){}};};")
        assertEquals(1, result.size)
        assertEquals("c::d::f", result[0].name)
    }

    @Test
    fun testTemplateClass() {
        var result = getCppFunctionList("template<typename T> class c {};")
        assertEquals(0, result.size)
        result = getCppFunctionList("template<class T> class c {};")
        assertEquals(0, result.size)
        result = getCppFunctionList("template<typename T> class c {" +
            "void f(T t) {}};")
        assertEquals(1, result.size)
        assertEquals("c::f", result[0].name)
        result = getCppFunctionList("template<class T, typename S>" +
                "class c {void f(T t) {}};")
        assertEquals(1, result.size)
        assertEquals("c::f", result[0].name)
        result = getCppFunctionList("namespace ns { template<class T>" +
                "class c {void f(T t) {}}; }")
        assertEquals(1, result.size)
        assertEquals("ns::c::f", result[0].name)
    }
}