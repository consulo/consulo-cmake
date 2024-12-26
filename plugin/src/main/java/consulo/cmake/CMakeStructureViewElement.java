package consulo.cmake;

import consulo.cmake.utils.*;
import consulo.colorScheme.TextAttributesKey;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.fileEditor.structureView.tree.SortableTreeElement;
import consulo.fileEditor.structureView.tree.TreeElement;
import consulo.language.psi.NavigatablePsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.navigation.ItemPresentation;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.ex.tree.PresentationData;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;

import java.util.stream.Stream;

public abstract class CMakeStructureViewElement
    implements StructureViewTreeElement, SortableTreeElement {
  NavigatablePsiElement element;
  ItemPresentation presentation;
  String presentableText;
  String notesText = null;
  Image icon = null;
  TextAttributesKey attributesKey = null;

  CMakeStructureViewElement(NavigatablePsiElement element) {
    this.element = element;
    this.presentation = element.getPresentation();
    this.presentableText = element.getText();
  }

  @Override
  public Object getValue() {
    return element;
  }

  @Override
  public void navigate(boolean requestFocus) {
    element.navigate(requestFocus);
  }

  @Override
  public boolean canNavigate() {
    return element.canNavigate();
  }

  @Override
  public boolean canNavigateToSource() {
    return element.canNavigateToSource();
  }

  @Nonnull
  @Override
  public String getAlphaSortKey() {
    return presentableText != null ? presentableText : "";
  }

  @Nonnull
  @Override
  public ItemPresentation getPresentation() {
    return new PresentationData(presentableText, notesText, icon, attributesKey);
  }

  @Override
  @Nonnull
  public TreeElement[] getChildren() {
    return EMPTY_ARRAY;
  }

  boolean isVarDef(NavigatablePsiElement element) {
    return CMakePSITreeSearch.existReferenceTo(element)
        || (CMakeIFWHILEcheck.couldBeVarDef(element)
        && CMakeVarStringUtil.isPredefinedCMakeVar(element.getText()));
  }
}

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

class MacroElement extends FunMacroBase {

  MacroElement(NavigatablePsiElement element) {
    super(element);
    icon = CMakePlusPDC.ICON_CMAKE_MACRO;
    attributesKey = CMakeSyntaxHighlighter.MACROS;
  }
}

class FunctionElement extends FunMacroBase {

  FunctionElement(NavigatablePsiElement element) {
    super(element);
    icon = CMakePlusPDC.ICON_CMAKE_FUNCTION;
    attributesKey = CMakeSyntaxHighlighter.FUNCTION;
  }
}

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
