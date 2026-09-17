package consulo.cmake.importProvider;

import consulo.cmake.localize.CMakeLocalize;
import consulo.cmake.setting.CMakeProjectSettings;
import consulo.configurable.ConfigurationException;
import consulo.disposer.Disposable;
import consulo.externalSystem.service.setting.AbstractExternalProjectSettingsConfigurable;
import consulo.externalSystem.service.setting.ExternalSystemSettingsPlace;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.LabeledBuilder;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Objects;

/**
 * @author VISTALL
 */
public class CMakeProjectSettingsConfigurable extends AbstractExternalProjectSettingsConfigurable<CMakeProjectSettings> {
    private FileChooserTextBoxBuilder.@Nullable Controller myBuildDirectoryBox;

    public CMakeProjectSettingsConfigurable(CMakeProjectSettings settings, ExternalSystemSettingsPlace place) {
        super(settings, place);
    }

    @Override
    @RequiredUIAccess
    protected Component createExtraUIComponent(Disposable uiDisposable) {
        FileChooserTextBoxBuilder.Controller buildDirectoryBox = FileChooserTextBoxBuilder.create(null)
            .uiDisposable(uiDisposable)
            .dialogTitle(CMakeLocalize.buildDirectoryDialogTitle())
            .dialogDescription(CMakeLocalize.buildDirectoryDialogDescription())
            .fileChooserDescriptor(FileChooserDescriptorFactory.createSingleFolderDescriptor())
            .build();
        myBuildDirectoryBox = buildDirectoryBox;

        buildDirectoryBox.setValue(getBuildDirectoryOrDefault());

        return LabeledBuilder.filled(CMakeLocalize.buildDirectoryLabel(), buildDirectoryBox);
    }

    private String getBuildDirectoryOrDefault() {
        CMakeProjectSettings settings = getSettings();

        String buildDirectory = settings.getBuildDirectory();
        if (buildDirectory != null) {
            return buildDirectory;
        }

        String projectPath = settings.getExternalProjectPath();
        if (projectPath == null) {
            return "";
        }

        File file = new File(projectPath);
        return (file.isDirectory() ? file.getAbsolutePath() : file.getParent()) + "/build";
    }

    @Override
    @RequiredUIAccess
    protected boolean isExtraModified() {
        String current = myBuildDirectoryBox == null ? null : myBuildDirectoryBox.getValue().trim();
        return !Objects.equals(current, getSettings().getBuildDirectory());
    }

    @Override
    @RequiredUIAccess
    protected void applyExtra() throws ConfigurationException {
        if (myBuildDirectoryBox == null) {
            return;
        }

        String buildDirectory = myBuildDirectoryBox.getValue().trim();
        if (buildDirectory.isEmpty()) {
            throw new ConfigurationException(CMakeLocalize.buildDirectoryRequired());
        }

        getSettings().setBuildDirectory(buildDirectory);
    }
}
