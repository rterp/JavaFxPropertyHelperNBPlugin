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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.*;
import javafx.embed.swing.JFXPanel;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

public class JavaFxBeanHelper implements CodeGenerator {

    protected JTextComponent textComponent;
    //this is needed to initialize the JavaFx Toolkit
    protected JFXPanel panel = new JFXPanel();
    protected List<VariableElement> fields;

    public JavaFxBeanHelper textComponent(final JTextComponent value) {
        this.textComponent = value;
        return this;
    }

    public JavaFxBeanHelper fields(final List<VariableElement> value) {
        this.fields = value;
        return this;
    }

    /**
     *
     * @param context containing JTextComponent and possibly other items
     * registered by {@link CodeGeneratorContextProvider}
     */
    private JavaFxBeanHelper(Lookup context) { // Good practice is not to save Lookup outside ctor
        textComponent = context.lookup(JTextComponent.class);
        CompilationController controller = context.lookup(CompilationController.class);
        try {
            fields = getFields(context, controller);
        } catch (CodeGeneratorException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @MimeRegistration(mimeType = "text/x-java", position = 250, service = CodeGenerator.Factory.class)
    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            return Collections.singletonList(new JavaFxBeanHelper(context));
        }
    }

    /**
     * The name which will be inserted inside Insert Code dialog
     */
    @Override
    public String getDisplayName() {
        return "Java FX Getter and Setter...";
    }

    /**
     * This will be invoked when user chooses this Generator from Insert Code
     * dialog
     */
    @Override
    public void invoke() {
        Document doc = textComponent.getDocument();
        JavaSource javaSource = JavaSource.forDocument(doc);

        CancellableTask<WorkingCopy> task = new CodeGeneratorCancellableTask(textComponent) {
            @Override
            public void generateCode(WorkingCopy workingCopy, TreePath path, int position) {
                JavaFxBeanHelper.this.generateCode(workingCopy, path, position, JavaFxBeanHelper.this.fields);
            }
        };

        try {
            ModificationResult result = javaSource.runModificationTask(task);
            result.commit();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    protected void generateCode(WorkingCopy wc, TreePath path, int position, List<VariableElement> fields) {

        TypeElement typeClassElement = (TypeElement) wc.getTrees().getElement(path);
        if (typeClassElement != null) {
            int index = position;

            TreeMaker make = wc.getTreeMaker();
            ClassTree classTree = (ClassTree) path.getLeaf();
            List<Tree> members = new ArrayList<>(classTree.getMembers());
            String className = typeClassElement.toString();

            PropertyMethodBuilder propertyMethodBuilder = new PropertyMethodBuilder(make, members, fields, className);

            index = propertyMethodBuilder.removeExistingPropMethods(index);

            propertyMethodBuilder.addPropMethods(index);

            ClassTree newClassTree = make.Class(classTree.getModifiers(),
                    classTree.getSimpleName(),
                    classTree.getTypeParameters(),
                    classTree.getExtendsClause(),
                    (List<ExpressionTree>) classTree.getImplementsClause(),
                    members);

            wc.rewrite(classTree, newClassTree);
        }
    }

    private List<VariableElement> getFields(Lookup context, CompilationController controller) throws CodeGeneratorException {
        try {
            List<VariableElement> elementList = new ArrayList<>();
            TreePath treePath = context.lookup(TreePath.class);
            TreePath path = TreeHelper.getParentElementOfKind(Tree.Kind.CLASS, treePath);
            TypeElement typeElement = (TypeElement) controller.getTrees().getElement(path);

            if (!typeElement.getKind().isClass()) {
                throw new CodeGeneratorException("typeElement " + typeElement.getKind().name() + " is not a class, cannot generate code.");
            }

            Elements elements = controller.getElements();
            List<VariableElement> temp = ElementFilter.fieldsIn(elements.getAllMembers(typeElement));

            for (VariableElement e : temp) {
                try {
                    Class<?> memberClass = Class.forName(getClassName(e.asType().toString()));
                    if (Property.class.isAssignableFrom(memberClass)
                            && !ListProperty.class.isAssignableFrom(memberClass)
                            && !MapProperty.class.isAssignableFrom(memberClass)
                            && !ObjectProperty.class.isAssignableFrom(memberClass)
                            && !SetProperty.class.isAssignableFrom(memberClass)) {

                        elementList.add(e);
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return elementList;
        } catch (NullPointerException ex) {
            throw new CodeGeneratorException(ex);
        }
    }

    protected String getClassName(String fullName) {
        if (!fullName.contains("<")) {
            return fullName;
        } else {
            return fullName.substring(0, fullName.indexOf("<"));
        }
    }

    private static class CodeGeneratorException extends Exception {

        private static final long serialVersionUID = 1L;

        public CodeGeneratorException(String message) {
            super(message);
        }

        public CodeGeneratorException(Throwable cause) {
            super(cause);
        }
    }

}
