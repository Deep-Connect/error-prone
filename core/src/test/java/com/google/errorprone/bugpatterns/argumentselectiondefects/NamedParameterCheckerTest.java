/*
 * Copyright 2017 Google Inc. All Rights Reserved.
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

package com.google.errorprone.bugpatterns.argumentselectiondefects;

import com.google.errorprone.CompilationTestHelper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@code NamedParameterChecker} */
@RunWith(JUnit4.class)
public class NamedParameterCheckerTest {

  private CompilationTestHelper compilationHelper;

  @Before
  public void setUp() {
    compilationHelper = CompilationTestHelper.newInstance(NamedParameterChecker.class, getClass());
  }

  @Test
  public void namedParametersChecker_ignoresCall_withNoComments() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "abstract class Test {",
            "  abstract void target(Object param1, Object param2);",
            "  void test(Object arg1, Object arg2) {",
            "    target(arg1, arg2);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void namedParametersChecker_findsError_withOneBadComment() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "abstract class Test {",
            "  abstract void target(Object param1, Object param2);",
            "  void test(Object arg1, Object arg2) {",
            "    // BUG: Diagnostic contains: target(arg2, /* param2= */arg1)",
            "    target(/* param2= */arg1, arg2);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void namedParametersChecker_suggestsSwap_withSwappedArgs() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "abstract class Test {",
            "  abstract void target(Object param1, Object param2);",
            "  void test(Object arg1, Object arg2) {",
            "    // BUG: Diagnostic contains: target(/* param1= */arg1, /* param2= */arg2)",
            "    target(/* param2= */arg2, /* param1= */arg1);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void namedParametersChecker_suggestsSwap_withOneCommentedSwappedArgs() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "abstract class Test {",
            "  abstract void target(Object param1, Object param2);",
            "  void test(Object arg1, Object arg2) {",
            "    // BUG: Diagnostic contains: target(arg1, /* param2= */arg2)",
            "    target(/* param2= */arg2, arg1);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void namedParametersChecker_toleratesApproximateComment_onRequiredNamesMethod() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "abstract class Test {",
            "  abstract void target(Object param);",
            "  void test(Object arg) {",
            "    target(/*note param = */arg);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void namedParametersChecker_tolerateComment_withNoEquals() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "abstract class Test {",
            "  abstract void target(Object param);",
            "  void test(Object arg) {",
            "    target(/*param*/arg);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void namedParametersChecker_toleratesMatchingComment_blockAfter() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "abstract class Test {",
            "  abstract void target(Object param);",
            "  void test(Object arg) {",
            "    target(arg/*param*/);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void namedParametersChecker_toleratesApproximateComment_blockAfter() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "abstract class Test {",
            "  abstract void target(Object param);",
            "  void test(Object arg) {",
            "    target(arg/*imprecise match for param*/);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void namedParametersChecker_toleratesMatchingComment_lineAfter() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "abstract class Test {",
            "  abstract void target(Object param);",
            "  void test(Object arg) {",
            "    target(arg); //param",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void namedParametersChecker_ignoresComment_nonMatchinglineAfter() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "abstract class Test {",
            "  abstract void target(Object param);",
            "  void test(Object arg) {",
            "    target(arg); // some_other_comment",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void namedParametersChecker_ignoresComment_markedUpDelimiter() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "abstract class Test {",
            "  abstract void target(Object param1, Object param2);",
            "  void test(Object arg1, Object arg2) {",
            "    target(arg1,",
            "    /* ---- param1 <-> param2 ---- */",
            "           arg2);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void namedParametersChecker_ignoresComment_wrongNameWithNoEquals() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "abstract class Test {",
            "  abstract void target(Object param);",
            "  void test(Object arg) {",
            "    target(/* some_other_comment */arg);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void namedParametersChecker_matchesComment_withChainedMethod() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "abstract class Test {",
            "  abstract Test getTest(Object param);",
            "  abstract void target(Object param2);",
            "  void test(Object arg, Object arg2) {",
            "    getTest(/* param= */arg).target(arg2);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void namedParametersChecker_suggestsChangeComment_whenNoMatchingNames() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "abstract class Test {",
            "  abstract void target(Object param1, Object param2);",
            "  void test(Object arg1, Object arg2) {",
            "    // BUG: Diagnostic contains:",
            "    // target(/* param1= */arg1, arg2)",
            "    // `/* notMatching= */` does not match formal parameter name `param1`",
            "    target(/* notMatching= */arg1, arg2);",
            "  }",
            "}")
        .doTest();
  }

  /** A test for inner class constructor parameter names across compilation boundaries. */
  public static class InnerClassTest {
    /** An inner class. */
    public class Inner {
      public Inner(int foo, int bar) {}

      // this is a (non-static) inner class on purpose
      {
        System.err.println(InnerClassTest.this);
      }
    }
  }

  @Test
  public void innerClassNegative() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "import " + InnerClassTest.class.getCanonicalName() + ";",
            "class Test {",
            "  {",
            "    new InnerClassTest().new Inner(/* foo= */ 1, /* bar= */ 2);",
            "  }",
            "}")
        .doTest();
  }

  @Ignore // see b/64954766
  @Test
  public void innerClassPositive() {
    compilationHelper
        .addSourceLines(
            "Test.java",
            "import " + InnerClassTest.class.getCanonicalName() + ";",
            "class Test {",
            "  {",
            "    // BUG: Diagnostic contains:",
            "    new InnerClassTest().new Inner(/* bar= */ 1, /* foo= */ 2);",
            "  }",
            "}")
        .doTest();
  }
}
