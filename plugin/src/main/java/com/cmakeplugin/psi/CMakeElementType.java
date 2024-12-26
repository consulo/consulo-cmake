package com.cmakeplugin.psi;

import com.cmakeplugin.CMakeLanguage;
import consulo.language.ast.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CMakeElementType extends IElementType {
  public CMakeElementType(@NotNull @NonNls String debugName) {
    super(debugName, CMakeLanguage.INSTANCE);
  }
}