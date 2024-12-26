package com.cmakeplugin.psi;

import com.cmakeplugin.CMakeLanguage;
import consulo.language.ast.IElementType;

public class CMakeTokenType extends IElementType {
  public CMakeTokenType(String debugName) {
    super(debugName, CMakeLanguage.INSTANCE);
  }

  @Override
  public String toString() {
    return "CMakeTokenType." + super.toString();
  }
}