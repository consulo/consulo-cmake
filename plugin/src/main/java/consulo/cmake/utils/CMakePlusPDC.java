package consulo.cmake.utils;

import consulo.cmake.CMakeLanguage;
import com.cmakeplugin.psi.*;
import com.cmakeplugin.psi.impl.CMakeUnquotedArgumentMaybeVariableContainerImpl;
import consulo.cmake.psi.CMakeFile;
import consulo.cmake.psi.CMakePsiElementFactory;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.ast.TokenSet;
import consulo.language.psi.*;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;

import java.util.Objects;

@Deprecated
public class CMakePlusPDC {

    @Nonnull
    @SuppressWarnings("unchecked")
    public static final Class<? extends PsiElement>[] FOLDABLE_BODIES = new Class[]{
        CMakeFunbody.class,
        CMakeMacrobody.class,
        CMakeIfbody.class,
        CMakeForbody.class,
        CMakeWhilebody.class,
        CMakeArguments.class,
        PsiComment.class
    };

    public static boolean isLineComment(PsiComment comment) {
        return comment.getNode().getElementType() == CMakeTypes.LINE_COMMENT;
    }

    public static boolean isSubsequentLineCommentsGlueElement(PsiElement element) {
        return element instanceof PsiWhiteSpace
            // accept only one caret-return inside subsequent comments
            && element.getText().split("\n", 3).length == 2;
    }

    public static TextRange getBodyBlockRangeToFold(PsiElement element) {
        TextRange range = element.getTextRange();
        return range;
    }

    public static final Class<? extends PsiFile> CMAKE_FILE_CLASS =
        CMakeFile.class;

    public static final Class<? extends NavigatablePsiElement> VARDEF_CLASS =
        CMakeUnquotedArgumentMaybeVariableContainerImpl.class;

    public static final TokenSet VARDEF_ELEMENT_TYPES =
        TokenSet.create(
            CMakeTypes.UNQUOTED_ARGUMENT_MAYBE_VAR_DEF, CMakeTypes.UNQUOTED_ARGUMENT);

    public static final TokenSet VARREF_ELEMENT_TYPES =
        TokenSet.create(CMakeTypes.UNQUOTED_ARGUMENT, CMakeTypes.QUOTED_ARGUMENT);

    public static final TokenSet COMMAND_KEYWORD_ELEMENT_TYPES =
        TokenSet.create(
            CMakeTypes.IF,
            CMakeTypes.ELSE,
            CMakeTypes.ELSEIF,
            CMakeTypes.ENDIF,
            CMakeTypes.FOREACH,
            CMakeTypes.ENDFOREACH,
            CMakeTypes.WHILE,
            CMakeTypes.ENDWHILE,
            CMakeTypes.FUNCTION,
            CMakeTypes.ENDFUNCTION,
            CMakeTypes.MACRO,
            CMakeTypes.ENDMACRO);

    public static final TokenSet END_OF_COMMAND_KEYWORD_ELEMENT_TYPES =
        TokenSet.create(
            CMakeTypes.ELSE_EXPR,
            CMakeTypes.ENDIF_EXPR,
            CMakeTypes.FOREND,
            CMakeTypes.WHILEEND,
            CMakeTypes.FEND,
            CMakeTypes.MEND);

    public static Language getLanguageInstance() {
        return CMakeLanguage.INSTANCE;
    }

    public static final Image ICON_CMAKE_MACRO = PlatformIconGroup.nodesMethod();

    public static final Image ICON_CMAKE_FUNCTION = PlatformIconGroup.nodesFunction();

    public static final Image ICON_VAR = PlatformIconGroup.nodesVariable();

    @Nonnull
    public static PsiElement createCommandName(
        @Nonnull Project project, @Nonnull String newCommandName) {
        return CMakePsiElementFactory.createCommandName(project, newCommandName);
    }

    @Nonnull
    public static PsiElement createEmptyArguments(@Nonnull Project project) {
        PsiElement result;
        String text = "if() \n endif()";
        PsiFile tempFile =
            CMakePsiElementFactory.createFile(project, text);
        result = PsiTreeUtil.findChildOfType(tempFile, CMakePDC.ARGUMENTS_CLASS);
        return Objects.requireNonNull(result);
    }
}
