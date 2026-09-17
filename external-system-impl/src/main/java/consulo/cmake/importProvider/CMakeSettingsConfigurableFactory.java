package consulo.cmake.importProvider;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.cmake.setting.CMakeProjectSettings;
import consulo.cmake.setting.CMakeSettings;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.setting.AbstractExternalProjectSettingsConfigurable;
import consulo.externalSystem.service.setting.ExternalSystemSettingsConfigurableFactory;
import consulo.externalSystem.service.setting.ExternalSystemSettingsPlace;
import consulo.project.ProjectManager;
import consulo.ui.annotation.RequiredUIAccess;

/**
 * @author VISTALL
 */
@ExtensionImpl
public class CMakeSettingsConfigurableFactory implements ExternalSystemSettingsConfigurableFactory<CMakeProjectSettings, CMakeSettings> {
    @Override
    public ProjectSystemId getSystemId() {
        return CMakeConstants.SYSTEM_ID;
    }

    @Override
    public CMakeProjectSettings createProjectSettings() {
        return new CMakeProjectSettings();
    }

    @Override
    public CMakeSettings createSystemSettings() {
        return new CMakeSettings(ProjectManager.getInstance().getDefaultProject());
    }

    @Override
    @RequiredUIAccess
    public AbstractExternalProjectSettingsConfigurable<CMakeProjectSettings> createProjectSettingsConfigurable(
        CMakeProjectSettings settings,
        ExternalSystemSettingsPlace place
    ) {
        return new CMakeProjectSettingsConfigurable(settings, place);
    }
}
