package consulo.cmake;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.utils.CMakePSITreeSearch;
import consulo.cmake.utils.CMakePlusPDC;
import consulo.cmake.utils.SemanticChecks;
import consulo.language.Language;
import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.pattern.PlatformPatterns;
import consulo.language.psi.PsiElement;
import consulo.language.util.ProcessingContext;
import jakarta.annotation.Nonnull;

import java.util.stream.Collectors;

@ExtensionImpl
public class CMakeVariableCompletionContributor extends CompletionContributor {
    public CMakeVariableCompletionContributor() {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(CMakePlusPDC.getLanguageInstance())
                .withElementType(CMakePlusPDC.VARDEF_ELEMENT_TYPES),
            new CMakeVarDefCompletionProvider());
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(CMakePlusPDC.getLanguageInstance())
                .withElementType(CMakePlusPDC.VARREF_ELEMENT_TYPES),
            new CMakeVarRefCompletionProvider());
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return CMakeLanguage.INSTANCE;
    }

    private static class CMakeVarDefCompletionProvider implements CompletionProvider {

        @Override
        public void addCompletions(
            @Nonnull CompletionParameters parameters,
            @Nonnull ProcessingContext context,
            @Nonnull CompletionResultSet resultSet) {

            if (SemanticChecks.possibleVarDef(parameters.getPosition())) {
/*
        final Color varDefColor =
            CMakeSyntaxHighlighter.VAR_DEF.getDefaultAttributes().getForegroundColor();

        resultSet.addAllElements(
            CMakePSITreeSearch.getAllReferencedVarDefs(parameters.getOriginalFile())
                .map(LookupElementBuilder::create)
                .map(it -> it.withIcon(CMakePlusPDC.ICON_VAR))
                .map(it -> it.withItemTextForeground(varDefColor))
                .collect(Collectors.toList()));
*/

                resultSet.addAllElements(
                    CMakePSITreeSearch.getAllPossibleVarDefs(parameters.getOriginalFile())
                        .map(LookupElementBuilder::create)
                        .map(it -> it.withIcon(CMakePlusPDC.ICON_VAR))
                        .collect(Collectors.toList()));

//        if (!CMakePDC.isCLION)
                resultSet.addAllElements(
                    CMakeKeywords.getAllVariables().stream()
                        .map(LookupElementBuilder::create)
                        .collect(Collectors.toList()));
            }
        }
    }

    private static class CMakeVarRefCompletionProvider implements CompletionProvider {

        @Override
        public void addCompletions(
            @Nonnull CompletionParameters parameters,
            @Nonnull ProcessingContext context,
            @Nonnull CompletionResultSet resultSet) {

            final PsiElement element = parameters.getPosition();
            final String text = element.getText();
            if (text.length() < 2) {
                return;
            }
            final int offsetInElement = parameters.getOffset() - element.getTextRange().getStartOffset();
            int varDefStart = text.substring(0, offsetInElement).lastIndexOf("${");
            if (varDefStart >= 0) {
                resultSet = resultSet.withPrefixMatcher(text.substring(varDefStart + 2, offsetInElement));
/*
        final Color varDefColor =
            CMakeSyntaxHighlighter.VAR_DEF.getDefaultAttributes().getForegroundColor();

        resultSet.addAllElements(
            CMakePSITreeSearch.getAllReferencedVarDefs(parameters.getOriginalFile())
                .map(LookupElementBuilder::create)
                .map(it -> it.withIcon(CMakePlusPDC.ICON_VAR))
                .map(it -> it.withItemTextForeground(varDefColor))
                .map(it -> it.withInsertHandler(getVarRefInsertHandler()))
                .collect(Collectors.toList()));
*/

                resultSet.addAllElements(
                    CMakePSITreeSearch.getAllPossibleVarDefs(parameters.getOriginalFile())
                        .map(LookupElementBuilder::create)
                        .map(it -> it.withIcon(CMakePlusPDC.ICON_VAR))
                        .map(it -> it.withInsertHandler(getVarRefInsertHandler()))
                        .collect(Collectors.toList()));

                if (!false) {
                    resultSet.addAllElements(
                        CMakeKeywords.getAllVariables().stream()
                            .map(LookupElementBuilder::create)
                            .map(it -> it.withInsertHandler(getVarRefInsertHandler()))
                            .collect(Collectors.toList()));
                }
            }
      /*
            if (text.contains("${")) {
              final String[] allPossibleVarDefs =
                  CMakePSITreeSearch.getAllPossibleVarDefs(parameters.getOriginalFile())
                      .toArray(String[]::new);
              CMakeVariableProviderFriend.myAddCompletions(parameters, resultSet, "${", allPossibleVarDefs);
            }
      */
        }

        private static InsertHandler<LookupElement> getVarRefInsertHandler() {
            return new InsertHandler<LookupElement>() {
                @Override
                public void handleInsert(@Nonnull InsertionContext context, @Nonnull LookupElement item) {
                    final int indexLookupEnd = context.getStartOffset() + item.getLookupString().length();
                    int caretShift = 0;
                    if (context.getDocument().getText().charAt(indexLookupEnd) != '}') {
                        context.getDocument().insertString(context.getTailOffset(), "}");
                    }
                    else {
                        caretShift = 1;
                    }
                    context.getEditor().getCaretModel().moveToOffset(context.getTailOffset() + caretShift);
                }
            };
        }
    }
}
