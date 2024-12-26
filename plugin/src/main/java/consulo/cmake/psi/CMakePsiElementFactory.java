package consulo.cmake.psi;

import consulo.cmake.CMakeLanguage;
import com.cmakeplugin.psi.CMakeCommandName;
import consulo.cmake.psi.impl.CMakePsiImplUtil;
import consulo.cmake.utils.CMakeIFWHILEcheck;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import jakarta.annotation.Nonnull;

import java.util.Objects;

public class CMakePsiElementFactory {

  public static PsiFile createFile(@Nonnull Project project, @Nonnull String text) {
    return PsiFileFactory.getInstance(project)
            .createFileFromText("a.cmake", CMakeLanguage.INSTANCE, text, false, false);
  }

  public static PsiElement createArgumentFromText(@Nonnull PsiElement element,
                                                  @Nonnull String text,
                                                  @Nonnull final Class<? extends PsiElement> aClass) {
    String fileText = CMakeIFWHILEcheck.isVarRefInsideIFWHILE(element)
            ? "if(" + text + ") endif()"
            : "set(" + text + ")";
    return CMakePsiImplUtil.computeElementsOfClass( createFile( element.getProject(), fileText), aClass)
            .get(0).getFirstChild();
  }

  @Nonnull
  public static CMakeCommandName createCommandName(@Nonnull Project project, @Nonnull String newCommandName) {
    PsiFile tempFile = createFile(project, newCommandName + "()\n");
    CMakeCommandName commandName = PsiTreeUtil.findChildOfType(tempFile, CMakeCommandName.class);
    return Objects.requireNonNull(commandName);
  }

}
