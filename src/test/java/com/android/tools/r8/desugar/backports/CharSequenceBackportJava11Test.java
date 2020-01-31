// Copyright (c) 2020, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.desugar.backports;

import com.android.tools.r8.TestParameters;
import com.android.tools.r8.TestRuntime.CfVm;
import com.android.tools.r8.ToolHelper;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static com.android.tools.r8.utils.FileUtils.JAR_EXTENSION;

@RunWith(Parameterized.class)
public final class CharSequenceBackportJava11Test extends AbstractBackportTest {
  @Parameters(name = "{0}")
  public static Iterable<?> data() {
    return getTestParameters()
        .withDexRuntimes()
        .withAllApiLevels()
        .withCfRuntimesStartingFromIncluding(CfVm.JDK11)
        .build();
  }

  private static final Path TEST_JAR =
      Paths.get(ToolHelper.EXAMPLES_JAVA11_JAR_DIR).resolve("backport" + JAR_EXTENSION);

  public CharSequenceBackportJava11Test(TestParameters parameters) {
    super(parameters, Character.class, TEST_JAR, "backport.CharSequenceBackportJava11Main");
    // Note: None of the methods in this test exist in the latest android.jar. If/when they ship in
    // an actual API level, migrate these tests to CharacterBackportTest.
  }
}
