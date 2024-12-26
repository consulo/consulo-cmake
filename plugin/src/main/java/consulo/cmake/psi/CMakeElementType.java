package consulo.cmake.psi;

import consulo.cmake.CMakeLanguage;
import consulo.language.ast.IElementType;
import org.jetbrains.annotations.NonNls;
import jakarta.annotation.Nonnull;

public class CMakeElementType extends IElementType {
  public CMakeElementType(@Nonnull @NonNls String debugName) {
    super(debugName, CMakeLanguage.INSTANCE);
  }
}