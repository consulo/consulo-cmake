package com.cmakeplugin.annotator;

import com.cmakeplugin.psi.*;
import com.cmakeplugin.utils.CMakePDC;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import static com.cmakeplugin.annotator.CMakeAnnotatorUtils.*;

public class CMakeAnnotator implements Annotator {

  @Override
  public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
      annotateIdea(element, holder);
  }

  private void annotateIdea(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
    if (element instanceof CMakeCommandName) {
      annotateCommand(element, holder);
    } else if (CMakePDC.FUNCTION_CLASS.isInstance(element)) {
      annotateFunctionName(element, holder);
    } else if (CMakePDC.MACRO_CLASS.isInstance(element)) {
      annotateMacrosName(element, holder);
    } else if (element instanceof CMakeQuotedArgumentContainer) {
      // Annotate Quoted argument
      assert element.getPrevSibling() instanceof CMakeBrace
          && element.getNextSibling() instanceof CMakeBrace;
      annotateVarReferences(element, holder);
    } else if (element instanceof CMakeUnquotedArgumentMaybeVariableContainer) {
      // Annotate Unquoted argument with possible Var declaration
      if (!(annotatePredefinedVariable(element, holder)
          || annotateProperty(element, holder)
          || annotateOperator(element, holder)
          || annotateVarDeclaration(element, holder))) {
        annotatePathURL(element, holder);
      }
    } else if (element instanceof CMakeUnquotedArgumentContainer) {
      // Annotate Unquoted argument
      if (!(annotateLegacy(element, holder)
          || annotatePredefinedVariable(element, holder)
          || annotateProperty(element, holder)
          || annotateOperator(element, holder))) {
        annotateVarReferences(element, holder);
        annotatePathURL(element, holder);
      }
    }
  }
}
