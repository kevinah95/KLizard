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

package io.github.kevinah95

import io.github.kevinah95.lizard_ext.LizardMcCabe
import kotlin.test.Test
import kotlin.test.assertEquals

class TestFunctionExitCount {
    @Test
    fun testNormalCase() {
        val sourceCode = """int fun(){
                                switch(x) {
                                    case 1: break;
                                    case 2: break;
                                    case 3: break;
                                };
                            }""".trimIndent()
        val result = getCppFunctionListWithExtension(sourceCode, LizardMcCabe())
        assertEquals(4, result[0].cyclomaticComplexity)
    }

    @Test
    fun testFallThrough(){
        val sourceCode = """int fun(){
                                switch(x) {
                                    case 1:
                                    case 2: break;
                                    case 3: break;
                                };
                            }""".trimIndent()
        val result = getCppFunctionListWithExtension(sourceCode, LizardMcCabe())

        assertEquals(3, result[0].cyclomaticComplexity)
    }

    @Test
    fun testMore(){
        val sourceCode = """int fun(){
                                switch(x) {
                                    case 1:
                                    case 2:
                                    case 3: break;
                                };
                            }""".trimIndent()
        val result = getCppFunctionListWithExtension(sourceCode, LizardMcCabe())

        assertEquals(2, result[0].cyclomaticComplexity)
    }
}