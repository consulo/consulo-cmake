package consulo.cmake.language.impl.structureView;

import consulo.cmake.utils.CMakePSITreeSearch;
import consulo.cmake.utils.CMakePlusPDC;
import consulo.fileEditor.structureView.tree.TreeElement;
import consulo.language.psi.NavigatablePsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import jakarta.annotation.Nonnull;

abstract class FunMacroBase extends CMakeStructureViewElement {

  FunMacroBase(NavigatablePsiElement element) {
    super(element);
    presentableText = CMakePSITreeSearch.getFunMacroName(element);
    notesText = CMakePSITreeSearch.getFunMacroArgs(element);
  }

  @Override
  public void navigate(boolean requestFocus) {
    NavigatablePsiElement name = CMakePSITreeSearch.getFunMacroNameElement(element);
    name = (name != null) ? name : element;
    name.navigate(requestFocus);
  }

  @Override
  @Nonnull
  public TreeElement[] getChildren() {
    return PsiTreeUtil.findChildrenOfAnyType(element, CMakePlusPDC.VARDEF_CLASS).stream()
        .filter(this::isVarDef)
        .map(VarDefElement::new)
        .toArray(TreeElement[]::new);
  }
}
