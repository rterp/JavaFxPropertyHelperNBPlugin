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
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
abstract class CodeGeneratorCancellableTask implements CancellableTask<WorkingCopy> {

    private final JTextComponent textComponent;

    public CodeGeneratorCancellableTask(JTextComponent textComponent) {
        this.textComponent = textComponent;
    }

    @Override
    public void run(WorkingCopy workingCopy) throws IOException {
        workingCopy.toPhase(JavaSource.Phase.RESOLVED);
        workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
        generate(workingCopy);
    }

    public abstract void generateCode(WorkingCopy workingCopy, TreePath path,
            int position);

    private void generate(WorkingCopy wc) throws IOException {
        final int caretOffset = textComponent.getCaretPosition();
        TreePath path = wc.getTreeUtilities().pathFor(caretOffset);
        path = TreeHelper.getParentElementOfKind(Tree.Kind.CLASS, path);
        int idx = TreeHelper.findClassMemberIndex(wc, (ClassTree) path.getLeaf(), caretOffset);
        generateCode(wc, path, idx);
    }

    @Override
    public void cancel() {
    }

}
