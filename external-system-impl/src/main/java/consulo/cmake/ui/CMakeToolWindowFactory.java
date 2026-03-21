package consulo.cmake.ui;

import consulo.annotation.component.ExtensionImpl;
import consulo.cmake.externalSystem.impl.CMakeConstants;
import consulo.cmake.icon.CMakeIconGroup;
import consulo.externalSystem.ui.AbstractExternalSystemToolWindowFactory;
import consulo.ui.ex.toolWindow.ToolWindowAnchor;
import consulo.ui.image.Image;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 */
@ExtensionImpl
public class CMakeToolWindowFactory extends AbstractExternalSystemToolWindowFactory {
    public CMakeToolWindowFactory() {
        super(CMakeConstants.SYSTEM_ID);
    }

    @Nonnull
    @Override
    public ToolWindowAnchor getAnchor() {
        return ToolWindowAnchor.RIGHT;
    }

    @Nonnull
    @Override
    public Image getIcon() {
        return CMakeIconGroup.cmaketoolwindow();
    }
}
