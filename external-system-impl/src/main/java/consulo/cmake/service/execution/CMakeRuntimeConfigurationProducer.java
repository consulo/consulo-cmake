package consulo.cmake.service.execution;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.service.execution.AbstractExternalSystemRuntimeConfigurationProducer;

/**
 * @author VISTALL
 */
@ExtensionImpl
public class CMakeRuntimeConfigurationProducer extends AbstractExternalSystemRuntimeConfigurationProducer {
    public CMakeRuntimeConfigurationProducer() {
        super(new CMakeExternalTaskConfigurationType());
    }
}
