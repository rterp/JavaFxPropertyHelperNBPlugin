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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

        String className = JavaFxBeanHelper.getClassName(typeName);
        String typeParams = JavaFxBeanHelper.getTypeParameters(typeName);

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
    private final List<VariableElement> elements;
    private final String className;

    public PropertyMethodBuilder(TreeMaker make,
            List<Tree> members,
            List<VariableElement> elements,
            String className) {
        this.make = make;
        this.members = members;
        this.elements = elements;
        this.className = className;
    }

    int removeExistingPropMethods(int index) {
        int counter = 0;
        if (elements == null) {
            return 0;
        }
        for (Iterator<Tree> treeIt = members.iterator(); treeIt.hasNext();) {
            Tree member = treeIt.next();

            if (member.getKind().equals(Tree.Kind.METHOD)) {
                MethodTree mt = (MethodTree) member;
                for (Element element : elements) {
                    if (mt.getName().contentEquals(getGetMethodName(element.getSimpleName().toString()))
                            || mt.getName().contentEquals(getGetMethodName(element.getSimpleName().toString(), "is"))
                            || mt.getName().contentEquals(getSetMethodName(element.getSimpleName().toString()))
                            || mt.getName().contentEquals(getPropertyMethodName(element.getSimpleName().toString()))) {

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

        if (elements == null) {
            return;
        }

        int position = index - 1;
        for (VariableElement element : elements) {

            position = Math.min(position + 1, members.size());
            members.add(position, createGetMethod(element));
            position = Math.min(position + 1, members.size());
            members.add(position, createSetMethod(element));
            position = Math.min(position + 1, members.size());
            members.add(position, createPropertyMethod(element));

        }
    }

    protected MethodTree createGetMethod(VariableElement element) {
        ModifiersTree modifiers = make.Modifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));

        String returnTypeName = getValueType(element.asType().toString());
        Tree returnType = make.Type(returnTypeName);

        String getterPrefix = ("boolean".equals(returnTypeName)) ? "is" : "get";
        String name = getGetMethodName(element.getSimpleName().toString(), getterPrefix);

        List<TypeParameterTree> typeParameters = Collections.emptyList();

        List<VariableTree> parameters = Collections.emptyList();

        List<ExpressionTree> throwsList = Collections.emptyList();

        String body = createGetMethodBody(element);

        ExpressionTree defaultValue = null;


        return make.Method(modifiers, name, returnType, typeParameters, parameters, throwsList, body, defaultValue);
    }

    protected MethodTree createPropertyMethod(VariableElement element) {
        ModifiersTree modifiers = make.Modifiers(EnumSet.of(Modifier.PUBLIC));

        Tree returnType = make.Type(element.asType().toString());

        String name = getPropertyMethodName(element.getSimpleName().toString());

        List<TypeParameterTree> typeParameters = Collections.emptyList();

        List<VariableTree> parameters = Collections.emptyList();

        List<ExpressionTree> throwsList = Collections.emptyList();

        String body = createPropertyMethodBody(element);

        ExpressionTree defaultValue = null;


        return make.Method(modifiers, name, returnType, typeParameters, parameters, throwsList, body, defaultValue);
    }

    protected MethodTree createSetMethod(VariableElement element) {
        ModifiersTree modifiers = make.Modifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));

        Tree returnType = make.Type("void");

        String name = getSetMethodName(element.getSimpleName().toString());

        List<TypeParameterTree> typeParameters = Collections.emptyList();

        String parameterTypeName = getValueType(element.asType().toString());
        VariableTree parameter = make.Variable(make.Modifiers(Collections.emptySet()), "value", make.Type(parameterTypeName), null);
        List<VariableTree> parameters = Collections.singletonList(parameter);

        List<ExpressionTree> throwsList = Collections.emptyList();

        String body = createSetMethodBody(element);

        ExpressionTree defaultValue = null;


        return make.Method(modifiers, name, returnType, typeParameters, parameters, throwsList, body, defaultValue);
    }

    protected String createPropertyMethodBody(Element element) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n")
                .append("return ")
                .append(element.getSimpleName())
                .append(";\n}");
        return sb.toString();
    }

    protected String createGetMethodBody(Element element) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n")
                .append("return ")
                .append(element.getSimpleName())
                .append(".get();\n}");
        return sb.toString();
    }

    protected String createSetMethodBody(Element element) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n")
                .append(element.getSimpleName())
                .append(".set(value);\n}");
        return sb.toString();
    }

    void addFields() {
        for (VariableElement element : elements) {
            VariableTree field
                    = make.Variable(make.Modifiers(
                            EnumSet.of(Modifier.PRIVATE),
                            Collections.<AnnotationTree>emptyList()),
                            element.getSimpleName().toString(),
                            make.Identifier(toStringWithoutPackages(element)),
                            null);

            members.add(field);
        }
    }

    private String getPropertyMethodName(String fieldName) {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.prepareFieldNameForMethodName(fieldName, false));
        sb.append(PROPERTY);

        return sb.toString();
    }

    private String getSetMethodName(String fieldName) {
        final StringBuilder sb = new StringBuilder();
        sb.append("set");
        sb.append(this.prepareFieldNameForMethodName(fieldName));

        return sb.toString();
    }

    private String getGetMethodName(String fieldName) {
        return getGetMethodName(fieldName, "get");
    }

    private String getGetMethodName(String fieldName, String prefix) {
        final StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append(this.prepareFieldNameForMethodName(fieldName));

        return sb.toString();
    }

    private String prepareFieldNameForMethodName(String fieldName, boolean firstCharToUpperCase) {
        if (firstCharToUpperCase) {
            fieldName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        }

        if (fieldName.endsWith(PROPERTY)) {
            fieldName = fieldName.substring(0, fieldName.length() - PROPERTY.length());
        }

        return fieldName;
    }

    private String prepareFieldNameForMethodName(String fieldName) {
        return this.prepareFieldNameForMethodName(fieldName, true);
    }

    private static String toStringWithoutPackages(VariableElement element) {
        String fullProp = PackageHelper.removePackagesFromGenericsType(element.asType().toString());

        return fullProp.substring(0, fullProp.indexOf(("Prop")));
    }

}
