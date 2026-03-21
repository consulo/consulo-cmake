package consulo.cmake.language.impl.structureView;

import consulo.cmake.language.impl.CMakeSyntaxHighlighter;
import consulo.cmake.utils.CMakePlusPDC;
import consulo.language.psi.NavigatablePsiElement;

class MacroElement extends FunMacroBase {

  MacroElement(NavigatablePsiElement element) {
    super(element);
    icon = CMakePlusPDC.ICON_CMAKE_MACRO;
    attributesKey = CMakeSyntaxHighlighter.MACROS;
  }
}
