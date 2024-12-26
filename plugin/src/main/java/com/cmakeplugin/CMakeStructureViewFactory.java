package com.cmakeplugin;

import consulo.codeEditor.Editor;
import consulo.fileEditor.structureView.StructureViewBuilder;
import consulo.fileEditor.structureView.StructureViewModel;
import consulo.fileEditor.structureView.TreeBasedStructureViewBuilder;
import consulo.language.editor.structureView.PsiStructureViewFactory;
import consulo.language.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CMakeStructureViewFactory implements PsiStructureViewFactory {

  @Nullable
  @Override
  public StructureViewBuilder getStructureViewBuilder(@NotNull PsiFile psiFile) {
    return new TreeBasedStructureViewBuilder() {
      @NotNull
      @Override
      public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
        return new CMakeStructureViewModel(psiFile, editor);
      }
    };
  }
}
