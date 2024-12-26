package com.cmakeplugin;

import com.cmakeplugin.psi.CMakeUnquotedArgumentMaybeVariableContainer;
import consulo.language.findUsage.EmptyFindUsagesProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;

public class CMakeFindUsagesProvider extends EmptyFindUsagesProvider {

  @Override
  public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
    return psiElement instanceof PsiNamedElement;
  }

  @NotNull
  @Override
  public String getType(@NotNull PsiElement psiElement) {
    String result = "";
    if (psiElement instanceof CMakeUnquotedArgumentMaybeVariableContainer) result = "variable";
    return result;
  }
}
