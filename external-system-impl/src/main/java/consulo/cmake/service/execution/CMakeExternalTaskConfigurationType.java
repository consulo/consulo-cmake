package consulo.cmake.service.execution;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.externalSystem.service.execution.AbstractExternalSystemTaskConfigurationType;

/**
 * @author VISTALL
 */
@ExtensionImpl
public class CMakeExternalTaskConfigurationType extends AbstractExternalSystemTaskConfigurationType {
    public CMakeExternalTaskConfigurationType() {
        super(CMakeConstants.SYSTEM_ID);
    }
}
