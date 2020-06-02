// Copyright (c) 2020, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.naming.retrace;

import static com.android.tools.r8.naming.retrace.StackTrace.isSame;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import com.android.tools.r8.CompilationMode;
import com.android.tools.r8.R8TestRunResult;
import com.android.tools.r8.TestBase;
import com.android.tools.r8.TestParameters;
import com.android.tools.r8.TestParametersCollection;
import com.android.tools.r8.references.Reference;
import com.android.tools.r8.shaking.ProguardKeepAttributes;
import com.android.tools.r8.utils.codeinspector.Matchers;
import com.android.tools.r8.utils.codeinspector.Matchers.LinePosition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@RunWith(Parameterized.class)
public class LineNumberRangeTest extends TestBase {

  private final TestParameters parameters;

  private final String classDescriptor = "Lcom/android/tools/r8/naming/retrace/Main;";

  private final LinePosition EXPECTED_STACK_TRACE =
      LinePosition.stack(
          LinePosition.create(
              Reference.methodFromDescriptor(classDescriptor, "method3", "()V"),
              88,
              88,
              "LineNumberRangeTest.java"),
          LinePosition.create(
              Reference.methodFromDescriptor(classDescriptor, "method2", "()V"),
              94,
              94,
              "LineNumberRangeTest.java"),
          LinePosition.create(
              Reference.methodFromDescriptor(classDescriptor, "method1", "()V"),
              102,
              102,
              "LineNumberRangeTest.java"),
          LinePosition.create(
              Reference.methodFromDescriptor(classDescriptor, "main", "()V"),
              108,
              108,
              "LineNumberRangeTest.java"));

  @Parameters(name = "{0}")
  public static TestParametersCollection data() {
    return getTestParameters().withAllRuntimesAndApiLevels().build();
  }

  public LineNumberRangeTest(TestParameters parameters) {
    this.parameters = parameters;
  }

  @Test
  public void testSourceFileAndLineNumberTable() throws Exception {
    StackTrace expectedStackTrace =
        testForJvm()
            .addProgramClassFileData(MainDump.dump())
            .run(parameters.getRuntime(), Main.class)
            .assertFailure()
            .map(StackTrace::extractFromJvm);
    assertThat(expectedStackTrace, Matchers.containsLinePositions(EXPECTED_STACK_TRACE));
    R8TestRunResult result =
        testForR8(parameters.getBackend())
            .addProgramClassFileData(MainDump.dump())
            .setMode(CompilationMode.DEBUG)
            .addKeepMainRule(Main.class)
            .addKeepAttributes(
                ProguardKeepAttributes.SOURCE_FILE, ProguardKeepAttributes.LINE_NUMBER_TABLE)
            .setMinApi(parameters.getApiLevel())
            .addOptionsModification(
                options -> {
                  options.enableInlining = false;
                })
            .run(parameters.getRuntime(), Main.class)
            .assertFailure();
    // Extract actual stack trace and retraced stack trace from failed run result.
    StackTrace actualStackTrace;
    if (parameters.isCfRuntime()) {
      actualStackTrace = StackTrace.extractFromJvm(result.getStdErr());
    } else {
      actualStackTrace =
          StackTrace.extractFromArt(result.getStdErr(), parameters.getRuntime().asDex().getVm());
    }
    StackTrace retracedStackTrace = actualStackTrace.retrace(result.proguardMap());
    assertThat(retracedStackTrace, not(isSame(expectedStackTrace)));
  }

  // This class is generated by taking the output of InliningRetraceTest without running the
  // line number optimizer.
  public static class MainDump implements Opcodes {

    public static byte[] dump() {

      ClassWriter classWriter = new ClassWriter(0);
      MethodVisitor methodVisitor;

      classWriter.visit(
          V1_8,
          ACC_SUPER,
          "com/android/tools/r8/naming/retrace/Main",
          null,
          "java/lang/Object",
          null);

      classWriter.visitSource("LineNumberRangeTest.java", null);

      {
        methodVisitor = classWriter.visitMethod(0, "<init>", "()V", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(82, label0);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();
      }
      {
        methodVisitor =
            classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "method3", "()V", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(86, label0);
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitLdcInsn("In method3");
        methodVisitor.visitMethodInsn(
            INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLineNumber(87, label1);
        methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
        methodVisitor.visitInsn(LCONST_0);
        methodVisitor.visitInsn(LCMP);
        Label label2 = new Label();
        methodVisitor.visitJumpInsn(IFLE, label2);
        Label label3 = new Label();
        methodVisitor.visitLabel(label3);
        methodVisitor.visitLineNumber(88, label3);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitInsn(ATHROW);
        methodVisitor.visitLabel(label2);
        methodVisitor.visitLineNumber(90, label2);
        methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(4, 2);
        methodVisitor.visitEnd();
      }
      {
        methodVisitor =
            classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "method2", "(I)V", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(92, label0);
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitLdcInsn("In method2");
        methodVisitor.visitMethodInsn(
            INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLineNumber(93, label1);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitVarInsn(ISTORE, 1);
        Label label2 = new Label();
        methodVisitor.visitLabel(label2);
        methodVisitor.visitVarInsn(ILOAD, 1);
        methodVisitor.visitIntInsn(BIPUSH, 10);
        Label label3 = new Label();
        methodVisitor.visitJumpInsn(IF_ICMPLT, label3);
        Label label4 = new Label();
        methodVisitor.visitLabel(label4);
        methodVisitor.visitLineNumber(96, label4);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitLabel(label3);
        methodVisitor.visitLineNumber(94, label3);
        methodVisitor.visitFrame(Opcodes.F_CHOP, 1, null, 0, null);
        methodVisitor.visitMethodInsn(
            INVOKESTATIC, "com/android/tools/r8/naming/retrace/Main", "method3", "()V", false);
        Label label5 = new Label();
        methodVisitor.visitLabel(label5);
        methodVisitor.visitLineNumber(93, label5);
        methodVisitor.visitInsn(ACONST_NULL);
        methodVisitor.visitInsn(ATHROW);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
      }
      {
        methodVisitor =
            classWriter.visitMethod(
                ACC_PUBLIC | ACC_STATIC, "method1", "(Ljava/lang/String;)V", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(100, label0);
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitLdcInsn("In method1");
        methodVisitor.visitMethodInsn(
            INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLineNumber(101, label1);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitVarInsn(ISTORE, 1);
        Label label2 = new Label();
        methodVisitor.visitLabel(label2);
        methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[] {Opcodes.INTEGER}, 0, null);
        methodVisitor.visitVarInsn(ILOAD, 1);
        methodVisitor.visitIntInsn(BIPUSH, 10);
        Label label3 = new Label();
        methodVisitor.visitJumpInsn(IF_ICMPGE, label3);
        Label label4 = new Label();
        methodVisitor.visitLabel(label4);
        methodVisitor.visitLineNumber(102, label4);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(
            INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
        methodVisitor.visitMethodInsn(
            INVOKESTATIC, "com/android/tools/r8/naming/retrace/Main", "method2", "(I)V", false);
        Label label5 = new Label();
        methodVisitor.visitLabel(label5);
        methodVisitor.visitLineNumber(101, label5);
        methodVisitor.visitIincInsn(1, 1);
        methodVisitor.visitJumpInsn(GOTO, label2);
        methodVisitor.visitLabel(label3);
        methodVisitor.visitLineNumber(104, label3);
        methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
      }
      {
        methodVisitor =
            classWriter.visitMethod(
                ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        methodVisitor.visitCode();
        Label label0 = new Label();
        methodVisitor.visitLabel(label0);
        methodVisitor.visitLineNumber(107, label0);
        methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        methodVisitor.visitLdcInsn("In main");
        methodVisitor.visitMethodInsn(
            INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        Label label1 = new Label();
        methodVisitor.visitLabel(label1);
        methodVisitor.visitLineNumber(108, label1);
        methodVisitor.visitLdcInsn("1");
        methodVisitor.visitMethodInsn(
            INVOKESTATIC,
            "com/android/tools/r8/naming/retrace/Main",
            "method1",
            "(Ljava/lang/String;)V",
            false);
        Label label2 = new Label();
        methodVisitor.visitLabel(label2);
        methodVisitor.visitLineNumber(109, label2);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(2, 1);
        methodVisitor.visitEnd();
      }
      classWriter.visitEnd();

      return classWriter.toByteArray();
    }
  }
}
