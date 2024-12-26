package consulo.cmake;

import consulo.language.Language;

public class CMakeLanguage extends Language {
  public static final CMakeLanguage INSTANCE = new CMakeLanguage();

  private CMakeLanguage() {
    super("CMake");
  }
}
