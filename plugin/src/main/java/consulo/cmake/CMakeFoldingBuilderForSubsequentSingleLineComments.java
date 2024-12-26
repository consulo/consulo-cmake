package consulo.cmake;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.utils.CMakePlusPDC;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.editor.folding.FoldingBuilder;
import consulo.language.editor.folding.FoldingDescriptor;
import consulo.language.editor.folding.NamedFoldingDescriptor;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;

@ExtensionImpl
public class CMakeFoldingBuilderForSubsequentSingleLineComments implements FoldingBuilder {

    @RequiredReadAction
    @Nonnull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@Nonnull ASTNode node, @Nonnull Document document) {
        List<FoldingDescriptor> result = new ArrayList<>();
        final Collection<PsiComment> comments =
            PsiTreeUtil.findChildrenOfType(node.getPsi(), PsiComment.class);
        Set<PsiComment> processedComments = new HashSet<>();

        for (PsiComment comment : comments) {
            if (processedComments.add(comment) && CMakePlusPDC.isLineComment(comment)) {
                final FoldingDescriptor commentDescriptor =
                    getCommentDescriptor(comment, processedComments);
                if (commentDescriptor != null) {
                    result.add(commentDescriptor);
                }
            }
        }
        return result.toArray(new FoldingDescriptor[0]);
    }

    @Nullable
    private FoldingDescriptor getCommentDescriptor(
        @Nonnull PsiComment comment, @Nonnull Set<PsiComment> processedComments) {
        final TextRange commentRange = getSubsequentSingleLineCommentsRange(comment, processedComments);
        return (commentRange == null)
            ? null
            : new NamedFoldingDescriptor(
            comment.getNode(), commentRange, null, getPlaceholderText(comment));
    }

    /**
     * see {@link com.intellij.codeInsight.folding.impl.CommentFoldingUtil}
     */
    @Nullable
    private TextRange getSubsequentSingleLineCommentsRange(
        PsiComment comment, Set<PsiComment> processedComments) {
        PsiElement end = null;
        for (PsiElement current = comment.getNextSibling();
             current != null;
             current = current.getNextSibling()) {
            if (current instanceof PsiComment
                && CMakePlusPDC.isLineComment((PsiComment) current)
                && !processedComments.contains(current)) {
                end = current;
                processedComments.add((PsiComment) current);
                continue;
            }
            if (CMakePlusPDC.isSubsequentLineCommentsGlueElement(current)) {
                continue;
            }
            break;
        }
        return (end == null)
            ? null
            : new TextRange(comment.getTextRange().getStartOffset(), end.getTextRange().getEndOffset());
    }

    @Nonnull
    private String getPlaceholderText(PsiElement element) {
        String text = element.getText();
        if (text.length() > 50) {
            text = text.substring(0, 50);
        }
        return text + " ... ";
    }

    @RequiredReadAction
    @Nullable
    @Override
    public String getPlaceholderText(@Nonnull ASTNode node) {
        return null;
    }

    @RequiredReadAction
    @Override
    public boolean isCollapsedByDefault(@Nonnull ASTNode node) {
        return false;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return CMakeLanguage.INSTANCE;
    }
}
