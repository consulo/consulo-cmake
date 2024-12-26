package consulo.cmake.utils;

import consulo.cmake.CMakeFileType;
import com.cmakeplugin.psi.*;
import com.cmakeplugin.psi.impl.CMakeFunDefImpl;
import com.cmakeplugin.psi.impl.CMakeMacroDefImpl;
import consulo.language.psi.NavigatablePsiElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.virtualFileSystem.fileType.FileType;

@Deprecated
public class CMakePDC {

  public static final Class<? extends NavigatablePsiElement> MACRO_CLASS =  CMakeMacroDefImpl.class;

  public static final Class<? extends NavigatablePsiElement> FUNCTION_CLASS = CMakeFunDefImpl.class;

  @SuppressWarnings("unchecked")
  public static final Class<? extends PsiElement>[] FUN_MACRO_END_CLASSES = new Class[] {CMakeFend.class, CMakeMend.class};

  public static final Class<? extends PsiElement> ARGUMENTS_CLASS = CMakeArguments.class;

  public static final Class<? extends PsiElement> COMMAND_NAME_CLASS = CMakeCommandName.class;

  public static final Class<? extends PsiElement> COMMAND_CLASS = CMakeCmd.class;

  @SuppressWarnings("unchecked")
  public static final Class<? extends PsiElement>[] VARDEF_ARGUMENT_CLASSES = new Class[] {CMakeUnquotedArgumentMaybeVariableContainer.class};

  @SuppressWarnings("unchecked")
  public static final Class<? extends PsiElement>[] FUN_MACRO_ARGUMENT_CLASSES = new Class[] {
            CMakeUnquotedArgumentContainer.class, CMakeUnquotedArgumentMaybeVariableContainer.class
          };

  @SuppressWarnings("unchecked")
  public static final Class<? extends PsiElement>[] COMMAND_ARGUMENT_CLASSES = new Class[] {
            CMakeUnquotedArgumentContainer.class,
            CMakeUnquotedArgumentMaybeVariableContainer.class,
            CMakeQuotedArgumentContainer.class,
            CMakeBracketArgumentContainer.class
          };

  public static PsiElement transformToLiteral (PsiElement cmakeArgument) {
    return cmakeArgument;
  }

  public static boolean isClassOfVarRefInsideIfWhile(PsiElement element) {
    return (element instanceof CMakeUnquotedArgumentContainer);
  }

  public static boolean isClassOfVarDef(PsiElement element) {
    return (element instanceof CMakeUnquotedArgumentMaybeVariableContainer);
  }

  @SuppressWarnings("unchecked")
  private static final Class<? extends PsiElement>[] IF_WHILE_CLASSES =
      new Class[]{
          CMakeIfExpr.class,
          CMakeElseifExpr.class,
          CMakeElseExpr.class,
          CMakeEndifExpr.class,
          CMakeWhilebegin.class,
          CMakeWhileend.class
      };

  static boolean isIfWhileConditionArgument(PsiElement element) {
    PsiElement condCommandName = PsiTreeUtil.getParentOfType(element, IF_WHILE_CLASSES);
    if (condCommandName == null) return false;

    return true;
  }

  static FileType getCmakeFileType() {
    return CMakeFileType.INSTANCE;
  }

  public static boolean classCanHoldVarRef(PsiElement element) {
    return element instanceof CMakeUnquotedArgumentContainer
        || element instanceof CMakeQuotedArgumentContainer;
  }

}
