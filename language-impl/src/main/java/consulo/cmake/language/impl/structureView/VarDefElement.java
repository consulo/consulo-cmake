package consulo.cmake.language.impl.structureView;

import consulo.cmake.language.impl.CMakeSyntaxHighlighter;
import consulo.cmake.utils.CMakeVarStringUtil;
import consulo.language.psi.NavigatablePsiElement;
import consulo.platform.base.icon.PlatformIconGroup;

class VarDefElement extends CMakeStructureViewElement {

  VarDefElement(NavigatablePsiElement element) {
    super(element);
    if (presentation != null
        && (notesText = presentation.getPresentableText()) != null
        && notesText.length() > 20) notesText = notesText.substring(20);
    icon = PlatformIconGroup.nodesVariable();
    attributesKey =
        CMakeVarStringUtil.isPredefinedCMakeVar(element.getText())
            ? CMakeSyntaxHighlighter.CMAKE_VAR_DEF
            : CMakeSyntaxHighlighter.VAR_DEF;
  }
}
