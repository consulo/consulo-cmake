package consulo.cmake;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.BracePair;
import consulo.language.Language;
import consulo.language.PairedBraceMatcher;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import static com.cmakeplugin.psi.CMakeTypes.*;

@ExtensionImpl
public class CMakeBraceMatcher implements PairedBraceMatcher {

    private static BracePair[] PAIRS;

    @Override
    public BracePair[] getPairs() {
        if (PAIRS == null) {
            PAIRS =
                new BracePair[]{
                    new BracePair(LPAR, RPAR, false),
                    new BracePair(IF, ENDIF, false),
                    new BracePair(FOREACH, ENDFOREACH, false),
                    new BracePair(WHILE, ENDWHILE, false),
                    new BracePair(FUNCTION, ENDFUNCTION, false),
                    new BracePair(MACRO, ENDMACRO, false),
                    //          new BracePair(, , false),
                };
        }
        return PAIRS;
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(
        @Nonnull IElementType lbraceType, @Nullable IElementType contextType) {
        return true;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return CMakeLanguage.INSTANCE;
    }
}
