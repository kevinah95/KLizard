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

import io.github.kevinah95.klizard.getCppFunctionList
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
        result = getCppFunctionList(
            "template<typename T> class c {" +
                    "void f(T t) {}};"
        )
        assertEquals(1, result.size)
        assertEquals("c::f", result[0].name)
        result = getCppFunctionList(
            "template<class T, typename S>" +
                    "class c {void f(T t) {}};"
        )
        assertEquals(1, result.size)
        assertEquals("c::f", result[0].name)
        result = getCppFunctionList(
            "namespace ns { template<class T>" +
                    "class c {void f(T t) {}}; }"
        )
        assertEquals(1, result.size)
        assertEquals("ns::c::f", result[0].name)
    }

    @Test
    fun testTemplateClassFullSpecialization() {
        var result = getCppFunctionList("template<> class c<double> {};")
        assertEquals(0, result.size)
        result = getCppFunctionList(
            "template<> class c<double> {" +
                    "void f() {}};"
        )
        assertEquals(1, result.size)
        assertEquals("c::f", result[0].name)
        result = getCppFunctionList(
            "template<>" +
                    "class c<double, int> {void f() {}};"
        )
        assertEquals(1, result.size)
        assertEquals("c::f", result[0].name)
        result = getCppFunctionList(
            "namespace ns { template<>" +
                    "class c<double> {void f() {}}; }"
        )
        assertEquals(1, result.size)
        assertEquals("ns::c::f", result[0].name)
    }

    @Test
    fun testTemplateClassPartialSpecialization() {
        var result = getCppFunctionList("template<typename T> class c<int,T> {};")
        assertEquals(0, result.size)
        result = getCppFunctionList("template<class T> class c<int,T> {};")
        assertEquals(0, result.size)
        result = getCppFunctionList(
            "template<typename T> class c<int,T> {" +
                    "void f(T t) {}};"
        )
        assertEquals(1, result.size)
        assertEquals("c::f", result[0].name)
        result = getCppFunctionList(
            "template<class T> class c<int,T> {" +
                    "void f(T t) {}};"
        )
        assertEquals(1, result.size)
        assertEquals("c::f", result[0].name)
        result = getCppFunctionList(
            "template<class T, typename S>" +
                    "class c<int,T,S> {void f(T t) {}};"
        )
        assertEquals(1, result.size)
        assertEquals("c::f", result[0].name)
        result = getCppFunctionList(
            "namespace ns { template<class T>" +
                    "class c<int,T> {void f(T t) {}}; }"
        )
        assertEquals(1, result.size)
        assertEquals("ns::c::f", result[0].name)
    }

    @Test
    fun testTemplateFunction() {
        var result = getCppFunctionList("template<typename T> void f(T t) {}")
        assertEquals(1, result.size)
        assertEquals("f", result[0].name)
        result = getCppFunctionList("template<class T> void f(T t) {}")
        assertEquals(1, result.size)
        assertEquals("f", result[0].name)
        result = getCppFunctionList(
            "namespace ns {" +
                    "template<class T> void f(T t) {}}"
        )
        assertEquals(1, result.size)
        assertEquals("ns::f", result[0].name)
    }

    @Test
    fun testTemplateFunctionSpecialization() {
        var result = getCppFunctionList("template<> void f<double>() {}")
        assertEquals(1, result.size)
        assertEquals("f<double>", result[0].name)
        result = getCppFunctionList(
            "namespace ns {" +
                    "template<> void f<double>() {}}"
        )
        assertEquals(1, result.size)
        assertEquals("ns::f<double>", result[0].name)
    }

    @Test
    fun testNestedTemplateFunction() {
        var result = getCppFunctionList(
            "template<typename T> class c { " +
                    "template<typename S> void f() {} };"
        )
        assertEquals(1, result.size)
        assertEquals("c::f", result[0].name)
        result = getCppFunctionList(
            "template<class T> class c { " +
                    "template<class S> void f() {} };"
        )
        assertEquals(1, result.size)
        assertEquals("c::f", result[0].name)
        result = getCppFunctionList(
            "namespace ns { " +
                    "template<class T> class c { " +
                    "template<class S> void f() {} }; }"
        )
        assertEquals(1, result.size)
        assertEquals("ns::c::f", result[0].name)
    }

    @Test
    fun testTemplatedCodeWithQuestionMark() {
        val result = getCppFunctionList(
            "void a(){Class<?>[];}"
        )
        assertEquals(1, result[0].cyclomaticComplexity)
    }

    @Test
    fun testClassAsAnAttribute() {
        val result = getCppFunctionList(
            "void a(){{String.class}}"
        )
        assertEquals(1, result[0].cyclomaticComplexity)
    }

    @Test
    fun test1() {
        val result = getCppFunctionList(
            "class c {{}}"
        )
        assertEquals(0, result.size)
    }

    @Test
    fun testBracketThatIsNotANamespace() {
        val result = getCppFunctionList(
            "class c { {};int f(){}};"
        )
        assertEquals(1, result.size)
        assertEquals("c::f", result[0].name)
    }

    @Test
    fun testNestedClassMiddle() {
        val result = getCppFunctionList(
            "class c {class d {};int f(){}};"
        )
        assertEquals(1, result.size)
        assertEquals("c::f", result[0].name)
    }

    @Test
    fun testTemplateAsReference() {
        val result = getCppFunctionList(
            "abc::def(a<b>& c){}"
        )
        assertEquals(1, result.size)
    }

    @Test
    fun testLessThenIsNotTemplate() {
        val result = getCppFunctionList(
            "def(<); foo(){}"
        )
        assertEquals(1, result.size)
    }

    @Test
    fun testTemplateWithPointer() {
        val result = getCppFunctionList(
            "abc::def (a<b*> c){}"
        )
        assertEquals(1, result.size)
    }

    @Test
    fun testNestedTemplate() {
        val result = getCppFunctionList(
            "abc::def (a<b<c>> c){}"
        )
        assertEquals(1, result.size)
    }

    @Test
    fun testDoubleNestedTemplate() {
        val result = getCppFunctionList(
            "abc::def (a<b<c<d>>> c){}"
        )
        assertEquals(1, result.size)
    }

    @Test
    fun testTemplateWithReference() {
        val result = getCppFunctionList(
            "void fun(t<int &>b){} "
        )
        assertEquals(1, result.size)
    }

    @Test
    fun testTemplateWithReferenceAsReference() {
        val result = getCppFunctionList(
            "void fun(t<const int&>&b){} "
        )
        assertEquals(1, result.size)
    }

    @Test
    fun testTemplateAsPartOfFunctionName() {
        val result = getCppFunctionList(
            "void fun<a,b<c>>(){} "
        )
        assertEquals("fun<a,b<c>>", result[0].name)
    }


    @Test
    fun testOperatorOverloading() {
        val result = getCppFunctionList(
            "bool operator +=(int b){}"
        )
        assertEquals("operator +=", result[0].name)
    }

    @Test
    fun testOperatorOverloadingShift() {
        val result = getCppFunctionList(
            "bool operator <<(int b){}"
        )
        assertEquals("operator < <", result[0].name)
    }

    @Test
    fun testOperatorWithComplicatedName() {
        val result = getCppFunctionList(
            "operator MyStruct&(){}"
        )
        assertEquals("operator MyStruct &", result[0].name)
    }

    @Test
    fun testOperatorOverloadingWithNamespace() {
        val result = getCppFunctionList(
            "bool TC::operator !(int b){}"
        )
        assertEquals(1, result.size)
        assertEquals("TC::operator !", result[0].name)
    }

    @Test
    fun testFunctionOperator() {
        val result = getCppFunctionList(
            "bool TC::operator ()(int b){}"
        )
        assertEquals(1, result.size)
        assertEquals("TC::operator ( )", result[0].name)
    }

    @Test
    fun testInlineOperator() {
        val result = getCppFunctionList(
            "class A { bool operator ()(int b) {} };"
        )
        assertEquals(1, result.size)
        assertEquals("A::operator ( )", result[0].name)
    }

    @Test
    fun testNamespaceAlias() {
        val result = getCppFunctionList(
            "namespace p;" +
                    "namespace real { bool foo() {} }"
        )
        assertEquals(1, result.size)
        assertEquals("real::foo", result[0].name)
    }

    @Test
    fun testNestedUnnamedNamespace() {
        val result = getCppFunctionList(
            "namespace real { namespace { bool foo() {} } }"
        )
        assertEquals(1, result.size)
        assertEquals("real::foo", result[0].name)
    }

    @Test
    fun testConstructorInitializationList() {
        val result = getCppFunctionList(
            "A::A():a(1){}"
        )
        assertEquals(1, result.size)
        assertEquals("A::A", result[0].name)
    }

    @Test
    fun testConstructorInitializationListNoexcept() {
        val result = getCppFunctionList(
            "A::A()noexcept:a(1){}"
        )
        assertEquals(1, result.size)
        assertEquals("A::A", result[0].name)
    }

    @Test
    fun testConstructorInitializerList() {
        val result = getCppFunctionList(
            "A::A():a({1}),value(true){}"
        )
        assertEquals(1, result.size)
        assertEquals("A::A", result[0].name)
    }

    @Test
    fun testConstructorUniformInitialization() {
        val result = getCppFunctionList(
            "A::A():a{1}{}"
        )
        assertEquals(1, result.size)
        assertEquals("A::A", result[0].name)
    }

    @Test
    fun testParenthesesBeforeFunction() {
        val result = getCppFunctionList(
            "()"
        )
        assertEquals(0, result.size)
    }

    @Test
    fun testDestructorImplementation() {
        val result = getCppFunctionList(
            "A::~A(){}"
        )
        assertEquals(1, result.size)
        assertEquals("A::~A", result[0].name)
    }

    @Test
    fun testFunctionThatReturnsFunctionPointers() {
        val result = getCppFunctionList(
            "int (*fun())(){}"
        )
        assertEquals(1, result.size)
        assertEquals("int( * fun())", result[0].name)
    }

    @Test
    fun testStructInReturnType() {
        val result = getCppFunctionList(
            " struct a b() { a(1>2); }"
        )
        assertEquals(1, result.size)
        assertEquals("b", result[0].name)
    }

    @Test
    fun testFunctionNameClass() {
        val result = getCppFunctionList(
            "int class(){};"
        )
        assertEquals(1, result.size)
        assertEquals("class", result[0].name)
    }

    @Test
    fun testUnderscore() {
        val result = getCppFunctionList(
            " a() _() { }"
        )
        assertEquals(1, result.size)
    }

    @Test
    fun testGlobalVarConstructor() {
        var result = getCppFunctionList(
            "std::string s(\"String\");"
        )
        assertEquals(0, result.size)
        result = getCppFunctionList(
            "std::string s = \"String\";"
        )
        assertEquals(0, result.size)
    }

    @Test
    fun testNonFunctionInitializerList() {
        var result = getCppFunctionList(
            "v={}"
        )
        assertEquals(0, result.size)
        result = getCppFunctionList(
            "v = {};"
        )
        assertEquals(0, result.size)
        result = getCppFunctionList(
            "std::vector<int> v = {1, 2, 3};"
        )
        assertEquals(0, result.size)
        result = getCppFunctionList(
            "v = {1, 2, 3};"
        )
        assertEquals(0, result.size)
        result = getCppFunctionList(
            "namespace n { v = {}; }"
        )
        assertEquals(0, result.size)
        result = getCppFunctionList(
            "class n { int v = {0}; }"
        )
        assertEquals(0, result.size)
    }

    @Test
    fun testNonFunctionUniformInitialization() {
        var result = getCppFunctionList(
            "std::vector<int> v{1, 2, 3};"
        )
        assertEquals(0, result.size)
        result = getCppFunctionList(
            "std::vector<int> v{};"
        )
        assertEquals(0, result.size)
        result = getCppFunctionList(
            "namespace n { int v{0}; }"
        )
        assertEquals(0, result.size)
        result = getCppFunctionList(
            "class n { int v{0}; }"
        )
        assertEquals(0, result.size)
    }

    @Test
    fun testStructInParam() {
        val result = getCppFunctionList(
            "int fun(struct a){}"
        )
        assertEquals(1, result.size)
    }

    @Test
    fun testTrailingReturnType() {
        var result = getCppFunctionList(
            "auto foo() -> void {}"
        )
        assertEquals(1, result.size)
        assertEquals("foo", result[0].name)
        result = getCppFunctionList(
            "auto foo(int a) -> decltype(a) {}"
        )
        assertEquals(1, result.size)
        assertEquals("foo", result[0].name)
    }

    @Test
    fun testRefQualifiers() {
        var result = getCppFunctionList(
            "struct A { void foo() & {} };"
        )
        assertEquals(1, result.size)
        assertEquals("A::foo", result[0].name)
        result = getCppFunctionList(
            "struct A { void foo() const & {} };"
        )
        assertEquals(1, result.size)
        assertEquals("A::foo", result[0].name)
        result = getCppFunctionList(
            "struct A { void foo() && {} };"
        )
        assertEquals(1, result.size)
        assertEquals("A::foo", result[0].name)
        result = getCppFunctionList(
            "struct A { void foo() const && {} };"
        )
        assertEquals(1, result.size)
        assertEquals("A::foo", result[0].name)
    }

    @Test
    fun testUnionAsQualifier() {
        val result = getCppFunctionList(
            "union A { void foo() {} };"
        )
        assertEquals(1, result.size)
        assertEquals("A::foo", result[0].name)
    }
}

class TestPreprocessing {
    @Test
    fun testContentMacroShouldBeIgnored() {
        val result = getCppFunctionList(
            """

                    #define MTP_CHEC                    \
                       int foo () {                     \
                        }
               
                       """
        )
        assertEquals(0, result.size)
    }

    @Test
    fun testPreprocessorsShouldBeIgnoredOutsideFunctionImplementation() {
        val result = getCppFunctionList(
            """
              #ifdef MAGIC
              #endif
              void foo()
              {}""".trimIndent()
        )
        assertEquals(1, result.size)
    }

    @Test
    fun testPreprocessorIsNotFunction() {
        val result = getCppFunctionList(
            """
              #ifdef A
              #elif (defined E)
              #endif""".trimIndent()
        )
        assertEquals(0, result.size)
    }

    @Test
    fun testBodyWithFunctionLike() {
        val result = getCppFunctionList("int a() { xws (a) if(){} }")
        assertEquals(1, result.size)
        assertEquals("a", result[0].name)
    }

    @Test
    fun testBodyWithMacroCallAfterIf() {
        val result = getCppFunctionList("int a() { if (a) b(){} }")
        assertEquals(1, result.size)
        assertEquals("a", result[0].name)
    }

    @Test
    fun testBodyWithMacroCallAfterIfAndNoSemicolonBeforeTheClosingBr() {
        val result = getCppFunctionList("int a() { if (a) b() } int c(){}")
        assertEquals(2, result.size)
        assertEquals("c", result[1].name)
    }

    @Test
    fun testBodyWithMacroCallAfterIfAndNoSemicolonBeforeTheClosingBr2() {
        val result = getCppFunctionList("int a() { if (a) if(x) b() } int c(){}")
        assertEquals(2, result.size)
        assertEquals("c", result[1].name)
    }

    @Test
    fun testBodyWithMacroAndClass() {
        val result = getCppFunctionList("class A{int a() { if (a) if(x) b() } int c(){}}")
        assertEquals(2, result.size)
        assertEquals("A::c", result[1].name)
    }

    @Test
    fun testBodyWithFunctionLike2() {
        val result = getCppFunctionList(
            """
                void myFunction()
                {
                  IGNORE_FLAGS("w-maybe")
                  if(2+2==4)
                  END_IGNORE_FLAGS("w-maybe")
                  {
                    mySecondFunction()
                  }
                }

                int mySecondFunction()
                {
                  return 2;
                }
            """.trimIndent()
        )
        assertEquals(2, result.size)
        assertEquals("mySecondFunction", result[1].name)
    }
}

class Test_Big {
    @Test
    fun testTrouble() {
        val result = getCppFunctionList("foo<y () >> 5> r;")
        assertEquals(0, result.size)
    }

    @Test
    fun testTypedef() {
        val code = """
        typedef struct tagAAA
        {
        }AAA;

        int func_a(int size)
        {
            if(ccc && eee)
            {
                return 1;
            }
        }
        """.trimIndent()
        val result = getCppFunctionList(code)
        assertEquals(1, result.size)
        assertEquals(3, result[0].cyclomaticComplexity)
    }
}

class Test_Dialects {
    @Test
    fun testCudaKernelLaunch() {
        var code = """
            void foo() {
                kernel <<< gridDim, blockDim, 0 >>> (d_data, height, width);
            }
        """.trimIndent()
        var result = getCppFunctionList(code)
        assertEquals(1, result.size)
        assertEquals("foo", result[0].name)
        assertEquals(1, result[0].cyclomaticComplexity)
        code = """
            void foo() {
                kernel <<< gridDim, blockDim, (bar ? 0 : 1) >>> (x, y, z);
            }
        """.trimIndent()
        result = getCppFunctionList(code)
        assertEquals(1, result.size)
        assertEquals(2, result[0].cyclomaticComplexity)
        code = """
            void foo() {
                kernel <<< gridDim, blockDim, 0 >>> (x, y, (bar ? w : z));
            }
        """.trimIndent()
        result = getCppFunctionList(code)
        assertEquals(1, result.size)
        assertEquals(2, result[0].cyclomaticComplexity)
    }


}