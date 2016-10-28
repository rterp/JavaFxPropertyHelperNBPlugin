/**
The MIT License (MIT)

Copyright (c) 2015 Lynden, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
**/
package com.lynden.netbeans.javafx;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import org.netbeans.api.java.source.TreeMaker;

/**
 *
 */
public class PropertyMethodBuilder {
    
    private static final String PROPERTY = "Property"; // NOI18N
    private static final Map<String,String> PRIMITIVES_MAP;

    static {
	PRIMITIVES_MAP = new HashMap<>();
	PRIMITIVES_MAP.put("Integer", "int");
	PRIMITIVES_MAP.put("Float", "float");
	PRIMITIVES_MAP.put("Double", "double");
	PRIMITIVES_MAP.put("Boolean", "boolean");
	PRIMITIVES_MAP.put("Long", "long");
    }

    private static String replaceWithPrimitive(String typeName) {
	return PRIMITIVES_MAP.getOrDefault(typeName, typeName);
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
        if( elements == null ) {
            return 0;
        }
        for (Iterator<Tree> treeIt = members.iterator(); treeIt.hasNext();) {
            Tree member = treeIt.next();

            if (member.getKind().equals(Tree.Kind.METHOD)) {
                MethodTree mt = (MethodTree) member;
                for (Element element : elements) {
                    if( mt.getName().contentEquals(getGetterName(element.getSimpleName().toString()) ) ||
			mt.getName().contentEquals(getGetterName(element.getSimpleName().toString(), "is") ) ||

                        mt.getName().contentEquals(getSetterName(element.getSimpleName().toString()) ) ||
                        mt.getName().contentEquals(getPropertyMethodName(element.getSimpleName().toString()))) {
                            
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

        if( elements == null ) {
            return;
        }
        
        int position = index - 1;
        for (VariableElement element : elements) {

            position = Math.min(position + 1, members.size());
            members.add(position, createSetMethod(element));
            position = Math.min(position + 1, members.size());
            members.add(position, createGetMethod(element));
            position = Math.min(position + 1, members.size());
            members.add(position, createPropertyMethod(element));

        }
    }

    protected MethodTree createGetMethod(VariableElement element) {
        Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC, Modifier.FINAL);
        List<AnnotationTree> annotations = new ArrayList<>();
	String typeName = replaceWithPrimitive(toStringWithoutPackages(element));
        VariableTree parameter = make.Variable(make.Modifiers(new HashSet<Modifier>(), Collections.<AnnotationTree>emptyList()), "value", make.Identifier(typeName),
                null);

        ExpressionTree returnType = make.QualIdent(parameter.getType().toString());

        final String bodyText = createPropGetterMethodBody(element);

	String setterPrefix = ("boolean".equals(typeName)) ? "is" : "get"; 

        MethodTree method = make.Method(
                make.Modifiers(modifiers, annotations),
                getGetterName(element.getSimpleName().toString(), setterPrefix),
                returnType,
                Collections.<TypeParameterTree>emptyList(),
                //Collections.<VariableTree>singletonList(parameter),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                bodyText,
                null);

        return method;

    }

    protected MethodTree createPropertyMethod(VariableElement element) {
        Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC, Modifier.FINAL);
        List<AnnotationTree> annotations = new ArrayList<>();
        VariableTree parameter = make.Variable(make.Modifiers(new HashSet<Modifier>(), Collections.<AnnotationTree>emptyList()), "value", make.Identifier(toStringWithoutPackages(element)),
                null);

        ExpressionTree returnType = make.QualIdent(parameter.getType().toString() + PROPERTY);

        final String bodyText = createPropertyMethodBody(element);

        MethodTree method = make.Method(
                make.Modifiers(modifiers, annotations),
                getPropertyMethodName(element.getSimpleName().toString()),
                returnType,
                Collections.<TypeParameterTree>emptyList(),
                //Collections.<VariableTree>singletonList(parameter),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                bodyText,
                null);

        return method;

    }

    protected MethodTree createSetMethod(VariableElement element) {
        Set<Modifier> modifiers = EnumSet.of(Modifier.PUBLIC, Modifier.FINAL);
        List<AnnotationTree> annotations = new ArrayList<>();
	String typeName = replaceWithPrimitive(toStringWithoutPackages(element));
        VariableTree parameter = make.Variable(make.Modifiers(new HashSet<Modifier>(), Collections.<AnnotationTree>emptyList()), "value", make.Identifier(typeName),
                null);

        ExpressionTree returnType = make.QualIdent("void");

        final String bodyText = createPropSetterMethodBody(element);

        MethodTree method = make.Method(
                make.Modifiers(modifiers, annotations),
                getSetterName(element.getSimpleName().toString()),
                returnType,
                Collections.<TypeParameterTree>emptyList(),
                //Collections.<VariableTree>singletonList(parameter),
                Collections.<VariableTree>singletonList(parameter),
                Collections.<ExpressionTree>emptyList(),
                bodyText,
                null);

        return method;

    }

    protected String createPropertyMethodBody(Element element) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n")
                .append("return ")
                .append(element.getSimpleName())
                .append(";\n}");
        return sb.toString();
    }

    protected String createPropGetterMethodBody(Element element) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n")
                .append("return ")
                .append(element.getSimpleName())
                .append(".get();\n}");
        return sb.toString();
    }

    protected String createPropSetterMethodBody(Element element) {
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
        sb.append(this.prepareFieldNameForMethodName(fieldName, Boolean.FALSE));
        sb.append(PROPERTY);
        
        return sb.toString();
    }

    private String getSetterName(String fieldName) {
        final StringBuilder sb = new StringBuilder();
        sb.append("set");
        sb.append(this.prepareFieldNameForMethodName(fieldName));
        
        return sb.toString();
    }

    private String getGetterName(String fieldName) {
        return getGetterName(fieldName, "get");
    }

    private String getGetterName(String fieldName, String prefix) {
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
        return this.prepareFieldNameForMethodName(fieldName, Boolean.TRUE);
    }

    private static String toStringWithoutPackages(VariableElement element) {
        String fullProp = PackageHelper.removePackagesFromGenericsType(element.asType().toString());

        return fullProp.substring(0, fullProp.indexOf(("Prop")));
    }

}
