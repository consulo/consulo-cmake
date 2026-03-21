package consulo.cmake.setting;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.TopicAPI;
import consulo.externalSystem.setting.ExternalSystemSettingsListener;

import java.util.Collection;
import java.util.Set;

/**
 * @author VISTALL
 */
@TopicAPI(ComponentScope.PROJECT)
public interface CMakeSettingsListener extends ExternalSystemSettingsListener<CMakeProjectSettings> {
    Class<CMakeSettingsListener> TOPIC = CMakeSettingsListener.class;

    @Override
    default void onProjectRenamed(String oldName, String newName) {
    }

    @Override
    default void onProjectsLinked(Collection<CMakeProjectSettings> settings) {
    }

    @Override
    default void onProjectsUnlinked(Set<String> linkedProjectPaths) {
    }

    @Override
    default void onUseAutoImportChange(boolean currentValue, String linkedProjectPath) {
    }

    @Override
    default void onBulkChangeStart() {
    }

    @Override
    default void onBulkChangeEnd() {
    }
}
