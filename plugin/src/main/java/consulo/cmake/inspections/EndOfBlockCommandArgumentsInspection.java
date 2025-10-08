package consulo.cmake.inspections;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.CMakeLanguage;
import consulo.cmake.utils.CMakePDC;
import consulo.cmake.utils.CMakePlusPDC;
import consulo.language.Language;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.inspection.localize.InspectionLocalize;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class EndOfBlockCommandArgumentsInspection extends LocalInspectionTool {

    private final MyQuickFix myQuickFix = new MyQuickFix();

    @Nonnull
    @Override
    public PsiElementVisitor buildVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                if (CMakePlusPDC.END_OF_COMMAND_KEYWORD_ELEMENT_TYPES.contains(
                    element.getNode().getElementType())) {
                    PsiElement arguments = PsiTreeUtil.getChildOfType(element, CMakePDC.ARGUMENTS_CLASS);
                    if (arguments != null && !arguments.getText().replace(" ", "").equals("()")) {
                        holder.registerProblem(
                            arguments,
                            "Commands else, endif, endforeach, endwhile, endfunction, endmacro should not take arguments.",
                            myQuickFix);
                    }
                }
            }
        };
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Nullable
    @Override
    public Language getLanguage() {
        return CMakeLanguage.INSTANCE;
    }

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return InspectionLocalize.groupNamesNamingConventions();
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("End-of-block commands should not take arguments");
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WEAK_WARNING;
    }

    private static class MyQuickFix implements LocalQuickFix {

        @Nonnull
        @Override
        public LocalizeValue getName() {
            return LocalizeValue.localizeTODO("Convert to empty arguments list");
        }

        @Override
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            PsiElement arguments = descriptor.getPsiElement();
            assert arguments != null;
            // Can't replace arguments element due to extra whitespace insertion in CLion (bug?)
//      arguments.replace(CMakePlusPDC.createEmptyArguments(project));
            for (PsiElement child : arguments.getChildren()) {
                if (child.getPrevSibling() == null || child.getNextSibling() == null) {
                    continue;
                }
                child.delete();
            }
        }
    }
}
