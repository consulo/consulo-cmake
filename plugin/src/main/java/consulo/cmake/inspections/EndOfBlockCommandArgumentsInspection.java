package consulo.cmake.inspections;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.CMakeLanguage;
import consulo.cmake.utils.CMakePDC;
import consulo.cmake.utils.CMakePlusPDC;
import consulo.language.Language;
import consulo.language.editor.inspection.*;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.Nls;

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
    public String getGroupDisplayName() {
        return InspectionsBundle.message("group.names.naming.conventions");
    }

    @Nonnull
    @Override
    public String getDisplayName() {
        return "End-of-block commands should not take arguments";
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WEAK_WARNING;
    }

    private static class MyQuickFix implements LocalQuickFix {

        @Nls(capitalization = Nls.Capitalization.Sentence)
        @Nonnull
        @Override
        public String getFamilyName() {
            return "Convert to empty arguments list";
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
