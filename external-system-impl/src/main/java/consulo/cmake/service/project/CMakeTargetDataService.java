/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.cmake.service.project;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.module.CMakeTarget;
import consulo.cmake.run.CMakeApplicationConfigurationCreator;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.service.module.extension.ExternalSystemMutableModuleExtension;
import consulo.externalSystem.service.project.manage.ProjectDataService;
import consulo.externalSystem.util.DisposeAwareProjectChange;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.project.Project;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2026-10-10
 */
@ExtensionImpl
public class CMakeTargetDataService implements ProjectDataService<CMakeTargetData, Module> {
    @Override
    public Key<CMakeTargetData> getTargetDataKey() {
        return CMakeTargetData.KEY;
    }

    @Override
    public void importData(Collection<DataNode<CMakeTargetData>> toImport, Project project, boolean synchronous) {
        if (toImport.isEmpty()) {
            return;
        }
        Map<String, List<CMakeTarget>> targetsByModule = new LinkedHashMap<>();
        for (DataNode<CMakeTargetData> node : toImport) {
            ModuleData moduleData = node.getData(ProjectKeys.MODULE);
            if (moduleData == null) {
                continue;
            }
            CMakeTargetData data = node.getData();
            String artifact = data.getArtifactPath();
            targetsByModule.computeIfAbsent(moduleData.getInternalName(), name -> new ArrayList<>())
                .add(new CMakeTarget(data.getTargetName(), data.getType(), artifact == null ? null : Path.of(artifact), Path.of(data.getBuildDirectory())));
        }
        ExternalSystemApiUtil.executeProjectChangeAction(synchronous, new DisposeAwareProjectChange(project) {
            @Override
            public void execute() {
                ModuleManager moduleManager = ModuleManager.getInstance(project);
                for (Map.Entry<String, List<CMakeTarget>> entry : targetsByModule.entrySet()) {
                    Module module = moduleManager.findModuleByName(entry.getKey());
                    if (module != null && !module.isDisposed()) {
                        apply(module, entry.getValue());
                    }
                }
            }
        });
        CMakeApplicationConfigurationCreator.createConfigurations(project);
    }

    private static void apply(Module module, List<CMakeTarget> targets) {
        ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
        ExternalSystemMutableModuleExtension<?> extension = rootModel.getExtensionWithoutCheck(CMakeTarget.EXTENSION_ID);
        if (extension == null) {
            rootModel.dispose();
            return;
        }
        CMakeTarget.store(extension, targets);
        rootModel.commit();
    }

    @Override
    public void removeData(Collection<? extends Module> toRemove, Project project, boolean synchronous) {
    }
}
