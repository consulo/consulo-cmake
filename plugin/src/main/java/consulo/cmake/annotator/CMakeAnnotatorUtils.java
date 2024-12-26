package consulo.cmake.annotator;

import consulo.cmake.CMakeKeywords;
import consulo.cmake.CMakeSyntaxHighlighter;
import consulo.cmake.utils.CMakeIFWHILEcheck;
import consulo.cmake.utils.CMakePSITreeSearch;
import consulo.cmake.utils.CMakeVarStringUtil;
import consulo.colorScheme.TextAttributesKey;
import consulo.document.util.TextRange;
import consulo.language.editor.annotation.Annotation;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;

// import static CMakeKeywords.*;

class CMakeAnnotatorUtils {

  static boolean annotateLegacy(@Nonnull PsiElement element, @Nonnull AnnotationHolder holder) {
    if (CMakeVarStringUtil.isCMakeLegacy(element.getText())) {
      return createInfoAnnotation(element, holder, CMakeSyntaxHighlighter.UNQUOTED_LEGACY);
    }
    return false;
  }

  static boolean annotatePathURL(@Nonnull PsiElement element, @Nonnull AnnotationHolder holder) {
    if (element.getText().contains("/")) {
      return createInfoAnnotation(element, holder, CMakeSyntaxHighlighter.CMAKE_PATH_URL);
    }
    return false;
  }

  static boolean annotatePredefinedVariable(
      @Nonnull PsiElement element, @Nonnull AnnotationHolder holder) {
    if (CMakeIFWHILEcheck.couldBeVarDef(element)
        && CMakeVarStringUtil.isPredefinedCMakeVar(element.getText())) {
      return createInfoAnnotation(element, holder, CMakeSyntaxHighlighter.CMAKE_VAR_DEF);
    }
    return false;
  }

  static boolean annotateVarDeclaration(
      @Nonnull PsiElement element, @Nonnull AnnotationHolder holder) {
    if (CMakePSITreeSearch.existReferenceTo(element)) {
      return createInfoAnnotation(element, holder, CMakeSyntaxHighlighter.VAR_DEF);
    }
    return false;
  }

  static void annotateVarReferences(@Nonnull PsiElement element, @Nonnull AnnotationHolder holder) {
    String argtext = element.getText();
    int elementStartInFile = element.getTextRange().getStartOffset();

    // Highlight Outer variables.
    for (TextRange outerVarRange : CMakeIFWHILEcheck.getOuterVarRefs(element)) {
      createInfoAnnotation(
          outerVarRange.shiftRight(elementStartInFile), holder, CMakeSyntaxHighlighter.VAR_REF);
    }

    // Highlight Inner variables.
    for (TextRange innerVarRange : CMakeIFWHILEcheck.getInnerVars(element)) {
      String innerVarName =
          argtext.substring(innerVarRange.getStartOffset(), innerVarRange.getEndOffset());

      // Highlight Inner CMake predefined variables
      if (CMakeVarStringUtil.isPredefinedCMakeVar(innerVarName)) {
        createInfoAnnotation(
            innerVarRange.shiftRight(elementStartInFile),
            holder,
            CMakeSyntaxHighlighter.CMAKE_VAR_REF);
      }
    }

    // Highlight Inner CMake predefined ENV variables
    // fixme: implement ref/resolve for ENV
    for (TextRange innerVarRange : CMakeVarStringUtil.getInnerEnvVars(argtext)) {
      final String varEnv =
          argtext.substring(innerVarRange.getStartOffset(), innerVarRange.getEndOffset());
      if (CMakeKeywords.isVariableENV(varEnv)) {
        createInfoAnnotation(
            innerVarRange.shiftRight(elementStartInFile),
            holder,
            CMakeSyntaxHighlighter.CMAKE_VAR_REF);
      }
    }
  }

  static void annotateCommand(@Nonnull PsiElement element, @Nonnull AnnotationHolder holder) {
    String commandName = element.getText();
    if (CMakeKeywords.isCommandDeprecated(commandName)) {
      createDeprecatedAnnotation(element, holder, "Deprecated command");
    } else if (CMakeKeywords.isCommand(commandName)) {
      createInfoAnnotation(element, holder, CMakeSyntaxHighlighter.CMAKE_COMMAND);
    } else if (CMakePSITreeSearch.existFunctionDefFor(element)) {
      createInfoAnnotation(element, holder, CMakeSyntaxHighlighter.FUNCTION);
    } else if (CMakePSITreeSearch.existMacroDefFor(element)) {
      createInfoAnnotation(element, holder, CMakeSyntaxHighlighter.MACROS);
    }
  }

  static void annotateFunctionName(@Nonnull PsiElement element, @Nonnull AnnotationHolder holder) {
    PsiElement nameElement = CMakePSITreeSearch.getFunMacroNameElement(element);
    if (nameElement != null)
      createInfoAnnotation(nameElement, holder, CMakeSyntaxHighlighter.FUNCTION);
  }

  static void annotateMacrosName(@Nonnull PsiElement element, @Nonnull AnnotationHolder holder) {
    PsiElement nameElement = CMakePSITreeSearch.getFunMacroNameElement(element);
    if (nameElement != null)
      createInfoAnnotation(nameElement, holder, CMakeSyntaxHighlighter.MACROS);
  }

  static boolean annotateProperty(@Nonnull PsiElement element, @Nonnull AnnotationHolder holder) {
    String propertyName = element.getText();
    if (CMakeVarStringUtil.isCMakePropertyDeprecated(propertyName)) {
      return createDeprecatedAnnotation(element, holder, "Deprecated property");
    }
    if (CMakeVarStringUtil.isCMakeProperty(propertyName)) {
      return createInfoAnnotation(element, holder, CMakeSyntaxHighlighter.CMAKE_PROPERTY);
    }
    return false;
  }

  static boolean annotateOperator(@Nonnull PsiElement element, @Nonnull AnnotationHolder holder) {
    String operatorName = element.getText();
    if (CMakeVarStringUtil.isCMakeOperator(operatorName)) {
      return createInfoAnnotation(element, holder, CMakeSyntaxHighlighter.CMAKE_OPERATOR);
    }
    if (CMakeVarStringUtil.isCMakeBoolValue(operatorName)) {
      return createInfoAnnotation(element, holder, CMakeSyntaxHighlighter.CMAKE_BOOLEAN);
    }
    // Modules
    if (CMakeVarStringUtil.isCMakeModule(operatorName)) {
      final PsiElement commandNameElement = CMakePSITreeSearch.getCommandNameElement(element);
      if (commandNameElement != null
          && (commandNameElement.textMatches("include")
          || commandNameElement.textMatches("find_package"))) {
      return createInfoAnnotation(element, holder, CMakeSyntaxHighlighter.CMAKE_MODULE);
      }
    }
    return false;
  }

  private static boolean createWeakWarningAnnotation(
      @Nonnull TextRange range, @Nonnull AnnotationHolder holder, @Nonnull String message) {
    holder
        .createWeakWarningAnnotation(range, message)
        .setHighlightType(ProblemHighlightType.WEAK_WARNING);
    return true;
  }

  private static boolean createDeprecatedAnnotation(
      @Nonnull PsiElement element, @Nonnull AnnotationHolder holder, @Nonnull String message) {
    holder
        .createWarningAnnotation(element, message)
        .setHighlightType(ProblemHighlightType.LIKE_DEPRECATED);
    return true;
  }

  private static boolean createInfoAnnotation(
      @Nonnull PsiElement element,
      @Nonnull AnnotationHolder holder,
      @Nonnull TextAttributesKey textAttKey) {
    return createInfoAnnotation(element.getTextRange(), holder, textAttKey);
  }

  private static boolean createInfoAnnotation(
      @Nonnull TextRange range,
      @Nonnull AnnotationHolder holder,
      @Nonnull TextAttributesKey textAttKey) {
    Annotation annotation = holder.createInfoAnnotation(range, textAttKey.getExternalName());
    annotation.setTooltip(LocalizeValue.empty());
    annotation.setTextAttributes(textAttKey);
    return true;
  }
}
