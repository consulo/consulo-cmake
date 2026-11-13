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
package consulo.cmake.configure;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.component.extension.ExtensionPointName;
import consulo.process.cmd.GeneralCommandLine;

/**
 * Adds to the command line which configures a CMake project - its environment or its cache variables, as a plugin
 * knows a compiler CMake would not find by itself: the CUDA plugin gives {@code CUDACXX} the nvcc of the toolkit when
 * it is not on the {@code PATH}.
 *
 * @author VISTALL
 * @since 2026-11-13
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface CMakeConfigureContributor {
    ExtensionPointName<CMakeConfigureContributor> EP_NAME = ExtensionPointName.create(CMakeConfigureContributor.class);

    /**
     * Adds to {@code commandLine}, {@code cmake -S <source> -B <build>}, before it runs.
     */
    void contribute(GeneralCommandLine commandLine);
}
