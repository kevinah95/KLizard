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

import io.github.kevinah95.klizard_languages.CLikeReader
import io.github.kevinah95.klizard_languages.getReaderFor
import kotlin.test.*

class TestLanguageChooser {

    @Test
    fun testNotCaseSensitive(){
        assertIs<CLikeReader>(getReaderFor("a.Cpp"))
    }

    @Test
    @Ignore
    fun testJava(){

    }

    @Test
    @Ignore
    fun testObjectiveC() {

    }

    @Test
    fun testCCpp() {
        for (name in listOf("a.cpp", ".cxx", ".h", ".hpp")){
            assertIs<CLikeReader>(getReaderFor(name), "File name '$name' is not recognized as c/c++ file")
        }
    }

    @Test
    @Ignore
    fun testJavaScript(){

    }

    @Test
    @Ignore
    fun testScala(){

    }

    @Test
    @Ignore
    fun testGdScript(){

    }

    @Test
    fun testUnknownExtension(){
        assertNull(getReaderFor("a.unknown"))
    }

    @Test
    @Ignore
    fun testSolidity(){

    }

    @Test
    @Ignore
    fun testErlang(){

    }

}