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

import io.github.kevinah95.FileInformation
import io.github.kevinah95.FunctionInfo
import io.github.kevinah95.KLizard
import kotlin.test.Test
import kotlin.test.assertEquals


fun getJavaFileinfo(sourceCode: String): FileInformation {
    return KLizard().analyzeFile.analyzeSourceCode("a.java", sourceCode)
}

fun getJavaFunctionList(sourceCode: String): MutableList<FunctionInfo> {
    return getJavaFileinfo(sourceCode).functionList
}

class TestJava {

    @Test
    fun testFunctionWithThrows() {
        val result = getJavaFunctionList("void fun() throws e1, e2{}")
        assertEquals(1, result.size)
    }

    @Test
    fun testFunctionWithDecorator() {
        val result = getJavaFunctionList("@abc() void fun() throws e1, e2{}")
        assertEquals(1, result.size)
    }

    @Test
    fun testClassWithDecorator() {
        val result = getJavaFunctionList("@abc() class funxx{ }")
        assertEquals(0, result.size)
    }

    @Test
    fun testClassWithDecoratorThatHasNamespace() {
        val result = getJavaFunctionList("@a.b() class funxx{ }")
        assertEquals(0, result.size)
    }

    @Test
    fun testClassNameWithExtends() {
        val result = getJavaFunctionList("class A extends B { void f(){}}")
        assertEquals("A::f", result[0].name)
    }

    @Test
    fun testClassNameWithInterface() {
        val result = getJavaFunctionList("class A implements B { void f(){}}")
        assertEquals("A::f", result[0].name)
    }

    /**
     * it turns out you can overload the operator keyword
     */
    @Test
    fun testOperatorAsAnOverloadedIdentifier() {
        val result = getJavaFunctionList(
            """
                package operator; class A { void f(){}}
            """.trimIndent()
        )
        assertEquals("A::f", result[0].name)
    }

    @Test
    fun testAbstractFunctionWithoutBodyFollowingMethod() {
        val result = getJavaFunctionList("abstract void fun(); void fun1(){}")
        assertEquals("fun1", result[0].name)
        assertEquals(1, result.size)
    }

    @Test
    fun testAbstractFunctionWithoutBodyWithThrowsFollowingMethod() {
        val result = getJavaFunctionList("abstract void fun() throws e; void fun2(){}")
        assertEquals("fun2", result[0].name)
        assertEquals(1, result.size)
    }

    @Test
    fun testGenericTypeWithExtends() {
        val result = getJavaFunctionList("class B<T extends C> {void fun(T t) {}}")
        // actual "B<T::fun"
        assertEquals("B::fun", result[0].name)
    }

    @Test
    fun testGenericTypeWithQuestionMark() {
        val result = getJavaFunctionList("void A(){ List<? extends x> list;}}")
        assertEquals(1, result.size)
        assertEquals("A", result[0].name)
        assertEquals(1, result[0].cyclomaticComplexity)
    }
}