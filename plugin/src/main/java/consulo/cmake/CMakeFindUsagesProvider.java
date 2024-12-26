package consulo.cmake;

import com.cmakeplugin.psi.CMakeUnquotedArgumentMaybeVariableContainer;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.findUsage.EmptyFindUsagesProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import jakarta.annotation.Nonnull;

@ExtensionImpl
public class CMakeFindUsagesProvider extends EmptyFindUsagesProvider {
    @Nonnull
    @Override
    public Language getLanguage() {
        return CMakeLanguage.INSTANCE;
    }

    @Override
    public boolean canFindUsagesFor(@Nonnull PsiElement psiElement) {
        return psiElement instanceof PsiNamedElement;
    }

    @Nonnull
    @Override
    public String getType(@Nonnull PsiElement psiElement) {
        String result = "";
        if (psiElement instanceof CMakeUnquotedArgumentMaybeVariableContainer) {
            result = "variable";
        }
        return result;
    }
}
