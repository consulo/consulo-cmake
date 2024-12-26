package com.cmakeplugin.psi;

import com.cmakeplugin.CMakeFileType;
import com.cmakeplugin.CMakeLanguage;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.PsiFileBase;
import consulo.virtualFileSystem.fileType.FileType;
import org.jetbrains.annotations.NotNull;

public class CMakeFile extends PsiFileBase {
  public CMakeFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, CMakeLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return CMakeFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "CMake File";
  }
}