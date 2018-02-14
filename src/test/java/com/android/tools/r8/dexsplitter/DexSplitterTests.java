// Copyright (c) 2017, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.dexsplitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.android.tools.r8.CompilationException;
import com.android.tools.r8.CompilationFailedException;
import com.android.tools.r8.D8;
import com.android.tools.r8.D8Command;
import com.android.tools.r8.OutputMode;
import com.android.tools.r8.R8;
import com.android.tools.r8.R8Command;
import com.android.tools.r8.ResourceException;
import com.android.tools.r8.ToolHelper;
import com.android.tools.r8.ToolHelper.ArtCommandBuilder;
import com.android.tools.r8.dexsplitter.DexSplitter.Options;
import com.android.tools.r8.utils.DexInspector;
import com.android.tools.r8.utils.DexInspector.ClassSubject;
import com.android.tools.r8.utils.FeatureClassMapping.FeatureMappingException;
import com.google.common.collect.ImmutableList;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DexSplitterTests {

  private static final String CLASS_DIR = ToolHelper.EXAMPLES_BUILD_DIR + "classes/dexsplitsample";
  private static final String CLASS1_CLASS = CLASS_DIR + "/Class1.class";
  private static final String CLASS2_CLASS = CLASS_DIR + "/Class2.class";
  private static final String CLASS3_CLASS = CLASS_DIR + "/Class3.class";
  private static final String CLASS3_INNER_CLASS = CLASS_DIR + "/Class3$InnerClass.class";

  @Rule public TemporaryFolder temp = ToolHelper.getTemporaryFolderForTest();

  /**
   * To test the file splitting we have 3 classes that we distribute like this: Class1 -> base
   * Class2 -> feature1 Class3 -> feature1
   *
   * <p>Class1 and Class2 works independently of each other, but Class3 extends Class1, and
   * therefore can't run without the base being loaded.
   */
  @Test
  public void splitFilesNoObfuscation()
      throws CompilationFailedException, IOException, FeatureMappingException, ResourceException,
      CompilationException, ExecutionException {
    noObfuscation(false);
    noObfuscation(true);
  }

  private void noObfuscation(boolean useOptions)
      throws IOException, CompilationFailedException, FeatureMappingException,
      ResourceException, ExecutionException, CompilationException {
    // Initial normal compile to create dex files.
    Path inputZip = temp.newFolder().toPath().resolve("input.zip");
    D8.run(
        D8Command.builder()
            .setOutput(inputZip, OutputMode.DexIndexed)
            .addProgramFiles(Paths.get(CLASS1_CLASS))
            .addProgramFiles(Paths.get(CLASS2_CLASS))
            .addProgramFiles(Paths.get(CLASS3_CLASS))
            .addProgramFiles(Paths.get(CLASS3_INNER_CLASS))
            .build());

    Path output = temp.getRoot().toPath().resolve("output");
    Path splitSpec = createSplitSpec();

    if (useOptions) {
      Options options = new Options();
      options.inputArchives.add(inputZip.toString());
      options.featureSplitMapping = splitSpec.toString();
      options.splitBaseName = output.toString();
      DexSplitter.run(options);
    } else {
      DexSplitter.main(
          new String[] {
              "--input", inputZip.toString(),
              "--output", output.toString(),
              "--feature-splits", splitSpec.toString()
          });
    }

    Path base = output.getParent().resolve("output.base.zip");
    Path feature = output.getParent().resolve("output.feature1.zip");
    validateUnobfuscatedOutput(base, feature);
  }

  private void validateUnobfuscatedOutput(Path base, Path feature) throws IOException {
    // Both classes should still work if we give all dex files to the system.
    for (String className : new String[] {"Class1", "Class2", "Class3"}) {
      ArtCommandBuilder builder = new ArtCommandBuilder();
      builder.appendClasspath(base.toString());
      builder.appendClasspath(feature.toString());
      builder.setMainClass("dexsplitsample." + className);
      String out = ToolHelper.runArt(builder);
      assertEquals(out, className + "\n");
    }
    // Individual classes should also work from the individual files.
    String className = "Class1";
    ArtCommandBuilder builder = new ArtCommandBuilder();
    builder.appendClasspath(base.toString());
    builder.setMainClass("dexsplitsample." + className);
    String out = ToolHelper.runArt(builder);
    assertEquals(out, className + "\n");

    className = "Class2";
    builder = new ArtCommandBuilder();
    builder.appendClasspath(feature.toString());
    builder.setMainClass("dexsplitsample." + className);
    out = ToolHelper.runArt(builder);
    assertEquals(out, className + "\n");

    className = "Class3";
    builder = new ArtCommandBuilder();
    builder.appendClasspath(feature.toString());
    builder.setMainClass("dexsplitsample." + className);
    try {
      ToolHelper.runArt(builder);
      assertFalse(true);
    } catch (AssertionError assertionError) {
      // We expect this to throw since base is not in the path and Class3 depends on Class1
    }
  }

  private Path createSplitSpec() throws FileNotFoundException, UnsupportedEncodingException {
    Path splitSpec = temp.getRoot().toPath().resolve("split_spec");
    try (PrintWriter out = new PrintWriter(splitSpec.toFile(), "UTF-8")) {
      out.write(
          "dexsplitsample.Class1:base\n"
              + "dexsplitsample.Class2:feature1\n"
              + "dexsplitsample.Class3:feature1");
    }
    return splitSpec;
  }

  private List<String> getProguardConf() {
    return ImmutableList.of(
        "-keep class dexsplitsample.Class3 {",
        "  public static void main(java.lang.String[]);",
        "}");
  }

  @Test
  public void splitFilesFromJar()
      throws IOException, CompilationFailedException, FeatureMappingException, ResourceException,
      CompilationException, ExecutionException {
    splitFromJars(true);
    splitFromJars(false);
  }

  private void splitFromJars(boolean useOptions)
      throws IOException, CompilationFailedException, FeatureMappingException, ResourceException,
      ExecutionException, CompilationException {
    // Initial normal compile to create dex files.
    Path inputZip = temp.newFolder().toPath().resolve("input.zip");
    D8.run(
        D8Command.builder()
            .setOutput(inputZip, OutputMode.DexIndexed)
            .addProgramFiles(Paths.get(CLASS1_CLASS))
            .addProgramFiles(Paths.get(CLASS2_CLASS))
            .addProgramFiles(Paths.get(CLASS3_CLASS))
            .addProgramFiles(Paths.get(CLASS3_INNER_CLASS))
            .build());

    Path output = temp.getRoot().toPath().resolve("output");
    Path baseJar = temp.getRoot().toPath().resolve("base.jar");
    Path featureJar = temp.getRoot().toPath().resolve("feature1.jar");
    ZipOutputStream baseStream = new ZipOutputStream(Files.newOutputStream(baseJar));
    String name = "dexsplitsample/Class1.class";
    baseStream.putNextEntry(new ZipEntry(name));
    baseStream.write(Files.readAllBytes(Paths.get(CLASS1_CLASS)));
    baseStream.closeEntry();
    baseStream.close();

    ZipOutputStream featureStream = new ZipOutputStream(Files.newOutputStream(featureJar));
    name = "dexsplitsample/Class2.class";
    featureStream.putNextEntry(new ZipEntry(name));
    featureStream.write(Files.readAllBytes(Paths.get(CLASS2_CLASS)));
    featureStream.closeEntry();
    name = "dexsplitsample/Class3.class";
    featureStream.putNextEntry(new ZipEntry(name));
    featureStream.write(Files.readAllBytes(Paths.get(CLASS3_CLASS)));
    featureStream.closeEntry();
    name = "dexsplitsample/Class3$InnerClass.class";
    featureStream.putNextEntry(new ZipEntry(name));
    featureStream.write(Files.readAllBytes(Paths.get(CLASS3_INNER_CLASS)));
    featureStream.closeEntry();
    featureStream.close();
    if (useOptions) {
      Options options = new Options();
      options.inputArchives.add(inputZip.toString());
      options.splitBaseName = output.toString();
      options.featureJars.add(baseJar.toString());
      options.featureJars.add(featureJar.toString());
      DexSplitter.run(options);
    } else {
      DexSplitter.main(
          new String[]{
              "--input",
              inputZip.toString(),
              "--output",
              output.toString(),
              "--feature-jar",
              baseJar.toString(),
              "--feature-jar",
              featureJar.toString()
          });
    }
    Path base = output.getParent().resolve("output.base.zip");
    Path feature = output.getParent().resolve("output.feature1.zip");
    validateUnobfuscatedOutput(base, feature);
  }


  @Test
  public void splitFilesObfuscation()
      throws CompilationFailedException, IOException, ExecutionException {
    // Initial normal compile to create dex files.
    Path inputDex = temp.newFolder().toPath().resolve("input.zip");
    Path proguardMap = temp.getRoot().toPath().resolve("proguard.map");

    R8.run(
        R8Command.builder()
            .setOutput(inputDex, OutputMode.DexIndexed)
            .addProgramFiles(Paths.get(CLASS1_CLASS))
            .addProgramFiles(Paths.get(CLASS2_CLASS))
            .addProgramFiles(Paths.get(CLASS3_CLASS))
            .addProgramFiles(Paths.get(CLASS3_INNER_CLASS))
            .addLibraryFiles(ToolHelper.getDefaultAndroidJar())
            .setProguardMapOutputPath(proguardMap)
            .addProguardConfiguration(getProguardConf(), null)
            .build());

    Path outputDex = temp.getRoot().toPath().resolve("output");
    Path splitSpec = createSplitSpec();

    DexSplitter.main(
        new String[] {
          "--input", inputDex.toString(),
          "--output", outputDex.toString(),
          "--feature-splits", splitSpec.toString(),
          "--proguard-map", proguardMap.toString()
        });

    Path base = outputDex.getParent().resolve("output.base.zip");
    Path feature = outputDex.getParent().resolve("output.feature1.zip");
    String class3 = "dexsplitsample.Class3";
    // We should still be able to run the Class3 which we kept, it has a call to the obfuscated
    // class1 which is in base.
    ArtCommandBuilder builder = new ArtCommandBuilder();
    builder.appendClasspath(base.toString());
    builder.appendClasspath(feature.toString());
    builder.setMainClass(class3);
    String out = ToolHelper.runArt(builder);
    assertEquals(out, "Class3\n");

    // Class1 should not be in the feature, it should still be in base.
    builder = new ArtCommandBuilder();
    builder.appendClasspath(feature.toString());
    builder.setMainClass(class3);
    try {
      ToolHelper.runArt(builder);
      assertFalse(true);
    } catch (AssertionError assertionError) {
      // We expect this to throw since base is not in the path and Class3 depends on Class1.
    }

    // Ensure that the Class1 is actually in the correct split. Note that Class2 would have been
    // shaken away.
    DexInspector inspector = new DexInspector(base, proguardMap.toString());
    ClassSubject subject = inspector.clazz("dexsplitsample.Class1");
    assertTrue(subject.isPresent());
    assertTrue(subject.isRenamed());
  }
}
