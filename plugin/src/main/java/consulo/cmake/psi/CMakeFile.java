package consulo.cmake.psi;

import consulo.cmake.CMakeFileType;
import consulo.cmake.CMakeLanguage;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.PsiFileBase;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nonnull;

public class CMakeFile extends PsiFileBase {
  public CMakeFile(@Nonnull FileViewProvider viewProvider) {
    super(viewProvider, CMakeLanguage.INSTANCE);
  }

  @Nonnull
  @Override
  public FileType getFileType() {
    return CMakeFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "CMake File";
  }
}