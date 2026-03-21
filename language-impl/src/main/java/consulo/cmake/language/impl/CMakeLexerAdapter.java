package consulo.cmake.language.impl;

import consulo.cmake._CMakeLexer;
import consulo.language.lexer.FlexAdapter;

import java.io.Reader;

public class CMakeLexerAdapter extends FlexAdapter {
  public CMakeLexerAdapter() {
    super(new _CMakeLexer((Reader) null));
  }
}
