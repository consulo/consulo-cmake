package consulo.cmake;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.utils.CMakePlusPDC;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.editor.folding.FoldingBuilder;
import consulo.language.editor.folding.FoldingDescriptor;
import consulo.language.editor.folding.NamedFoldingDescriptor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class CMakeFoldingBuilderForMultiLineBodies implements FoldingBuilder {
    @Nonnull
    @Override
    public FoldingDescriptor[] buildFoldRegions(@Nonnull ASTNode node, @Nonnull Document document) {
        return PsiTreeUtil.findChildrenOfAnyType(node.getPsi(), CMakePlusPDC.FOLDABLE_BODIES).stream()
            .filter(it -> isMultiLine(document, it))
            .map(this::createFoldingDescriptor)
            .toArray(FoldingDescriptor[]::new);
    }

    private boolean isMultiLine(@Nonnull Document document, @Nonnull PsiElement element) {
        final TextRange range = element.getTextRange();
        if (range.isEmpty()) {
            return false;
        }
        final int firstLineNumber = document.getLineNumber(range.getStartOffset());
        // exclude EOL with '\n' that belongs to BodyBlock
        final int lastLineNumber = document.getLineNumber(range.getEndOffset() - 1);
        return firstLineNumber != lastLineNumber;
    }

    @Nonnull
    private FoldingDescriptor createFoldingDescriptor(PsiElement element) {
        return new NamedFoldingDescriptor(
            element.getNode(),
            CMakePlusPDC.getBodyBlockRangeToFold(element),
            null,
            getPlaceholderText(element));
    }

    @Nonnull
    private String getPlaceholderText(PsiElement element) {
        String text = element.getText();
        if (text.length() < 50) {
            return text;
        }
        String prefix = text.substring(0, 35);
        String suffix = text.substring(text.length() - 10);
        return prefix + " ... " + suffix;
    }

    @Nullable
    @Override
    public String getPlaceholderText(@Nonnull ASTNode node) {
        return null;
    }

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
