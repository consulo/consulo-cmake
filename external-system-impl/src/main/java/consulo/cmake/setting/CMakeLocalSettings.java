package consulo.cmake.setting;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.externalSystem.setting.AbstractExternalSystemLocalSettings;
import consulo.application.Application;
import consulo.project.Project;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 */
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
@State(name = "CMakeLocalSettings", storages = @Storage(file = StoragePathMacros.WORKSPACE_FILE))
public class CMakeLocalSettings extends AbstractExternalSystemLocalSettings
    implements PersistentStateComponent<AbstractExternalSystemLocalSettings.State> {

    @Inject
    public CMakeLocalSettings(@Nonnull Project project) {
        super(CMakeConstants.SYSTEM_ID, project);
    }

    @Nonnull
    public static CMakeLocalSettings getInstance(@Nonnull Project project) {
        return project.getInstance(CMakeLocalSettings.class);
    }

    @Nullable
    @Override
    public State getState() {
        State state = new State();
        fillState(state);
        return state;
    }

    @Override
    public void loadState(@Nonnull State state) {
        super.loadState(state);
    }
}
