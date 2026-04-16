/**
 * @author VISTALL
 */
module consulo.cmake.language.impl {
    requires consulo.cmake;
    requires consulo.ide.api;
    requires consulo.language.impl;
    requires consulo.configurable.api;
    requires consulo.file.editor.api;
    requires consulo.language.editor.refactoring.api;

    exports consulo.cmake.psi;
    exports consulo.cmake.psi.impl;
    exports consulo.cmake.utils;
}
