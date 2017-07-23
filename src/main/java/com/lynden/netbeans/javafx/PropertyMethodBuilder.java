/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Lynden, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.lynden.netbeans.javafx;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.TreeMaker;

/**
 *
 */
public class PropertyMethodBuilder {

    private static final String PROPERTY = "Property"; // NOI18N

    private static final Map<String, String> VALUE_TYPES = new HashMap<>();
    private static final Map<String, String> GENERIC_VALUE_TYPES = new HashMap<>();
    private static final Set<String> VALUE_TYPE_AS_PARAM = new HashSet<>();

    private static final Set<String> WRITABLE_PROPERTIES = new HashSet<>();
    private static final Map<String, String> WRAPPED_READ_ONLY_TYPES = new HashMap<>();

    static {

        /* Type of the value can be found in map. */
        VALUE_TYPES.put("javafx.beans.property.IntegerProperty", "int");
        VALUE_TYPES.put("javafx.beans.property.LongProperty", "long");
        VALUE_TYPES.put("javafx.beans.property.FloatProperty", "float");
        VALUE_TYPES.put("javafx.beans.property.DoubleProperty", "double");
        VALUE_TYPES.put("javafx.beans.property.BooleanProperty", "boolean");
        VALUE_TYPES.put("javafx.beans.property.StringProperty", "java.lang.String");
        VALUE_TYPES.put("javafx.beans.property.ReadOnlyIntegerProperty", "int");
        VALUE_TYPES.put("javafx.beans.property.ReadOnlyLongProperty", "long");
        VALUE_TYPES.put("javafx.beans.property.ReadOnlyFloatProperty", "float");
        VALUE_TYPES.put("javafx.beans.property.ReadOnlyDoubleProperty", "double");
        VALUE_TYPES.put("javafx.beans.property.ReadOnlyBooleanProperty", "boolean");
        VALUE_TYPES.put("javafx.beans.property.ReadOnlyStringProperty", "java.lang.String");
        VALUE_TYPES.put("javafx.beans.property.ReadOnlyIntegerWrapper", "int");
        VALUE_TYPES.put("javafx.beans.property.ReadOnlyLongWrapper", "long");
        VALUE_TYPES.put("javafx.beans.property.ReadOnlyFloatWrapper", "float");
        VALUE_TYPES.put("javafx.beans.property.ReadOnlyDoubleWrapper", "double");
        VALUE_TYPES.put("javafx.beans.property.ReadOnlyBooleanWrapper", "boolean");
        VALUE_TYPES.put("javafx.beans.property.ReadOnlyStringWrapper", "java.lang.String");

        /* Type of the value can be found in map, but it requires the same type
         * parameters as the property type. */
        GENERIC_VALUE_TYPES.put("javafx.beans.property.ListProperty", "javafx.collections.ObservableList");
        GENERIC_VALUE_TYPES.put("javafx.beans.property.SetProperty", "javafx.collections.ObservableSet");
        GENERIC_VALUE_TYPES.put("javafx.beans.property.MapProperty", "javafx.collections.ObservableMap");
        GENERIC_VALUE_TYPES.put("javafx.beans.property.ReadOnlyListProperty", "javafx.collections.ObservableList");
        GENERIC_VALUE_TYPES.put("javafx.beans.property.ReadOnlySetProperty", "javafx.collections.ObservableSet");
        GENERIC_VALUE_TYPES.put("javafx.beans.property.ReadOnlyMapProperty", "javafx.collections.ObservableMap");
        GENERIC_VALUE_TYPES.put("javafx.beans.property.ReadOnlyListWrapper", "javafx.collections.ObservableList");
        GENERIC_VALUE_TYPES.put("javafx.beans.property.ReadOnlySetWrapper", "javafx.collections.ObservableSet");
        GENERIC_VALUE_TYPES.put("javafx.beans.property.ReadOnlyMapWrapper", "javafx.collections.ObservableMap");

        /* Type of the value is given as a type parameter of the property type. */
        VALUE_TYPE_AS_PARAM.add("javafx.beans.property.ObjectProperty");
        VALUE_TYPE_AS_PARAM.add("javafx.beans.property.ReadOnlyObjectProperty");
        VALUE_TYPE_AS_PARAM.add("javafx.beans.property.ReadOnlyObjectWrapper");


        /* These property types are writable. */
        WRITABLE_PROPERTIES.add("javafx.beans.property.IntegerProperty");
        WRITABLE_PROPERTIES.add("javafx.beans.property.LongProperty");
        WRITABLE_PROPERTIES.add("javafx.beans.property.FloatProperty");
        WRITABLE_PROPERTIES.add("javafx.beans.property.DoubleProperty");
        WRITABLE_PROPERTIES.add("javafx.beans.property.BooleanProperty");
        WRITABLE_PROPERTIES.add("javafx.beans.property.StringProperty");
        WRITABLE_PROPERTIES.add("javafx.beans.property.ListProperty");
        WRITABLE_PROPERTIES.add("javafx.beans.property.SetProperty");
        WRITABLE_PROPERTIES.add("javafx.beans.property.MapProperty");
        WRITABLE_PROPERTIES.add("javafx.beans.property.ObjectProperty");

        /* Read-only wrapper types and their respective wrapped types. These may
         * or may not be generic. If they are, the type parameters are the same. */
        WRAPPED_READ_ONLY_TYPES.put("javafx.beans.property.ReadOnlyIntegerWrapper", "javafx.beans.property.ReadOnlyIntegerProperty");
        WRAPPED_READ_ONLY_TYPES.put("javafx.beans.property.ReadOnlyLongWrapper", "javafx.beans.property.ReadOnlyLongProperty");
        WRAPPED_READ_ONLY_TYPES.put("javafx.beans.property.ReadOnlyFloatWrapper", "javafx.beans.property.ReadOnlyFloatProperty");
        WRAPPED_READ_ONLY_TYPES.put("javafx.beans.property.ReadOnlyDoubleWrapper", "javafx.beans.property.ReadOnlyDoubleProperty");
        WRAPPED_READ_ONLY_TYPES.put("javafx.beans.property.ReadOnlyBooleanWrapper", "javafx.beans.property.ReadOnlyBooleanProperty");
        WRAPPED_READ_ONLY_TYPES.put("javafx.beans.property.ReadOnlyStringWrapper", "javafx.beans.property.ReadOnlyStringProperty");
        WRAPPED_READ_ONLY_TYPES.put("javafx.beans.property.ReadOnlyListWrapper", "javafx.beans.property.ReadOnlyListProperty");
        WRAPPED_READ_ONLY_TYPES.put("javafx.beans.property.ReadOnlySetWrapper", "javafx.beans.property.ReadOnlySetProperty");
        WRAPPED_READ_ONLY_TYPES.put("javafx.beans.property.ReadOnlyMapWrapper", "javafx.beans.property.ReadOnlyMapProperty");
        WRAPPED_READ_ONLY_TYPES.put("javafx.beans.property.ReadOnlyObjectWrapper", "javafx.beans.property.ReadOnlyObjectProperty");
    }

    private static String getValueType(String typeName) {

        String className = TypeHelper.getClassName(typeName);
        String typeParams = TypeHelper.getTypeParameters(typeName);

        if (VALUE_TYPES.containsKey(className)) {
            return VALUE_TYPES.get(className);

        } else if (GENERIC_VALUE_TYPES.containsKey(className)) {
            return GENERIC_VALUE_TYPES.get(className) + '<' + typeParams + '>';

        } else if (VALUE_TYPE_AS_PARAM.contains(className)) {
            return typeParams;

        } else {
            return "java.lang.Object";
        }
    }

    private static boolean isWritableType(String typeName) {
        return WRITABLE_PROPERTIES.contains(TypeHelper.getClassName(typeName));
    }

    private static boolean isWrapperType(String typeName) {
        return WRAPPED_READ_ONLY_TYPES.containsKey(TypeHelper.getClassName(typeName));
    }

    private static String getWrappedReadOnlyType(String typeName) {

        String className = TypeHelper.getClassName(typeName);
        String typeParams = TypeHelper.getTypeParameters(typeName);

        if (typeParams == null) {
            return WRAPPED_READ_ONLY_TYPES.get(className);
        } else {
            return WRAPPED_READ_ONLY_TYPES.get(className) + '<' + typeParams + '>';
        }
    }

    private final TreeMaker make;
    private final List<VariableElement> fields;

    public PropertyMethodBuilder(TreeMaker make, List<VariableElement> fields) {
        this.make = make;
        this.fields = fields;
    }

    List<MethodTree> createPropMethods() {

        if (fields == null) {
            return Collections.emptyList();
        }

        List<MethodTree> createdMethods = new ArrayList<>();
        for (VariableElement field : fields) {

            createdMethods.add(createGetMethod(field));

            /* Only create set method if property is writable */
            if (isWritableType(field.asType().toString())) {
                createdMethods.add(createSetMethod(field));
            }

            createdMethods.add(createPropertyMethod(field));

        }
        return createdMethods;
    }

    protected MethodTree createGetMethod(VariableElement field) {
        ModifiersTree modifiers = make.Modifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));

        String returnTypeName = getValueType(field.asType().toString());
        Tree returnType = make.Type(returnTypeName);

        String getterPrefix = ("boolean".equals(returnTypeName)) ? "is" : "get";
        String name = getGetMethodName(field.getSimpleName().toString(), getterPrefix);

        List<TypeParameterTree> typeParameters = Collections.emptyList();

        List<VariableTree> parameters = Collections.emptyList();

        List<ExpressionTree> throwsList = Collections.emptyList();

        BlockTree body = createGetMethodBody(field);

        ExpressionTree defaultValue = null;


        return make.Method(modifiers, name, returnType, typeParameters, parameters, throwsList, body, defaultValue);
    }

    protected MethodTree createSetMethod(VariableElement field) {
        ModifiersTree modifiers = make.Modifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));

        Tree returnType = make.Type("void");

        String name = getSetMethodName(field.getSimpleName().toString());

        List<TypeParameterTree> typeParameters = Collections.emptyList();

        String parameterTypeName = getValueType(field.asType().toString());
        String parameterName = "value";
        VariableTree parameter = make.Variable(make.Modifiers(Collections.emptySet()), parameterName, make.Type(parameterTypeName), null);
        List<VariableTree> parameters = Collections.singletonList(parameter);

        List<ExpressionTree> throwsList = Collections.emptyList();

        BlockTree body = createSetMethodBody(field, parameterName);

        ExpressionTree defaultValue = null;


        return make.Method(modifiers, name, returnType, typeParameters, parameters, throwsList, body, defaultValue);
    }

    protected MethodTree createPropertyMethod(VariableElement field) {
        ModifiersTree modifiers = make.Modifiers(EnumSet.of(Modifier.PUBLIC));

        String fieldTypeName = field.asType().toString();
        boolean isReadOnlyWrapper = isWrapperType(fieldTypeName);

        Tree returnType = make.Type(isReadOnlyWrapper ? getWrappedReadOnlyType(fieldTypeName) : fieldTypeName);

        String name = getPropertyMethodName(field.getSimpleName().toString());

        List<TypeParameterTree> typeParameters = Collections.emptyList();

        List<VariableTree> parameters = Collections.emptyList();

        List<ExpressionTree> throwsList = Collections.emptyList();

        BlockTree body = createPropertyMethodBody(field, isReadOnlyWrapper);

        ExpressionTree defaultValue = null;


        return make.Method(modifiers, name, returnType, typeParameters, parameters, throwsList, body, defaultValue);
    }

    protected BlockTree createGetMethodBody(VariableElement field) {
        /* return field.get(); */
        ExpressionTree method = make.MemberSelect(make.Identifier(field), "get");
        StatementTree statement = make.Return(make.MethodInvocation(Collections.emptyList(), method, Collections.emptyList()));

        return make.Block(Collections.singletonList(statement), false);
    }

    protected BlockTree createSetMethodBody(VariableElement field, String parameterName) {
        /* field.set(parameterName); */
        ExpressionTree method = make.MemberSelect(make.Identifier(field), "set");
        ExpressionTree parameter = make.Identifier(parameterName);
        StatementTree statement = make.ExpressionStatement(make.MethodInvocation(Collections.emptyList(), method, Collections.singletonList(parameter)));

        return make.Block(Collections.singletonList(statement), false);
    }

    protected BlockTree createPropertyMethodBody(VariableElement field, boolean isReadOnlyWrapper) {
        StatementTree statement;
        if (isReadOnlyWrapper) {
            /* return field.getReadOnlyProperty(); */
            ExpressionTree method = make.MemberSelect(make.Identifier(field), "getReadOnlyProperty");
            statement = make.Return(make.MethodInvocation(Collections.emptyList(), method, Collections.emptyList()));
        } else {
            /* return field; */
            statement = make.Return(make.Identifier(field));
        }

        return make.Block(Collections.singletonList(statement), false);
    }

    private static String getGetMethodName(String fieldName) {
        return getGetMethodName(fieldName, "get");
    }

    private static String getGetMethodName(String fieldName, String prefix) {
        return prefix + prepareFieldNameForMethodName(fieldName);
    }

    private static String getSetMethodName(String fieldName) {
        return "set" + prepareFieldNameForMethodName(fieldName);
    }

    private static String getPropertyMethodName(String fieldName) {
        return prepareFieldNameForMethodName(fieldName, false) + PROPERTY;
    }

    private static String prepareFieldNameForMethodName(String fieldName, boolean firstCharToUpperCase) {
        if (firstCharToUpperCase) {
            fieldName = fieldName.substring(0, 1).toUpperCase(Locale.ROOT) + fieldName.substring(1);
        }

        if (fieldName.endsWith(PROPERTY)) {
            fieldName = fieldName.substring(0, fieldName.length() - PROPERTY.length());
        }

        return fieldName;
    }

    private static String prepareFieldNameForMethodName(String fieldName) {
        return prepareFieldNameForMethodName(fieldName, true);
    }
}
