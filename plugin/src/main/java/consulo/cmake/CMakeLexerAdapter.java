package consulo.cmake;

import consulo.language.lexer.FlexAdapter;

import java.io.Reader;

public class CMakeLexerAdapter extends FlexAdapter {
  public CMakeLexerAdapter() {
    super(new _CMakeLexer((Reader) null));
  }
}
