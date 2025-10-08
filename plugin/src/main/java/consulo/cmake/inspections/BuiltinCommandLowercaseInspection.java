package consulo.cmake.inspections;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.CMakeLanguage;
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
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class BuiltinCommandLowercaseInspection extends LocalInspectionTool {

    private final BuiltinCommandLowercaseQuickFix myQuickFix = new BuiltinCommandLowercaseQuickFix();

    @Nonnull
    @Override
    public PsiElementVisitor buildVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                if (InspectionUtils.isBuiltinCommand(element)) {
                    String commandName = element.getText();
                    if (!commandName.toLowerCase().equals(commandName)) {
                        holder.registerProblem(element, "Builtin commands should be used in lowercase.", myQuickFix);
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
        return LocalizeValue.localizeTODO("Builtin commands should be used in lowercase");
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WEAK_WARNING;
    }

    private static class BuiltinCommandLowercaseQuickFix implements LocalQuickFix {
        @Nonnull
        @Override
        public LocalizeValue getName() {
            return LocalizeValue.localizeTODO("Convert command name to lowercase");
        }

        @Override
        public void applyFix(@Nonnull Project project, @Nonnull ProblemDescriptor descriptor) {
            PsiElement commandName = descriptor.getPsiElement();
            String nameLowercased = commandName.getText().toLowerCase();
            commandName.replace(CMakePlusPDC.createCommandName(project, nameLowercased));
        }
    }

}
