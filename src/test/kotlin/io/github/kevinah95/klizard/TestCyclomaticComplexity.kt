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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.Test
import kotlin.test.assertEquals

class TestCyclomaticComplexity {

    @ParameterizedTest
    @CsvSource(
        delimiter = '|', quoteCharacter = '"', textBlock = """
         "no condition"    | "int fun() {}" | 1
         "one condition"    | "int fun(){if(a){xx;}}" | 2
         "ternary operator"    | "int fun(){return (a)?b:c;}" | 2
         "forever_loop"    | "int fun(){for(;;){dosomething();}}" | 2
         "and operator"    | "int fun(){if(a&&b){xx;}}" | 3
         "if-else-if"    | "int fun(){if(a)b;else if (c) d;}" | 3
         "macro if-elif"    | "int main(){
            #ifdef A
            #elif (defined E)
            #endif
         }" | 3
         "r-value reference in parameter"    | "int make(Args&&... args){}" | 1
         "r-value reference in body"    | "int f() {Args&& a=b;}" | 1
         "non r-value reference in body"    | "int f() {a && b==c;}" | 2
         "typedef with r-value reference"    | "int f() {typedef int&& rref;}" | 1
         "brace-less control structures"    | "void c() {
            if (a > -1 && b>= 0 )
                if(a != 0)
                    a = b;
         }" | 4
         "function ref qualifier"    | "struct A { void foo() && { return bar() && baz(); } };" | 2
         "function const ref qualifier"    | "struct A { void foo() const && { return bar() && baz(); } };" | 2"""
    )
    fun testCppCcn(description: String, code: String, ccn: Int) {
        val result = getCppFunctionList(code)
        assertEquals(ccn, result[0].cyclomaticComplexity, description)
    }

    @Test
    fun testCppCcnTwoFunctions(){
        val result = getCppFunctionList(
            """
                x c() {
                    if (a && b) {}
                }
                x a() {
                  inputs = c;
                }
            """.trimIndent()
        )
        assertEquals(3, result[0].cyclomaticComplexity)
        assertEquals(1, result[1].cyclomaticComplexity)
    }
}