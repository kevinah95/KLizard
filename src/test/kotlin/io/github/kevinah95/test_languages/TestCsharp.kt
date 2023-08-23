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

fun getCsharpFileinfo(sourceCode: String): FileInformation {
    return KLizard().analyzeFile.analyzeSourceCode("a.cs", sourceCode)
}

fun getCsharpFunctionList(sourceCode: String): MutableList<FunctionInfo> {
    return getCsharpFileinfo(sourceCode).functionList
}

class TestCsharp {
    @Test
    fun testFunctionWithOne() {
        val result = getCsharpFunctionList(
            """
                public void Method()
                {
                    Console.WriteLine("Hello World!");
                }
            """
        )
        assertEquals(1, result[0].cyclomaticComplexity)
    }

    @Test
    fun testFunctionWithTwo() {
        val result = getCsharpFunctionList(
            """
                void Method(bool condition)
                {
                    if (condition)
                    {
                        Console.WriteLine("Hello World!");
                    }
                }
            """
        )
        assertEquals(2, result[0].cyclomaticComplexity)
    }

    @Test
    fun testFunctionWithThree() {
        val result = getCsharpFunctionList(
            """
                public void Method(bool condition1, bool condition2)
                {
                    if (condition1 || condition2)
                    {
                        Console.WriteLine("Hello World!");
                    }
                }
            """
        )
        assertEquals(3, result[0].cyclomaticComplexity)
    }

    @Test
    fun testFunctionWithEight() {
        val result = getCsharpFunctionList(
            """
                public void Method(DayOfWeek day)
                {
    
                        switch (day)
                        {
                            case DayOfWeek.Monday:
                                Console.WriteLine("Today is Monday!");
                                break;
                            case DayOfWeek.Tuesday:
                                Console.WriteLine("Today is Tuesday!");
                                break;
                            case DayOfWeek.Wednesday:
                                Console.WriteLine("Today is Wednesday!");
                                break;
                            case DayOfWeek.Thursday:
                                Console.WriteLine("Today is Thursday!");
                                break;
                            case DayOfWeek.Friday:
                                Console.WriteLine("Today is Friday!");
                                break;
                            case DayOfWeek.Saturday:
                                Console.WriteLine("Today is Saturday!");
                                break;
                            case DayOfWeek.Sunday:
                                Console.WriteLine("Today is Sunday!");
                                break;
                        }
                    }
    
                }
            """
        )
        assertEquals(8, result[0].cyclomaticComplexity)
    }

    @Test
    fun testNullCoalescingOperator() {
        val result = getCsharpFunctionList(
            """
                public void Method()
                {
                    a ?? b;
                }
            """
        )
        assertEquals(2, result[0].cyclomaticComplexity)
    }

}