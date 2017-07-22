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
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.lang.model.element.Element;
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

    static {
        VALUE_TYPES.put("javafx.beans.property.IntegerProperty", "int");
        VALUE_TYPES.put("javafx.beans.property.LongProperty", "long");
        VALUE_TYPES.put("javafx.beans.property.FloatProperty", "float");
        VALUE_TYPES.put("javafx.beans.property.DoubleProperty", "double");
        VALUE_TYPES.put("javafx.beans.property.BooleanProperty", "boolean");
        VALUE_TYPES.put("javafx.beans.property.StringProperty", "java.lang.String");

        GENERIC_VALUE_TYPES.put("javafx.beans.property.ListProperty", "javafx.collections.ObservableList");
        GENERIC_VALUE_TYPES.put("javafx.beans.property.SetProperty", "javafx.collections.ObservableSet");
        GENERIC_VALUE_TYPES.put("javafx.beans.property.MapProperty", "javafx.collections.ObservableMap");
    }

    private static String getValueType(String typeName) {

        String className = TypeHelper.getClassName(typeName);
        String typeParams = TypeHelper.getTypeParameters(typeName);

        if (VALUE_TYPES.containsKey(className)) {
            return VALUE_TYPES.get(className);

        } else if (GENERIC_VALUE_TYPES.containsKey(className)) {
            return GENERIC_VALUE_TYPES.get(className) + '<' + typeParams + '>';

        } else if (className.equals("javafx.beans.property.ObjectProperty")) {
            return typeParams;

        } else {
            return "java.lang.Object";
        }
    }

    private final TreeMaker make;
    private final List<Tree> members;
    private final List<VariableElement> fields;
    private final String className;

    public PropertyMethodBuilder(TreeMaker make,
            List<Tree> members,
            List<VariableElement> fields,
            String className) {
        this.make = make;
        this.members = members;
        this.fields = fields;
        this.className = className;
    }

    int removeExistingPropMethods(int index) {
        int counter = 0;
        if (fields == null) {
            return 0;
        }
        for (Iterator<Tree> treeIt = members.iterator(); treeIt.hasNext();) {
            Tree member = treeIt.next();

            if (member.getKind().equals(Tree.Kind.METHOD)) {
                MethodTree mt = (MethodTree) member;
                for (Element field : fields) {
                    if (mt.getName().contentEquals(getGetMethodName(field.getSimpleName().toString()))
                            || mt.getName().contentEquals(getGetMethodName(field.getSimpleName().toString(), "is"))
                            || mt.getName().contentEquals(getSetMethodName(field.getSimpleName().toString()))
                            || mt.getName().contentEquals(getPropertyMethodName(field.getSimpleName().toString()))) {

                        treeIt.remove();
                        if (index > counter) {
                            index--;
                        }
                        break;
                    }
                }
            }
            counter++;
        }
        return index;
    }

    void addPropMethods(int index) {

        if (fields == null) {
            return;
        }

        int position = index - 1;
        for (VariableElement field : fields) {

            position = Math.min(position + 1, members.size());
            members.add(position, createGetMethod(field));
            position = Math.min(position + 1, members.size());
            members.add(position, createSetMethod(field));
            position = Math.min(position + 1, members.size());
            members.add(position, createPropertyMethod(field));

        }
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

        Tree returnType = make.Type(field.asType().toString());

        String name = getPropertyMethodName(field.getSimpleName().toString());

        List<TypeParameterTree> typeParameters = Collections.emptyList();

        List<VariableTree> parameters = Collections.emptyList();

        List<ExpressionTree> throwsList = Collections.emptyList();

        BlockTree body = createPropertyMethodBody(field);

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

    protected BlockTree createPropertyMethodBody(VariableElement field) {
        /* return field; */
        StatementTree statement = make.Return(make.Identifier(field));

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
