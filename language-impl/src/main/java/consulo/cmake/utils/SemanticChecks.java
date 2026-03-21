package consulo.cmake.utils;

import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;

public class SemanticChecks {

  public static boolean possibleVarDef(PsiElement possibleVarDef) {
    if (!PsiTreeUtil.instanceOf(possibleVarDef, CMakePDC.VARDEF_ARGUMENT_CLASSES))
      possibleVarDef = PsiTreeUtil.getParentOfType(possibleVarDef, CMakePDC.VARDEF_ARGUMENT_CLASSES);

    PsiElement commandName = CMakePSITreeSearch.getCommandNameElement(possibleVarDef);
    if (commandName != null) {
      PsiElement commandArguments =
          PsiTreeUtil.getParentOfType(possibleVarDef, CMakePDC.ARGUMENTS_CLASS);
      if (commandName.textMatches("set")) {
        final PsiElement firstArgument =
            PsiTreeUtil.getChildOfAnyType(commandArguments, CMakePDC.VARDEF_ARGUMENT_CLASSES);
        return possibleVarDef == firstArgument;
      }
    }

    if (isFunMacroName(possibleVarDef)) return false;

    return true;
  }

  private static boolean isFunMacroName(PsiElement possibleVarDef) {
    return CMakePSITreeSearch.getFunMacroNameElement(CMakePSITreeSearch.getFunMacroRootElement(possibleVarDef)) == possibleVarDef
        || CMakePSITreeSearch.getFunMacroNameElement(CMakePSITreeSearch.getFunMacroEndElement(possibleVarDef)) == possibleVarDef;
  }
}
