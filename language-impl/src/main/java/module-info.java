/**
 * @author VISTALL
 */
module consulo.cmake.language.impl {
    requires consulo.cmake;
    requires consulo.ide.api;

    exports consulo.cmake.psi;
    exports consulo.cmake.psi.impl;
    exports consulo.cmake.utils;
}
