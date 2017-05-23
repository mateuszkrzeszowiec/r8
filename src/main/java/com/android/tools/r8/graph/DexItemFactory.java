// Copyright (c) 2016, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.
package com.android.tools.r8.graph;

import com.android.tools.r8.dex.Constants;
import com.android.tools.r8.graph.DexMethodHandle.MethodHandleType;
import com.android.tools.r8.naming.NamingLens;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class DexItemFactory {

  private final Map<DexString, DexString> strings = new HashMap<>();
  private final Map<DexType, DexType> types = new HashMap<>();
  private final Map<DexField, DexField> fields = new HashMap<>();
  private final Map<DexProto, DexProto> protos = new HashMap<>();
  private final Map<DexMethod, DexMethod> methods = new HashMap<>();
  private final Map<DexCallSite, DexCallSite> callSites = new HashMap<>();
  private final Map<DexMethodHandle, DexMethodHandle> methodHandles = new HashMap<>();

  boolean sorted = false;

  public static final DexType catchAllType = new DexType(new DexString("CATCH_ALL"));
  private static final Set<DexItem> internalSentinels = ImmutableSet.of(catchAllType);

  public DexString booleanDescriptor = createString("Z");
  public DexString byteDescriptor = createString("B");
  public DexString charDescriptor = createString("C");
  public DexString doubleDescriptor = createString("D");
  public DexString floatDescriptor = createString("F");
  public DexString intDescriptor = createString("I");
  public DexString longDescriptor = createString("J");
  public DexString shortDescriptor = createString("S");
  public DexString voidDescriptor = createString("V");

  public DexString boxedBooleanDescriptor = createString("Ljava/lang/Boolean;");
  public DexString boxedByteDescriptor = createString("Ljava/lang/Byte;");
  public DexString boxedCharDescriptor = createString("Ljava/lang/Character;");
  public DexString boxedDoubleDescriptor = createString("Ljava/lang/Double;");
  public DexString boxedFloatDescriptor = createString("Ljava/lang/Float;");
  public DexString boxedIntDescriptor = createString("Ljava/lang/Integer;");
  public DexString boxedLongDescriptor = createString("Ljava/lang/Long;");
  public DexString boxedShortDescriptor = createString("Ljava/lang/Short;");
  public DexString boxedNumberDescriptor = createString("Ljava/lang/Number;");

  public DexString unboxBooleanMethodName = createString("booleanValue");
  public DexString unboxByteMethodName = createString("byteValue");
  public DexString unboxCharMethodName = createString("charValue");
  public DexString unboxShortMethodName = createString("shortValue");
  public DexString unboxIntMethodName = createString("intValue");
  public DexString unboxLongMethodName = createString("longValue");
  public DexString unboxFloatMethodName = createString("floatValue");
  public DexString unboxDoubleMethodName = createString("doubleValue");

  public DexString valueOfMethodName = createString("valueOf");

  public DexString getClassMethodName = createString("getClass");

  public DexString stringDescriptor = createString("Ljava/lang/String;");
  public DexString objectDescriptor = createString("Ljava/lang/Object;");
  public DexString classDescriptor = createString("Ljava/lang/Class;");
  public DexString objectsDescriptor = createString("Ljava/util/Objects;");

  public DexString constructorMethodName = createString(Constants.INSTANCE_INITIALIZER_NAME);
  public DexString classConstructorMethodName = createString(Constants.CLASS_INITIALIZER_NAME);

  public DexString thisName = createString("this");

  private DexString charArrayDescriptor = createString("[C");

  public DexType booleanType = createType(booleanDescriptor);
  public DexType byteType = createType(byteDescriptor);
  public DexType charType = createType(charDescriptor);
  public DexType doubleType = createType(doubleDescriptor);
  public DexType floatType = createType(floatDescriptor);
  public DexType intType = createType(intDescriptor);
  public DexType longType = createType(longDescriptor);
  public DexType shortType = createType(shortDescriptor);
  public DexType voidType = createType(voidDescriptor);

  public DexType boxedBooleanType = createType(boxedBooleanDescriptor);
  public DexType boxedByteType = createType(boxedByteDescriptor);
  public DexType boxedCharType = createType(boxedCharDescriptor);
  public DexType boxedDoubleType = createType(boxedDoubleDescriptor);
  public DexType boxedFloatType = createType(boxedFloatDescriptor);
  public DexType boxedIntType = createType(boxedIntDescriptor);
  public DexType boxedLongType = createType(boxedLongDescriptor);
  public DexType boxedShortType = createType(boxedShortDescriptor);
  public DexType boxedNumberType = createType(boxedNumberDescriptor);

  public DexType stringType = createType(stringDescriptor);
  public DexType objectType = createType(objectDescriptor);

  public StringBuildingMethods stringBuilderMethods =
      new StringBuildingMethods(createString("Ljava/lang/StringBuilder;"));
  public StringBuildingMethods stringBufferMethods =
      new StringBuildingMethods(createString("Ljava/lang/StringBuffer;"));
  public ObjectsMethods objectsMethods = new ObjectsMethods();
  public ObjectMethods objectMethods = new ObjectMethods();
  public LongMethods longMethods = new LongMethods();

  public void clearSubtypeInformation() {
    types.values().forEach(DexType::clearSubtypeInformation);
  }

  public class LongMethods {
    public DexMethod compare;

    private LongMethods() {
      compare = createMethod(boxedLongDescriptor,
          createString("compare"), intDescriptor, new DexString[]{longDescriptor, longDescriptor});
    }
  }

  public class ObjectMethods {
    public DexMethod getClass;

    private ObjectMethods() {
      getClass = createMethod(objectsDescriptor,
          getClassMethodName, classDescriptor, new DexString[]{});
    }
  }

  public class ObjectsMethods {
    public DexMethod requireNonNull;

    private ObjectsMethods() {
      requireNonNull = createMethod(objectsDescriptor,
          createString("requireNonNull"), objectDescriptor, new DexString[]{objectDescriptor});
    }
  }

  public class StringBuildingMethods {
    public DexMethod appendBoolean;
    public DexMethod appendChar;
    public DexMethod appendCharArray;
    public DexMethod appendSubCharArray;
    public DexMethod appendCharSequence;
    public DexMethod appendSubCharSequence;
    public DexMethod appendInt;
    public DexMethod appendDouble;
    public DexMethod appendFloat;
    public DexMethod appendLong;
    public DexMethod appendObject;
    public DexMethod appendString;
    public DexMethod appendStringBuffer;
    public DexMethod toString;

    private StringBuildingMethods(DexString receiver) {
      DexString sbuf = createString("Ljava/lang/StringBuffer;");
      DexString charSequence = createString("Ljava/lang/CharSequence;");
      DexString append = createString("append");
      DexString toStringMethodName = createString("toString");

      appendBoolean = createMethod(receiver, append, receiver, new DexString[]{booleanDescriptor});
      appendChar = createMethod(receiver, append, receiver, new DexString[]{charDescriptor});
      appendCharArray = createMethod(receiver, append, receiver, new DexString[]{
          charArrayDescriptor});
      appendSubCharArray = createMethod(receiver, append, receiver,
          new DexString[]{charArrayDescriptor, intDescriptor, intDescriptor});
      appendCharSequence = createMethod(receiver, append, receiver,
          new DexString[]{charSequence});
      appendSubCharSequence = createMethod(receiver, append, receiver,
          new DexString[]{charSequence, intDescriptor, intDescriptor});
      appendInt = createMethod(receiver, append, receiver, new DexString[]{intDescriptor});
      appendDouble = createMethod(receiver, append, receiver, new DexString[]{doubleDescriptor});
      appendFloat = createMethod(receiver, append, receiver, new DexString[]{floatDescriptor});
      appendLong = createMethod(receiver, append, receiver, new DexString[]{longDescriptor});
      appendObject = createMethod(receiver, append, receiver, new DexString[]{objectDescriptor});
      appendString = createMethod(receiver, append, receiver, new DexString[]{stringDescriptor});
      appendStringBuffer = createMethod(receiver, append, receiver, new DexString[]{sbuf});
      toString = createMethod(receiver, toStringMethodName, stringDescriptor,
          DexString.EMPTY_ARRAY);
    }

    public void forEeachAppendMethod(Consumer<DexMethod> consumer) {
      consumer.accept(appendBoolean);
      consumer.accept(appendChar);
      consumer.accept(appendCharArray);
      consumer.accept(appendSubCharArray);
      consumer.accept(appendCharSequence);
      consumer.accept(appendSubCharSequence);
      consumer.accept(appendInt);
      consumer.accept(appendDouble);
      consumer.accept(appendFloat);
      consumer.accept(appendLong);
      consumer.accept(appendObject);
      consumer.accept(appendString);
      consumer.accept(appendStringBuffer);
      consumer.accept(appendBoolean);
    }
  }

  synchronized private static <T extends DexItem> T canonicalize(Map<T, T> map, T item) {
    assert item != null;
    assert !internalSentinels.contains(item);
    T previous = map.putIfAbsent(item, item);
    return previous == null ? item : previous;
  }

  public DexString createString(int size, byte[] content) {
    assert !sorted;
    DexString string = new DexString(size, content);
    return canonicalize(strings, string);
  }

  public DexString createString(String source) {
    assert !sorted;
    DexString string = new DexString(source);
    return canonicalize(strings, string);
  }

  public DexString lookupString(String source) {
    return strings.get(new DexString(source));
  }

  public DexType createType(DexString descriptor) {
    assert !sorted;
    DexType type = new DexType(descriptor);
    return canonicalize(types, type);
  }

  public DexType createType(String descriptor) {
    return createType(createString(descriptor));
  }

  public DexType lookupType(String descriptor) {
    DexString string = lookupString(descriptor);
    if (string != null) {
      return types.get(new DexType(string));
    }
    return null;
  }

  public DexField createField(DexType clazz, DexType type, DexString name) {
    assert !sorted;
    DexField field = new DexField(clazz, type, name);
    return canonicalize(fields, field);
  }

  public DexProto createProto(DexString shorty, DexType type, DexTypeList parameters) {
    assert !sorted;
    DexProto proto = new DexProto(shorty, type, parameters);
    return canonicalize(protos, proto);
  }

  public DexProto createProto(DexString shorty, DexType type, DexType[] parameters) {
    assert !sorted;
    return createProto(shorty, type,
        parameters.length == 0 ? DexTypeList.empty() : new DexTypeList(parameters));
  }

  public DexProto createProto(DexType type, DexType[] parameters) {
    return createProto(createShorty(type, parameters), type, parameters);
  }

  public DexString createShorty(DexType returnType, DexType[] argumentTypes) {
    StringBuilder shortyBuilder = new StringBuilder();
    shortyBuilder.append(returnType.toShorty());
    for (DexType argumentType : argumentTypes) {
      shortyBuilder.append(argumentType.toShorty());
    }
    return createString(shortyBuilder.toString());
  }

  public DexMethod createMethod(DexType holder, DexProto proto, DexString name) {
    assert !sorted;
    DexMethod method = new DexMethod(holder, proto, name);
    return canonicalize(methods, method);
  }

  public DexMethodHandle createMethodHandle(
      MethodHandleType type, Descriptor<? extends DexItem, ? extends Descriptor> fieldOrMethod) {
    assert !sorted;
    DexMethodHandle methodHandle = new DexMethodHandle(type, fieldOrMethod);
    return canonicalize(methodHandles, methodHandle);
  }

  public DexCallSite createCallSite(
      DexString methodName, DexProto methodProto,
      DexMethodHandle bootstrapMethod, List<DexValue> bootstrapArgs) {
    assert !sorted;
    DexCallSite callSite = new DexCallSite(methodName, methodProto, bootstrapMethod, bootstrapArgs);
    return canonicalize(callSites, callSite);
  }

  public DexMethod createMethod(DexString clazzDescriptor, DexString name,
      DexString returnTypeDescriptor,
      DexString[] parameterDescriptors) {
    assert !sorted;
    DexType clazz = createType(clazzDescriptor);
    DexType returnType = createType(returnTypeDescriptor);
    DexType[] parameterTypes = new DexType[parameterDescriptors.length];
    for (int i = 0; i < parameterDescriptors.length; i++) {
      parameterTypes[i] = createType(parameterDescriptors[i]);
    }
    DexProto proto = createProto(shorty(returnType, parameterTypes), returnType, parameterTypes);

    return createMethod(clazz, proto, name);
  }

  public boolean isConstructor(DexMethod method) {
    return method.name == constructorMethodName;
  }

  public boolean isClassConstructor(DexMethod method) {
    return method.name == classConstructorMethodName;
  }

  private DexString shorty(DexType returnType, DexType[] parameters) {
    StringBuilder builder = new StringBuilder();
    builder.append(returnType.toDescriptorString().charAt(0));
    for (DexType parameter : parameters) {
      String descriptor = parameter.toDescriptorString();
      if (descriptor.charAt(0) == '[') {
        builder.append('L');
      } else {
        builder.append(descriptor.charAt(0));
      }
    }
    return createString(builder.toString());
  }

  private static <S extends PresortedComparable<S>> void assignSortedIndices(Collection<S> items,
      NamingLens namingLens) {
    List<S> sorted = new ArrayList<>(items);
    sorted.sort((a, b) -> a.layeredCompareTo(b, namingLens));
    int i = 0;
    for (S value : sorted) {
      value.setSortedIndex(i++);
    }
  }

  synchronized public void sort(NamingLens namingLens) {
    assert !sorted;
    assignSortedIndices(strings.values(), namingLens);
    assignSortedIndices(types.values(), namingLens);
    assignSortedIndices(fields.values(), namingLens);
    assignSortedIndices(protos.values(), namingLens);
    assignSortedIndices(methods.values(), namingLens);
    sorted = true;
  }

  synchronized public void resetSortedIndices() {
    if (!sorted) {
      return;
    }
    // Only used for asserting that we don't use the sorted index after we build the graph.
    strings.values().forEach(IndexedDexItem::resetSortedIndex);
    types.values().forEach(IndexedDexItem::resetSortedIndex);
    fields.values().forEach(IndexedDexItem::resetSortedIndex);
    protos.values().forEach(IndexedDexItem::resetSortedIndex);
    methods.values().forEach(IndexedDexItem::resetSortedIndex);
    sorted = false;
  }

  synchronized public void forAllTypes(Consumer<DexType> f) {
    new ArrayList<>(types.values()).forEach(f);
  }
}
