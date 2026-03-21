package consulo.cmake.importProvider;

import consulo.cmake.localize.CMakeLocalize;
import consulo.cmake.setting.CMakeProjectSettings;
import consulo.configurable.ConfigurationException;
import consulo.disposer.Disposable;
import consulo.externalSystem.service.setting.AbstractExternalProjectSettingsControl;
import consulo.externalSystem.ui.awt.ExternalSystemUiUtil;
import consulo.externalSystem.ui.awt.PaintAwarePanel;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.ui.ex.awt.TextFieldWithBrowseButton;
import jakarta.annotation.Nonnull;

import javax.swing.*;
import java.util.Objects;

/**
 * @author VISTALL
 */
public class CMakeProjectSettingsControl extends AbstractExternalProjectSettingsControl<CMakeProjectSettings> {
    private TextFieldWithBrowseButton myBuildDirectoryField;

    public CMakeProjectSettingsControl(@Nonnull CMakeProjectSettings settings) {
        super(settings);
    }

    @Override
    protected void fillExtraControls(@Nonnull Disposable uiDisposable, @Nonnull PaintAwarePanel content, int indentLevel) {
        myBuildDirectoryField = new TextFieldWithBrowseButton();
        myBuildDirectoryField.addBrowseFolderListener(
            CMakeLocalize.buildDirectoryDialogTitle().get(),
            CMakeLocalize.buildDirectoryDialogDescription().get(),
            null,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        );

        content.add(new JLabel(CMakeLocalize.buildDirectoryLabel().get()), ExternalSystemUiUtil.getLabelConstraints(indentLevel));
        content.add(myBuildDirectoryField, ExternalSystemUiUtil.getFillLineConstraints(indentLevel));
    }

    @Override
    protected boolean isExtraSettingModified() {
        String current = myBuildDirectoryField == null ? null : myBuildDirectoryField.getText().trim();
        return !Objects.equals(current, getInitialSettings().getBuildDirectory());
    }

    @Override
    protected void resetExtraSettings(boolean isDefaultModuleCreation) {
        if (myBuildDirectoryField != null) {
            String buildDir = getInitialSettings().getBuildDirectory();
            if (buildDir == null) {
                String projectPath = getInitialSettings().getExternalProjectPath();
                if (projectPath != null) {
                    java.io.File f = new java.io.File(projectPath);
                    String dir = f.isDirectory() ? f.getAbsolutePath() : f.getParent();
                    buildDir = dir + "/build";
                } else {
                    buildDir = "";
                }
            }
            myBuildDirectoryField.setText(buildDir);
        }
    }

    @Override
    protected void applyExtraSettings(@Nonnull CMakeProjectSettings settings) {
        if (myBuildDirectoryField != null) {
            String text = myBuildDirectoryField.getText().trim();
            settings.setBuildDirectory(text.isEmpty() ? null : text);
        }
    }

    @Override
    public boolean validate(@Nonnull CMakeProjectSettings settings) throws ConfigurationException {
        String buildDir = myBuildDirectoryField == null ? null : myBuildDirectoryField.getText().trim();
        if (buildDir == null || buildDir.isEmpty()) {
            throw new ConfigurationException(CMakeLocalize.buildDirectoryRequired().get());
        }
        return true;
    }
}
