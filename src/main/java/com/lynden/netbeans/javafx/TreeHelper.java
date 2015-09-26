package com.lynden.netbeans.javafx;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.io.IOException;
import javax.swing.text.Document;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.editor.GuardedDocument;

/**
 *
 */
class TreeHelper {
    /**
     * Search up the hierarchy of elements for one of the given kind.
     *
     * @param kind the element's kind to search for
     * @param path the starting element
     * @return {@code null} if no element was found.
     */
    static TreePath getParentElementOfKind(Tree.Kind kind, TreePath path) {
        if (path != null) {
            TreePath tpath = path;
            while (tpath != null) {
                if (kind == tpath.getLeaf().getKind()) {
                    return tpath;
                }
                tpath = tpath.getParentPath();
            }
        }
        return null;
    }

    /**
     * Find the index of the current class member.
     *
     * @param wc
     * @param classTree
     * @param offset
     * @return
     */
    static int findClassMemberIndex(WorkingCopy wc,
            ClassTree classTree,
            int offset) {

        int index = 0;
        SourcePositions sp = wc.getTrees().getSourcePositions();
        GuardedDocument gdoc = null;
        try {
            Document doc = wc.getDocument();
            if (doc != null && doc instanceof GuardedDocument) {
                gdoc = (GuardedDocument) doc;
            }
        } catch (IOException ioe) {
        }

        Tree lastMember = null;
        for (Tree tree : classTree.getMembers()) {
            if (offset <= sp.getStartPosition(wc.getCompilationUnit(), tree)) {
                if (gdoc == null) {
                    break;
                }
                int pos = (int) (lastMember != null ? sp.getEndPosition(wc.
                        getCompilationUnit(), lastMember) : sp.getStartPosition(
                                wc.getCompilationUnit(), classTree));
                pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
                if (pos <= sp.getStartPosition(wc.getCompilationUnit(), tree)) {
                    break;
                }
            }
            index++;
            lastMember = tree;
        }
        return index;
    }

}
