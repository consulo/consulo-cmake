package com.cmakeplugin;

import consulo.language.file.LanguageFileType;
import consulo.ui.image.Image;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CMakeFileType extends LanguageFileType {
    public static final CMakeFileType INSTANCE = new CMakeFileType();
    private static final String[] DEFAULT_EXTENSIONS = {"cmake", "txt"};

    private CMakeFileType() {
        super(CMakeLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getId() {
        return "CMAKE";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "CMake language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return DEFAULT_EXTENSIONS[0];
    }

    @Nullable
    @Override
    public Image getIcon() {
        return CMakeIcons.FILE;
    }
}