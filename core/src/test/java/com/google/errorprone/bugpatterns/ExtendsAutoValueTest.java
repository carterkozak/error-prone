/*
 * Copyright 2019 The Error Prone Authors.
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

package com.google.errorprone.bugpatterns;

import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.util.RuntimeVersion;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link ExtendsAutoValue}. */
@RunWith(JUnit4.class)
public class ExtendsAutoValueTest {

  private final CompilationTestHelper helper =
      CompilationTestHelper.newInstance(ExtendsAutoValue.class, getClass());

  @Test
  public void extendsAutoValue_GoodNoSuperclass() throws Exception {
    helper.addSourceLines("TestClass.java", "public class TestClass {}").doTest();
  }

  @Test
  public void extendsAutoValue_GoodSuperclass() throws Exception {
    helper
        .addSourceLines(
            "TestClass.java", "class SuperClass {}", "public class TestClass extends SuperClass {}")
        .doTest();
  }

  @Test
  public void extendsAutoValue_GoodAutoValueExtendsSuperclass() throws Exception {
    helper
        .addSourceLines(
            "TestClass.java",
            "import com.google.auto.value.AutoValue;",
            "public class TestClass {}",
            "@AutoValue class AutoClass extends TestClass {}")
        .doTest();
  }

  @Test
  public void extendsAutoValue_GoodGeneratedIgnored() throws Exception {
    helper
        .addSourceLines(
            "TestClass.java",
            "import com.google.auto.value.AutoValue;",
            (RuntimeVersion.isAtLeast9()
                ? "import javax.annotation.processing.Generated;"
                : "import javax.annotation.Generated;"),
            "@AutoValue class AutoClass {}",
            "@Generated(value=\"hi\") public class TestClass extends AutoClass {}")
        .doTest();
  }

  @Test
  public void extendsAutoValue_Bad() throws Exception {
    helper
        .addSourceLines(
            "TestClass.java",
            "import com.google.auto.value.AutoValue;",
            "@AutoValue class AutoClass {}",
            "// BUG: Diagnostic contains: ExtendsAutoValue",
            "public class TestClass extends AutoClass {}")
        .doTest();
  }

  @Test
  public void extendsAutoValue_BadNoImport() throws Exception {
    helper
        .addSourceLines(
            "TestClass.java",
            "@com.google.auto.value.AutoValue class AutoClass {}",
            "// BUG: Diagnostic contains: ExtendsAutoValue",
            "public class TestClass extends AutoClass {}")
        .doTest();
  }

  @Test
  public void extendsAutoValue_BadInnerClass() throws Exception {
    helper
        .addSourceLines(
            "OuterClass.java",
            "import com.google.auto.value.AutoValue;",
            "public class OuterClass { ",
            "  @AutoValue class AutoClass {}",
            "  // BUG: Diagnostic contains: ExtendsAutoValue",
            "  class TestClass extends AutoClass {}",
            "}")
        .doTest();
  }

  @Test
  public void extendsAutoValue_BadInnerStaticClass() throws Exception {
    helper
        .addSourceLines(
            "TestClass.java",
            "import com.google.auto.value.AutoValue;",
            "class OuterClass { ",
            "  @AutoValue static class AutoClass {}",
            "}",
            "// BUG: Diagnostic contains: ExtendsAutoValue",
            "public class TestClass extends OuterClass.AutoClass {}")
        .doTest();
  }

  @Test
  public void extendsAutoValue_BadButSuppressed() throws Exception {
    helper
        .addSourceLines(
            "TestClass.java",
            "import com.google.auto.value.AutoValue;",
            "@AutoValue class AutoClass {}",
            "@SuppressWarnings(\"ExtendsAutoValue\")",
            "public class TestClass extends AutoClass {}")
        .doTest();
  }
}
