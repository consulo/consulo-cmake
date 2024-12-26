package consulo.cmake;

import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class CMakeFileType extends LanguageFileType {
    public static final CMakeFileType INSTANCE = new CMakeFileType();

    private CMakeFileType() {
        super(CMakeLanguage.INSTANCE);
    }

    @Nonnull
    @Override
    public String getId() {
        return "CMAKE";
    }

    @Nonnull
    @Override
    public LocalizeValue getDescription() {
        return LocalizeValue.localizeTODO("CMake language file");
    }

    @Nonnull
    @Override
    public String getDefaultExtension() {
        return "cmake";
    }

    @Nullable
    @Override
    public Image getIcon() {
        return CMakeIcons.FILE;
    }
}