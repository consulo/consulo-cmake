package consulo.cmake.language.impl.inspections;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.language.impl.CMakeLanguage;
import consulo.cmake.localize.CMakeLocalize;
import consulo.cmake.utils.CMakeIFWHILEcheck;
import consulo.cmake.utils.CMakePDC;
import consulo.cmake.utils.CMakePSITreeSearch;
import consulo.cmake.utils.CMakeVarStringUtil;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.inspection.localize.InspectionLocalize;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class NotDefinedVariableInspection extends LocalInspectionTool {
    /**
     * This method is overridden to provide a custom visitor The visitor must not be recursive and
     * must be thread-safe.
     *
     * @param holder     object for visitor to register problems found.
     * @param isOnTheFly true if inspection was run in non-batch mode
     * @return PsiElementVisitor.
     */
    @Nonnull
    @Override
    public PsiElementVisitor buildVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);
                if (!CMakePDC.classCanHoldVarRef(element)
                    || CMakeVarStringUtil.isCMakeLegacy(element.getText())) {
                    return;
                }
                for (TextRange varRefRange : CMakeIFWHILEcheck.getInnerVars(element)) {
                    final String innerVarName =
                        element.getText().substring(varRefRange.getStartOffset(), varRefRange.getEndOffset());
                    if (!CMakeVarStringUtil.isPredefinedCMakeVar(innerVarName)
                        && !element.textMatches(innerVarName) // exclude unquoted arg inside If/While
                        && !CMakePSITreeSearch.existDefinitionOf(element, innerVarName)) {
                        holder.registerProblem(
                            element, varRefRange, CMakeLocalize.inspectionNotDefinedVariableMessage(innerVarName).get());
                    }
                }
            }
        };
    }

    @Nullable
    @Override
    public Language getLanguage() {
        return CMakeLanguage.INSTANCE;
    }

    @Nonnull
    @Override
    public LocalizeValue getGroupDisplayName() {
        return InspectionLocalize.groupNamesProbableBugs();
    }

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return CMakeLocalize.inspectionNotDefinedVariableDisplayName();
    }

    @Nonnull
    @Override
    public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WEAK_WARNING;
    }
}
