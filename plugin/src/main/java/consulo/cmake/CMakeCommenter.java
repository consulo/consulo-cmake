package consulo.cmake;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Commenter;
import consulo.language.Language;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class CMakeCommenter implements Commenter {

    @Nullable
    @Override
    public String getLineCommentPrefix() {
        return "#";
    }

    @Nullable
    @Override
    public String getBlockCommentPrefix() {
        return "#[[";
    }

    @Nullable
    @Override
    public String getBlockCommentSuffix() {
        return "]]";
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentPrefix() {
        return null;
    }

    @Nullable
    @Override
    public String getCommentedBlockCommentSuffix() {
        return null;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return CMakeLanguage.INSTANCE;
    }
}
