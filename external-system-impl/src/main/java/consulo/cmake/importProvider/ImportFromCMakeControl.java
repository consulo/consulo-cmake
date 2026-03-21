package consulo.cmake.importProvider;

import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.cmake.setting.CMakeProjectSettings;
import consulo.cmake.setting.CMakeSettings;
import consulo.cmake.setting.CMakeSettingsListener;
import consulo.externalSystem.service.execution.ExternalSystemSettingsControl;
import consulo.externalSystem.service.setting.AbstractImportFromExternalSystemControl;
import consulo.project.ProjectManager;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 */
public class ImportFromCMakeControl
    extends AbstractImportFromExternalSystemControl<CMakeProjectSettings, CMakeSettingsListener, CMakeSettings> {

    public ImportFromCMakeControl() {
        super(
            CMakeConstants.SYSTEM_ID,
            new CMakeSettings(ProjectManager.getInstance().getDefaultProject()),
            new CMakeProjectSettings()
        );
    }

    @Nonnull
    @Override
    protected ExternalSystemSettingsControl<CMakeProjectSettings> createProjectSettingsControl(@Nonnull CMakeProjectSettings settings) {
        return new CMakeProjectSettingsControl(settings);
    }

    @Nullable
    @Override
    protected ExternalSystemSettingsControl<CMakeSettings> createSystemSettingsControl(@Nonnull CMakeSettings settings) {
        return null;
    }

    @Override
    public void onLinkedProjectPathChange(@Nonnull String path) {
    }
}
