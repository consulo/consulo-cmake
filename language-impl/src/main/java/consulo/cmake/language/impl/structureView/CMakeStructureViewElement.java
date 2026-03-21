package consulo.cmake.language.impl.structureView;

import consulo.cmake.utils.CMakeIFWHILEcheck;
import consulo.cmake.utils.CMakePSITreeSearch;
import consulo.cmake.utils.CMakeVarStringUtil;
import consulo.colorScheme.TextAttributesKey;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.fileEditor.structureView.tree.SortableTreeElement;
import consulo.fileEditor.structureView.tree.TreeElement;
import consulo.language.psi.NavigatablePsiElement;
import consulo.navigation.ItemPresentation;
import consulo.ui.ex.tree.PresentationData;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;

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

