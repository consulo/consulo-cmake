package consulo.cmake;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileNameMatcherFactory;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;
import jakarta.inject.Inject;
import jakarta.annotation.Nonnull;

@ExtensionImpl
public class CMakeFileTypeFactory extends FileTypeFactory {
    private final FileNameMatcherFactory myFileNameMatcherFactory;

    @Inject
    public CMakeFileTypeFactory(FileNameMatcherFactory fileNameMatcherFactory) {
        myFileNameMatcherFactory = fileNameMatcherFactory;
    }

    @Override
    public void createFileTypes(@Nonnull FileTypeConsumer fileTypeConsumer) {
        fileTypeConsumer.consume(CMakeFileType.INSTANCE);
        
        fileTypeConsumer.consume(CMakeFileType.INSTANCE, myFileNameMatcherFactory.createExactFileNameMatcher("CMakeLists.txt"));
    }
}