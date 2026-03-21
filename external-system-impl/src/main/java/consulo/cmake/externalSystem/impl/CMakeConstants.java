package consulo.cmake.externalSystem.impl;

import consulo.cmake.icon.CMakeIconGroup;
import consulo.cmake.localize.CMakeLocalize;
import consulo.externalSystem.model.ProjectSystemId;

/**
 * @author VISTALL
 */
public interface CMakeConstants {
    ProjectSystemId SYSTEM_ID = new ProjectSystemId("CMAKE", CMakeLocalize.cmakeSystemName(), CMakeIconGroup.cmake());

    String CMAKE_LISTS_TXT = "CMakeLists.txt";
}
