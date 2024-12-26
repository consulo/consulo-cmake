package com.cmakeplugin;

import consulo.language.editor.refactoring.RefactoringSupportProvider;
import consulo.language.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CMakeRefactoringSupportProvider extends RefactoringSupportProvider {
    @Override
    public boolean isMemberInplaceRenameAvailable(@NotNull PsiElement element, @Nullable PsiElement context) {

    return true//element instanceof CMakeVariableDeclaration
//            && element.getReference()!=null
            ;  //fixme
  }
}
