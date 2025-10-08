package consulo.cmake.inspections;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.CMakeLanguage;
import consulo.cmake.utils.CMakePDC;
import consulo.cmake.utils.CMakePSITreeSearch;
import consulo.cmake.utils.CMakeVarStringUtil;
import consulo.language.Language;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.inspection.localize.InspectionLocalize;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class VariableUnusedInspection extends LocalInspectionTool {
  @Nonnull
  @Override
  public PsiElementVisitor buildVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        super.visitElement(element);
        if (!InspectionUtils.isCommandName(element, "set")) return;
        PsiElement commandArguments =
            PsiTreeUtil.getNextSiblingOfType(element, CMakePDC.ARGUMENTS_CLASS);
        PsiElement firstArgument =
            PsiTreeUtil.getChildOfAnyType(commandArguments, CMakePDC.COMMAND_ARGUMENT_CLASSES);
        if (firstArgument == null) return;
        firstArgument = CMakePDC.transformToLiteral(firstArgument);
        if (CMakePDC.isClassOfVarDef(firstArgument)
            && CMakeVarStringUtil.couldBeVarName(firstArgument.getText())
            && !CMakeVarStringUtil.isPredefinedCMakeVar(firstArgument.getText())
            && !CMakePSITreeSearch.existReferenceTo(firstArgument))
          holder.registerProblem(firstArgument, "Variable is set but never used.");
      }
    };
  }

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return InspectionLocalize.groupNamesProbableBugs();
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return LocalizeValue.localizeTODO("Variable is set but never used");
    }

    @Nullable
    @Override
    public Language getLanguage() {
        return CMakeLanguage.INSTANCE;
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WEAK_WARNING;
    }
}
