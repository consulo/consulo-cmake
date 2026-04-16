package consulo.cmake.setting;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.externalSystem.setting.AbstractExternalSystemSettings;
import consulo.externalSystem.setting.ExternalSystemSettingsListener;
import consulo.application.Application;
import consulo.project.Project;
import consulo.util.xml.serializer.annotation.AbstractCollection;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import jakarta.annotation.Nonnull;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author VISTALL
 */
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
@State(name = "CMakeSettings", storages = @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/cmake.xml"))
public class CMakeSettings extends AbstractExternalSystemSettings<CMakeSettings, CMakeProjectSettings, CMakeSettingsListener>
    implements PersistentStateComponent<CMakeSettings.MyState> {

    @Inject
    public CMakeSettings(@Nonnull Project project) {
        super(CMakeSettingsListener.TOPIC, project);
    }

    @Nonnull
    public static CMakeSettings getInstance(@Nonnull Project project) {
        return project.getInstance(CMakeSettings.class);
    }

    @Override
    public void subscribe(@Nonnull ExternalSystemSettingsListener<CMakeProjectSettings> listener) {
        getProject().getMessageBus().connect(getProject()).subscribe(CMakeSettingsListener.TOPIC, new CMakeSettingsListenerAdapter(listener));
    }

    private static class CMakeSettingsListenerAdapter extends consulo.externalSystem.setting.DelegatingExternalSystemSettingsListener<CMakeProjectSettings>
        implements CMakeSettingsListener {
        CMakeSettingsListenerAdapter(ExternalSystemSettingsListener<CMakeProjectSettings> delegate) {
            super(delegate);
        }
    }

    @Override
    protected void copyExtraSettingsFrom(@Nonnull CMakeSettings settings) {
    }

    @Override
    protected void checkSettings(@Nonnull CMakeProjectSettings old, @Nonnull CMakeProjectSettings current) {
    }

    @Override
    public MyState getState() {
        MyState state = new MyState();
        fillState(state);
        return state;
    }

    @Override
    public void loadState(@Nonnull MyState state) {
        super.loadState(state);
    }

    public static class MyState implements State<CMakeProjectSettings> {
        private Set<CMakeProjectSettings> myProjectSettings = new TreeSet<>();

        @Override
        @AbstractCollection(surroundWithTag = false, elementTypes = {CMakeProjectSettings.class})
        public Set<CMakeProjectSettings> getLinkedExternalProjectsSettings() {
            return myProjectSettings;
        }

        @Override
        public void setLinkedExternalProjectsSettings(Set<CMakeProjectSettings> settings) {
            if (settings != null) {
                myProjectSettings.addAll(settings);
            }
        }
    }
}
