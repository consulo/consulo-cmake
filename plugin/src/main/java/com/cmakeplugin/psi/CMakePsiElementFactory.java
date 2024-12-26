package com.cmakeplugin.psi;

import com.cmakeplugin.CMakeLanguage;
import com.cmakeplugin.psi.impl.CMakePsiImplUtil;
import com.cmakeplugin.utils.CMakeIFWHILEcheck;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CMakePsiElementFactory {

  public static PsiFile createFile(@NotNull Project project, @NotNull String text) {
    return PsiFileFactory.getInstance(project)
            .createFileFromText("a.cmake", CMakeLanguage.INSTANCE, text, false, false);
  }

  public static PsiElement createArgumentFromText(@NotNull PsiElement element,
                                                  @NotNull String text,
                                                  @NotNull final Class<? extends PsiElement> aClass) {
    String fileText = CMakeIFWHILEcheck.isVarRefInsideIFWHILE(element)
            ? "if(" + text + ") endif()"
            : "set(" + text + ")";
    return CMakePsiImplUtil.computeElementsOfClass( createFile( element.getProject(), fileText), aClass)
            .get(0).getFirstChild();
  }

  @NotNull
  public static CMakeCommandName createCommandName(@NotNull Project project, @NotNull String newCommandName) {
    PsiFile tempFile = createFile(project, newCommandName + "()\n");
    CMakeCommandName commandName = PsiTreeUtil.findChildOfType(tempFile, CMakeCommandName.class);
    return Objects.requireNonNull(commandName);
  }

}
