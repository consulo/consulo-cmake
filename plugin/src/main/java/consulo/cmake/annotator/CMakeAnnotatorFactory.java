package consulo.cmake.annotator;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.dumb.DumbAware;
import consulo.cmake.CMakeLanguage;
import consulo.language.Language;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.AnnotatorFactory;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2024-12-26
 */
@ExtensionImpl
public class CMakeAnnotatorFactory implements AnnotatorFactory, DumbAware {
    @Nullable
    @Override
    public Annotator createAnnotator() {
        return new CMakeAnnotator();
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return CMakeLanguage.INSTANCE;
    }
}
