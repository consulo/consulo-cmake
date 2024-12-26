package consulo.cmake;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.utils.CMakeIFWHILEcheck;
import consulo.cmake.utils.CMakePDC;
import consulo.cmake.utils.CMakePSITreeSearch;
import consulo.cmake.utils.CMakePlusPDC;
import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorKeys;
import consulo.dataContext.DataContext;
import consulo.dataContext.DataManager;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.editor.documentation.DocumentationProviderEx;
import consulo.language.editor.documentation.LanguageDocumentationProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Objects;

@ExtensionImpl
public class CMakeDocProvider extends DocumentationProviderEx implements LanguageDocumentationProvider {
    @Nullable
    @Override
    public PsiElement getCustomDocumentationElement(
        @Nonnull Editor editor, @Nonnull PsiFile file, @Nullable PsiElement contextElement) {
        if (contextElement == null) {
            return null;
        }
        if (CMakePDC.COMMAND_NAME_CLASS.isInstance(contextElement.getParent())) {
            return contextElement.getParent();
        }
        if (CMakePlusPDC.COMMAND_KEYWORD_ELEMENT_TYPES.contains(
            contextElement.getNode().getElementType())) {
            return contextElement;
        }
        return null;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return CMakeLanguage.INSTANCE;
    }

    @Override
    @Nullable
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (CMakePDC.COMMAND_NAME_CLASS.isInstance(element)
            || (element.getNode() != null
            && CMakePlusPDC.COMMAND_KEYWORD_ELEMENT_TYPES.contains(
            element.getNode().getElementType()))) {
            return CMakeKeywords.getCommandHelp(element.getText());
        }

        if (CMakePDC.isClassOfVarDef(element)) {
            final PsiElement commandNameElement = CMakePSITreeSearch.getCommandNameElement(element);
            if (commandNameElement != null
                && (commandNameElement.textMatches("include")
                || commandNameElement.textMatches("find_package"))) {
                return CMakeKeywords.getModuleHelp(element.getText());
            }
        }

        if (CMakePDC.isClassOfVarDef(element) || CMakePDC.isClassOfVarRefInsideIfWhile(element)) {
            final String elementText = element.getText();

            if (elementText.matches("CMP[0-9]{4}")) {
                return CMakeKeywords.getPolicyHelp(elementText);
            }

            String propHelp = CMakeKeywords.getPropertyHelp(elementText);
            if (propHelp != null) {
                return propHelp;
            }

            String varHelp = CMakeKeywords.getVariableHelp(elementText);
            if (varHelp != null) {
                return varHelp;
            }
        }

        // varRefs inside text
        if (originalElement != null
            && CMakePlusPDC.VARREF_ELEMENT_TYPES.contains(originalElement.getNode().getElementType())) {
            final String elementText = originalElement.getText();
            final int offsetInText = getCaretOffsetInElement(originalElement);
            final List<TextRange> innerVars = CMakeIFWHILEcheck.getInnerVars(originalElement);

            TextRange innerVarRange =
                (offsetInText < 0)
                    ? null
                    : innerVars.stream().filter(it -> it.contains(offsetInText)).findFirst().orElse(null);

            if (innerVarRange != null) {
                String innerVarName = innerVarRange.substring(elementText);
                return CMakeKeywords.getVariableHelp(innerVarName);
            }
            else if (!innerVars.isEmpty()) {
                final String allVarsHelp = innerVars.stream()
                    .map(range -> range.substring(elementText))
                    .map(CMakeKeywords::getVariableHelp)
                    .filter(Objects::nonNull)
                    .reduce("", (a, b) -> a + "<br>" + b);
                return allVarsHelp.isEmpty() ? null : allVarsHelp;
            }
        }
        return null;
    }

    // fixme
    // https://intellij-support.jetbrains.com/hc/en-us/community/posts/206794335-How-to-get-cursor-position-in-the-current-editor-?
    private int getCaretOffsetInElement(PsiElement element) {
        // PsiUtilBase.findEditor(element);
        final DataContext dataContext = DataManager.getInstance().getDataContext();
        final Editor editor = dataContext.getData(EditorKeys.EDITOR_EVEN_IF_INACTIVE);
        if (editor == null) {
            return -1;
        }

        final int caretOffset = editor.getCaretModel().getOffset();
        final int elementStartOffset = element.getTextRange().getStartOffset();
        final int elementEndOffset = element.getTextRange().getEndOffset();

        if (caretOffset < elementStartOffset || elementEndOffset < caretOffset) {
            return -1;
        }

        return caretOffset - elementStartOffset;
    }
}
