package consulo.cmake.language.impl.structureView;

import consulo.cmake.language.impl.CMakeSyntaxHighlighter;
import consulo.cmake.utils.CMakePlusPDC;
import consulo.language.psi.NavigatablePsiElement;

class FunctionElement extends FunMacroBase {

  FunctionElement(NavigatablePsiElement element) {
    super(element);
    icon = CMakePlusPDC.ICON_CMAKE_FUNCTION;
    attributesKey = CMakeSyntaxHighlighter.FUNCTION;
  }
}
