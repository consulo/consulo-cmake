package com.cmakeplugin.inspections;

import com.cmakeplugin.utils.CMakeIFWHILEcheck;
import com.cmakeplugin.utils.CMakePDC;
import com.cmakeplugin.utils.CMakePSITreeSearch;
import com.cmakeplugin.utils.CMakeVarStringUtil;
import consulo.document.util.TextRange;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class NotDefinedVariableInspection extends LocalInspectionTool {
  /**
   * This method is overridden to provide a custom visitor The visitor must not be recursive and
   * must be thread-safe.
   *
   * @param holder object for visitor to register problems found.
   * @param isOnTheFly true if inspection was run in non-batch mode
   * @return PsiElementVisitor.
   */
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
    return new PsiElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        super.visitElement(element);
        if (!CMakePDC.classCanHoldVarRef(element)
            || CMakeVarStringUtil.isCMakeLegacy(element.getText())) return;
        for (TextRange varRefRange : CMakeIFWHILEcheck.getInnerVars(element)) {
          final String innerVarName =
              element.getText().substring(varRefRange.getStartOffset(), varRefRange.getEndOffset());
          if (!CMakeVarStringUtil.isPredefinedCMakeVar(innerVarName)
              && !element.textMatches(innerVarName) // exclude unquoted arg inside If/While
              && !CMakePSITreeSearch.existDefinitionOf(element, innerVarName)) {
            holder.registerProblem(
                element, varRefRange, "Possibly not defined Variable: " + innerVarName);
          }
        }
      }
    };
  }
}
