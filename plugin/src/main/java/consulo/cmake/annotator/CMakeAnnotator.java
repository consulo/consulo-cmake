package consulo.cmake.annotator;

import com.cmakeplugin.psi.*;
import consulo.cmake.utils.CMakePDC;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;

public class CMakeAnnotator implements Annotator {

  @Override
  public void annotate(@Nonnull final PsiElement element, @Nonnull AnnotationHolder holder) {
      annotateIdea(element, holder);
  }

  private void annotateIdea(@Nonnull final PsiElement element, @Nonnull AnnotationHolder holder) {
    if (element instanceof CMakeCommandName) {
      CMakeAnnotatorUtils.annotateCommand(element, holder);
    } else if (CMakePDC.FUNCTION_CLASS.isInstance(element)) {
      CMakeAnnotatorUtils.annotateFunctionName(element, holder);
    } else if (CMakePDC.MACRO_CLASS.isInstance(element)) {
      CMakeAnnotatorUtils.annotateMacrosName(element, holder);
    } else if (element instanceof CMakeQuotedArgumentContainer) {
      // Annotate Quoted argument
      assert element.getPrevSibling() instanceof CMakeBrace
          && element.getNextSibling() instanceof CMakeBrace;
      CMakeAnnotatorUtils.annotateVarReferences(element, holder);
    } else if (element instanceof CMakeUnquotedArgumentMaybeVariableContainer) {
      // Annotate Unquoted argument with possible Var declaration
      if (!(CMakeAnnotatorUtils.annotatePredefinedVariable(element, holder)
          || CMakeAnnotatorUtils.annotateProperty(element, holder)
          || CMakeAnnotatorUtils.annotateOperator(element, holder)
          || CMakeAnnotatorUtils.annotateVarDeclaration(element, holder))) {
        CMakeAnnotatorUtils.annotatePathURL(element, holder);
      }
    } else if (element instanceof CMakeUnquotedArgumentContainer) {
      // Annotate Unquoted argument
      if (!(CMakeAnnotatorUtils.annotateLegacy(element, holder)
          || CMakeAnnotatorUtils.annotatePredefinedVariable(element, holder)
          || CMakeAnnotatorUtils.annotateProperty(element, holder)
          || CMakeAnnotatorUtils.annotateOperator(element, holder))) {
        CMakeAnnotatorUtils.annotateVarReferences(element, holder);
        CMakeAnnotatorUtils.annotatePathURL(element, holder);
      }
    }
  }
}
