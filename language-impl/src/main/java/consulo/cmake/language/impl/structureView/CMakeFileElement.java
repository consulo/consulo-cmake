package consulo.cmake.language.impl.structureView;

import consulo.cmake.utils.CMakePDC;
import consulo.cmake.utils.CMakePlusPDC;
import consulo.fileEditor.structureView.tree.TreeElement;
import consulo.language.psi.NavigatablePsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.navigation.ItemPresentation;
import jakarta.annotation.Nonnull;

import java.util.stream.Stream;

class CMakeFileElement extends CMakeStructureViewElement {

  CMakeFileElement(PsiFile element) {
    super(element);
  }

  @Nonnull
  @Override
  public ItemPresentation getPresentation() {
    if (CMakePlusPDC.CMAKE_FILE_CLASS.isInstance(element) && presentation != null)
      return presentation;
    return super.getPresentation();
  }

  @Override
  @Nonnull
  public TreeElement[] getChildren() {
    if (!CMakePlusPDC.CMAKE_FILE_CLASS.isInstance(element)) return super.getChildren();

    Stream<TreeElement> macroElements =
        PsiTreeUtil.findChildrenOfAnyType(element, CMakePDC.MACRO_CLASS).stream()
            .map(MacroElement::new);

    Stream<TreeElement> functionElements =
        PsiTreeUtil.findChildrenOfAnyType(element, CMakePDC.FUNCTION_CLASS).stream()
            .map(FunctionElement::new);

    Stream<TreeElement> varDefElements =
        PsiTreeUtil.findChildrenOfAnyType(element, CMakePlusPDC.VARDEF_CLASS).stream()
            .filter(this::isVarDef)
            .filter(this::hasNoFunMacroParent)
            .map(VarDefElement::new);

    return Stream.of(functionElements, macroElements, varDefElements)
        .flatMap(s -> s)
        .toArray(TreeElement[]::new);
  }

  private boolean hasNoFunMacroParent(NavigatablePsiElement varDef) {
    return PsiTreeUtil.getParentOfType(varDef, CMakePDC.FUNCTION_CLASS, CMakePDC.MACRO_CLASS)
        == null;
  }
}
