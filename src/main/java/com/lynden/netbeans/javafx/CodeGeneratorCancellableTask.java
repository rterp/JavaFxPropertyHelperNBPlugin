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
        int idx = TreeHelper.findClassMemberIndex(wc,
                (ClassTree) path.getLeaf(), caretOffset);
        generateCode(wc, path, idx);
    }

    @Override
    public void cancel() {
    }

}
